/**
 * useProfilePhotos (Stage 4) — loads and caches profile photo data URLs.
 *
 * Resolves MediaAsset.id references to data: URLs for display in avatar
 * components. Caches results to avoid re-reading from filesystem on
 * every render.
 */

import { useEffect, useState, useCallback } from 'react';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { ProfilePhotoService } from '../../services/profile/profilePhotoService.ts';

interface ProfilePhotosState {
  /** Owner photo data URL, or null (no photo or still loading). */
  ownerUrl: string | null;
  /** Partner photo data URL, or null. */
  partnerUrl: string | null;
  /** True while initial load is in progress. */
  loading: boolean;
  /** Refresh photos from database (call after save/remove). */
  refresh: () => void;
}

export function useProfilePhotos(): ProfilePhotosState {
  const [ownerUrl, setOwnerUrl] = useState<string | null>(null);
  const [partnerUrl, setPartnerUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  const refresh = useCallback(() => setRefreshKey((k) => k + 1), []);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      const relationship = coreServices.relationship;
      const mediaStorage = coreServices.mediaStorage;
      if (!relationship || !mediaStorage) {
        if (!cancelled) setLoading(false);
        return;
      }

      const photoService = new ProfilePhotoService(mediaStorage, relationship);

      try {
        const [owner, partner] = await Promise.all([
          relationship.getOwner(),
          relationship.getPartner(),
        ]);

        const [ownerUrlResult, partnerUrlResult] = await Promise.all([
          photoService.resolvePhotoUrl(owner?.photoRef ?? null),
          photoService.resolvePhotoUrl(partner?.photoRef ?? null),
        ]);

        if (!cancelled) {
          setOwnerUrl(ownerUrlResult);
          setPartnerUrl(partnerUrlResult);
        }
      } catch {
        // Graceful degradation — avatars show initials
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, [refreshKey]);

  return { ownerUrl, partnerUrl, loading, refresh };
}
