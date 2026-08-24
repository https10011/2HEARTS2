/**
 * Memory service (Phase 7).
 *
 * Application-facing boundary over MemoryRepository and MediaStorage.
 * Validates input, coordinates media references, normalizes errors,
 * and prevents orphaned media.
 *
 * Layer discipline: UI → MemoryService → Repository + MediaStorage → Local persistence.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import { AppError } from '../errors/appError.ts';
import { normalizeInput, textLength, validIsoDate, validate } from '../validation/validators.ts';
import { MemoryRepository } from '../../repositories/memoryRepository.ts';
import { MediaStorage, type MediaReference } from '../../data/media/mediaStorage.ts';
import type { MediaFileSystem } from '../../data/media/fileSystem.ts';
import type { MediaKind } from '../../data/media/mediaTypes.ts';
import { verifyMediaBytes } from '../media/mediaUtils.ts';
import { systemClock, type Clock } from '../../utils/time.ts';

const MAX_TITLE_LENGTH = 100;
const MAX_CAPTION_LENGTH = 2000;

export interface MemoryInput {
  title: string;
  caption?: string | null;
  memoryDate?: string | null;
  sortOrder?: number;
}

export interface MemoryWithMedia {
  id: string;
  title: string;
  caption: string | null;
  memoryDate: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  mediaReferences: MediaReference[];
}

function validationFailure(errors: string[]): never {
  throw new AppError('validation', 'invalid-input', {
    recoverable: true,
    userMessage: 'Please check the highlighted fields.',
    cause: { errors },
  });
}

export class MemoryService {
  private readonly memories: MemoryRepository;
  private readonly mediaStorage: MediaStorage;

  constructor(
    db: DatabaseAdapter,
    fs: MediaFileSystem,
    clock: Clock = systemClock,
  ) {
    this.memories = new MemoryRepository(db, clock);
    this.mediaStorage = new MediaStorage(db, fs, clock);
  }

  /** Lists all active memories with their media references. */
  async listMemories(): Promise<MemoryWithMedia[]> {
    const memories = await this.memories.listMemories();
    const result: MemoryWithMedia[] = [];

    for (const memory of memories) {
      const mediaIds = await this.memories.getMemoryMediaIds(memory.id);
      const mediaReferences: MediaReference[] = [];
      for (const mediaId of mediaIds) {
        try {
          const asset = await this.mediaStorage.getMetadata(mediaId);
          mediaReferences.push({
            id: asset.id,
            kind: asset.kind,
            mimeType: asset.mimeType,
          });
        } catch {
          // Media asset missing/deleted — skip gracefully
        }
      }
      result.push({ ...memory, mediaReferences });
    }

    return result;
  }

  /** Gets a single memory with its media references. */
  async getMemory(id: string): Promise<MemoryWithMedia> {
    const memory = await this.memories.getById(id);
    if (!memory) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Memory not found.',
      });
    }

    const mediaIds = await this.memories.getMemoryMediaIds(id);
    const mediaReferences: MediaReference[] = [];
    for (const mediaId of mediaIds) {
      try {
        const asset = await this.mediaStorage.getMetadata(mediaId);
        mediaReferences.push({
          id: asset.id,
          kind: asset.kind,
          mimeType: asset.mimeType,
        });
      } catch {
        // Media asset missing — skip gracefully
      }
    }

    return { ...memory, mediaReferences };
  }

  /** Creates a new memory with optional media. */
  async createMemory(
    input: MemoryInput,
    mediaItems: Array<{ kind: MediaKind; mimeType: string; data: Uint8Array }> = [],
  ): Promise<MemoryWithMedia> {
    const title = normalizeInput(input.title);
    const caption = input.caption ? normalizeInput(input.caption) : null;
    const memoryDate = input.memoryDate ?? null;

    // Validate
    const result = validate(
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      caption !== null ? textLength(caption, 0, MAX_CAPTION_LENGTH, 'Caption') : { ok: true, errors: [] },
      memoryDate !== null ? validIsoDate(memoryDate) : { ok: true, errors: [] },
    );
    if (!result.ok) validationFailure(result.errors);

    // Validate media items
    for (const item of mediaItems) {
      const mediaResult = verifyMediaBytes({
        kind: item.kind,
        declaredMimeType: item.mimeType,
        data: item.data,
      });
      if (!mediaResult.ok) {
        validationFailure(mediaResult.errors);
      }
    }

    // Get next sort order
    const count = await this.memories.count();
    const sortOrder = input.sortOrder ?? count;

    // Create memory + store media in a coordinated fashion
    const memory = await this.memories.create({
      title,
      caption,
      memoryDate,
      sortOrder,
      deletedAt: null,
    });

    // Store media items and associate them
    const mediaReferences: MediaReference[] = [];
    try {
      for (const item of mediaItems) {
        const ref = await this.mediaStorage.store(item.kind, item.mimeType, item.data);
        await this.memories.addMediaToMemory(memory.id, ref.id);
        mediaReferences.push(ref);
      }
    } catch (cause) {
      // If media storage fails after memory was created, the memory
      // still exists (it's valid without media). Log the error but
      // don't delete the memory — partial success is better than data loss.
      if (cause instanceof AppError) throw cause;
      throw new AppError('media', 'media-store-failed', {
        recoverable: true,
        userMessage: 'Memory saved but some media could not be stored.',
        cause,
      });
    }

    return { ...memory, mediaReferences };
  }

  /** Updates an existing memory. */
  async updateMemory(
    id: string,
    input: Partial<MemoryInput>,
  ): Promise<MemoryWithMedia> {
    const existing = await this.memories.getById(id);
    if (!existing) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Memory not found.',
      });
    }

    const changes: Record<string, unknown> = {};

    if (input.title !== undefined) {
      const title = normalizeInput(input.title);
      const result = validate(textLength(title, 1, MAX_TITLE_LENGTH, 'Title'));
      if (!result.ok) validationFailure(result.errors);
      changes.title = title;
    }

    if (input.caption !== undefined) {
      const caption = input.caption ? normalizeInput(input.caption) : null;
      if (caption !== null) {
        const result = validate(textLength(caption, 0, MAX_CAPTION_LENGTH, 'Caption'));
        if (!result.ok) validationFailure(result.errors);
      }
      changes.caption = caption;
    }

    if (input.memoryDate !== undefined) {
      if (input.memoryDate !== null) {
        const result = validate(validIsoDate(input.memoryDate));
        if (!result.ok) validationFailure(result.errors);
      }
      changes.memoryDate = input.memoryDate;
    }

    if (input.sortOrder !== undefined) {
      changes.sortOrder = input.sortOrder;
    }

    const updated = await this.memories.update(id, changes);
    const mediaIds = await this.memories.getMemoryMediaIds(id);
    const mediaReferences: MediaReference[] = [];
    for (const mediaId of mediaIds) {
      try {
        const asset = await this.mediaStorage.getMetadata(mediaId);
        mediaReferences.push({
          id: asset.id,
          kind: asset.kind,
          mimeType: asset.mimeType,
        });
      } catch {
        // Skip missing media
      }
    }

    return { ...updated, mediaReferences };
  }

  /** Adds media to an existing memory. */
  async addMedia(
    memoryId: string,
    kind: MediaKind,
    mimeType: string,
    data: Uint8Array,
  ): Promise<MediaReference> {
    const memory = await this.memories.getById(memoryId);
    if (!memory) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Memory not found.',
      });
    }

    const mediaResult = verifyMediaBytes({ kind, declaredMimeType: mimeType, data });
    if (!mediaResult.ok) validationFailure(mediaResult.errors);

    const ref = await this.mediaStorage.store(kind, mimeType, data);
    await this.memories.addMediaToMemory(memoryId, ref.id);
    return ref;
  }

  /**
   * Resolves a renderable `data:` URL for a media asset (photo-first UI).
   * Delegates to MediaStorage so raw filesystem paths never reach UI code.
   */
  async resolveMediaUrl(mediaAssetId: string): Promise<string> {
    return this.mediaStorage.resolveUrl(mediaAssetId);
  }

  /** Removes media from a memory. Also deletes the media asset. */
  async removeMedia(memoryId: string, mediaAssetId: string): Promise<boolean> {
    const removed = await this.memories.removeMediaFromMemory(memoryId, mediaAssetId);
    if (removed) {
      // Best-effort media asset deletion
      try {
        await this.mediaStorage.delete(mediaAssetId);
      } catch {
        // Media file may already be gone — metadata tombstone is sufficient
      }
    }
    return removed;
  }

  /**
   * Deletes a memory (soft-delete via tombstone) and cleans up its media.
   * Media assets are tombstoned individually for V2 sync compatibility.
   */
  async deleteMemory(id: string): Promise<boolean> {
    const memory = await this.memories.getById(id);
    if (!memory) return false;

    // Get media references before clearing the join table
    const mediaIds = await this.memories.getMemoryMediaIds(id);

    // Clear join table associations
    await this.memories.clearMemoryMedia(id);

    // Delete the memory (soft-delete)
    const deleted = await this.memories.delete(id);

    // Best-effort: tombstone the media assets
    for (const mediaId of mediaIds) {
      try {
        await this.mediaStorage.delete(mediaId);
      } catch {
        // Media may already be gone — orphan sweep handles leftovers
      }
    }

    return deleted;
  }

  /** Validates memory input without persisting. */
  validateInput(input: MemoryInput): { ok: boolean; errors: string[] } {
    const title = normalizeInput(input.title);
    const caption = input.caption ? normalizeInput(input.caption) : null;
    const memoryDate = input.memoryDate ?? null;

    const results = [
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      caption !== null ? textLength(caption, 0, MAX_CAPTION_LENGTH, 'Caption') : { ok: true, errors: [] as string[] },
      memoryDate !== null ? validIsoDate(memoryDate) : { ok: true, errors: [] as string[] },
    ];
    return validate(...results);
  }
}
