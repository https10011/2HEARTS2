/**
 * Profile photo service (Stage 4).
 *
 * Handles the complete profile-image lifecycle:
 *   1. Pick image from device gallery (via hidden <input type="file">)
 *   2. Process: resize to avatar-appropriate dimensions, compress as JPEG
 *   3. Store via the existing MediaStorage pipeline (local filesystem + metadata)
 *   4. Link the MediaAsset.id to the Profile.photoRef column
 *   5. Resolve display URLs via MediaStorage.resolveUrl()
 *   6. Clean up old photos on replace/remove
 *
 * Design decisions:
 *   - Max output: 400×400px, JPEG 85% — looks crisp on all Android densities
 *     while staying under 100KB per image
 *   - Uses canvas-based resize (no native plugin needed)
 *   - Gallery-only picker (file input) — works in browser dev AND Android WebView
 *   - Camera access via file input `capture="environment"` attribute on Android
 *   - No Capacitor Camera plugin required — zero new native dependencies
 *   - All storage through existing MediaStorage — no duplicate systems
 */

import type { RelationshipService } from '../relationship/relationshipService.ts';
import type { MediaStorage } from '../../data/media/mediaStorage.ts';

/** Maximum output dimension (px) for profile photos. */
const MAX_DIMENSION = 400;
/** JPEG quality for compression (0–1). */
const JPEG_QUALITY = 0.85;
/** Accepted MIME types for the file input. */
const ACCEPTED_TYPES = 'image/jpeg,image/png,image/webp';

export interface PhotoProcessResult {
  /** MediaAsset.id after successful store. */
  mediaRefId: string;
  /** data: URL for immediate display. */
  dataUrl: string;
}

export class ProfilePhotoService {
  constructor(
    private readonly mediaStorage: MediaStorage,
    private readonly relationshipService: RelationshipService,
  ) {}

  /**
   * Opens the native photo picker. Returns the selected File, or null if
   * the user cancelled. Uses `capture="environment"` on Android to offer
   * camera as an option alongside the gallery.
   */
  pickPhoto(): Promise<File | null> {
    return new Promise((resolve) => {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = ACCEPTED_TYPES;
      input.capture = 'environment';
      input.style.display = 'none';

      input.addEventListener('change', () => {
        const file = input.files?.[0] ?? null;
        input.remove();
        resolve(file);
      });

      // User cancelled (e.g. pressed back on Android)
      input.addEventListener('cancel', () => {
        input.remove();
        resolve(null);
      });

      document.body.appendChild(input);
      input.click();
    });
  }

  /**
   * Processes a raw image File into an avatar-optimized JPEG.
   * Returns the processed bytes and dimensions.
   */
  async processImage(file: File): Promise<{ bytes: Uint8Array; width: number; height: number }> {
    const bitmap = await createImageBitmap(file);

    // Calculate target dimensions preserving aspect ratio
    let { width, height } = bitmap;
    if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
      const scale = MAX_DIMENSION / Math.max(width, height);
      width = Math.round(width * scale);
      height = Math.round(height * scale);
    }

    // Draw to canvas for resize + JPEG conversion
    const canvas = new OffscreenCanvas(width, height);
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Canvas context unavailable');
    ctx.drawImage(bitmap, 0, 0, width, height);
    bitmap.close();

    const blob = await canvas.convertToBlob({ type: 'image/jpeg', quality: JPEG_QUALITY });
    const buffer = await blob.arrayBuffer();
    return { bytes: new Uint8Array(buffer), width, height };
  }

  /**
   * Full pipeline: pick → process → store → link to profile.
   * Returns the data URL for immediate display, or null on cancel/failure.
   */
  async selectAndSavePhoto(role: 'owner' | 'partner'): Promise<PhotoProcessResult | null> {
    const file = await this.pickPhoto();
    if (!file) return null;

    const { bytes } = await this.processImage(file);

    // Store via existing MediaStorage (filesystem + metadata)
    const ref = await this.mediaStorage.store('photo', 'image/jpeg', bytes);

    // Get display URL before updating profile (in case profile update fails)
    const dataUrl = await this.mediaStorage.resolveUrl(ref.id);

    // Link to profile
    await this.relationshipService.setProfilePhoto(role, ref.id);

    return { mediaRefId: ref.id, dataUrl };
  }

  /**
   * Removes the profile photo. Tombstones the media asset and clears
   * the profile's photoRef. The old image file is cleaned up by
   * MediaStorage.delete().
   */
  async removePhoto(role: 'owner' | 'partner'): Promise<void> {
    const profile = role === 'owner'
      ? await this.relationshipService.getOwner()
      : await this.relationshipService.getPartner();

    if (profile?.photoRef) {
      // Clean up old media asset (best effort — don't fail if cleanup errors)
      await this.mediaStorage.delete(profile.photoRef).catch(() => undefined);
    }

    // Clear the reference
    await this.relationshipService.setProfilePhoto(role, null);
  }

  /**
   * Replaces an existing photo. Stores the new image first, then cleans
   * up the old one — never leaves the profile in a broken state.
   */
  async replacePhoto(role: 'owner' | 'partner'): Promise<PhotoProcessResult | null> {
    const profile = role === 'owner'
      ? await this.relationshipService.getOwner()
      : await this.relationshipService.getPartner();
    const oldRef = profile?.photoRef ?? null;

    const result = await this.selectAndSavePhoto(role);

    // Clean up old media after successful replacement
    if (result && oldRef) {
      await this.mediaStorage.delete(oldRef).catch(() => undefined);
    }

    return result;
  }

  /**
   * Resolves a display URL for a profile's photo.
   * Returns null if the profile has no photo or resolution fails.
   */
  async resolvePhotoUrl(photoRef: string | null): Promise<string | null> {
    if (!photoRef) return null;
    try {
      return await this.mediaStorage.resolveUrl(photoRef);
    } catch {
      return null;
    }
  }
}
