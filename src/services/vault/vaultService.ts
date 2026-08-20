/**
 * Vault service (Phase 17).
 *
 * Application-facing boundary for the private vault system.
 * Manages: create/update/delete vault items, access control via AppLockService,
 * validation, error normalization.
 *
 * Vault content must NOT appear in global search or ordinary feature lists.
 * Architecture: UI → VaultService → VaultRepository → Local persistence.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import {
  type VaultItem,
  type VaultContentType,
  VAULT_CONTENT_TYPES,
} from '../../data/vault/vaultTypes.ts';
import { VaultRepository } from '../../repositories/vaultRepository.ts';
import type { AppLockService, LockState } from '../security/appLockService.ts';

export interface CreateVaultItemInput {
  title: string;
  contentType: VaultContentType;
  mediaRef?: string | null;
  filePath?: string | null;
  content?: string | null;
  description?: string | null;
  profileId: string;
}

export interface UpdateVaultItemInput {
  title?: string;
  description?: string | null;
  content?: string | null;
}

export class VaultService {
  private lockUnsub: (() => void) | null = null;

  constructor(
    private readonly repository: VaultRepository,
    private readonly appLock: AppLockService,
  ) {
    // Subscribe to lock state changes for potential future use
    this.lockUnsub = this.appLock.onLockChange(() => {
      // Lock state changes are handled by querying appLock directly
    });
  }

  // -----------------------------------------------------------------------
  // Access control
  // -----------------------------------------------------------------------

  /** Whether the vault is currently accessible (unlocked or no lock configured). */
  isAccessible(): boolean {
    const lockState = this.appLock.currentState();
    return lockState === 'disabled' || lockState === 'unlocked';
  }

  /** Get current lock state. */
  getLockState(): LockState {
    return this.appLock.currentState();
  }

  /** Whether app lock is configured at all. */
  isLockConfigured(): boolean {
    return this.appLock.isConfigured();
  }

  /** Attempt to unlock vault with PIN. Returns true on success. */
  async unlock(pin: string): Promise<boolean> {
    return this.appLock.unlock(pin);
  }

  /** Explicitly lock the vault. */
  lock(): void {
    this.appLock.lock();
  }

  // -----------------------------------------------------------------------
  // CRUD (requires unlocked state)
  // -----------------------------------------------------------------------

  private assertAccessible(): void {
    if (!this.isAccessible()) {
      throw new AppError('security', 'vault-locked', {
        recoverable: true,
        userMessage: 'Vault is locked. Please unlock to continue.',
      });
    }
  }

  async create(input: CreateVaultItemInput): Promise<VaultItem> {
    this.assertAccessible();
    this.validateInput(input);

    try {
      return await this.repository.create({
        title: input.title.trim(),
        contentType: input.contentType,
        mediaRef: input.mediaRef ?? null,
        filePath: input.filePath ?? null,
        content: input.content?.trim() ?? null,
        description: input.description?.trim() ?? null,
        profileId: input.profileId,
      });
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'vault-create-failed', {
        recoverable: false,
        userMessage: 'Could not add vault content.',
      });
    }
  }

  async update(id: string, input: UpdateVaultItemInput): Promise<VaultItem> {
    this.assertAccessible();

    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Vault item not found.',
      });
    }

    if (input.title !== undefined) this.validateTitle(input.title);

    try {
      const updated = await this.repository.update(id, {
        title: input.title?.trim(),
        description: input.description?.trim(),
        content: input.content?.trim(),
      });
      if (!updated) throw new Error('Update returned null');
      return updated;
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'vault-update-failed', {
        recoverable: false,
        userMessage: 'Could not update vault item.',
      });
    }
  }

  async delete(id: string): Promise<void> {
    this.assertAccessible();

    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Vault item not found.',
      });
    }

    // Soft delete the database record
    // Note: physical file deletion from filesystem would happen here
    // if we had filesystem access — for now, soft-delete is sufficient
    // as the reference becomes unreachable.
    await this.repository.delete(id);
  }

  async getById(id: string): Promise<VaultItem | null> {
    if (!this.isAccessible()) return null;
    return this.repository.getById(id);
  }

  async list(profileId: string): Promise<VaultItem[]> {
    if (!this.isAccessible()) return [];
    return this.repository.list(profileId);
  }

  async listByType(profileId: string, contentType: VaultContentType): Promise<VaultItem[]> {
    if (!this.isAccessible()) return [];
    return this.repository.listByType(profileId, contentType);
  }

  async count(profileId: string): Promise<number> {
    if (!this.isAccessible()) return 0;
    return this.repository.count(profileId);
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  private validateInput(input: CreateVaultItemInput): void {
    this.validateTitle(input.title);
    if (!VAULT_CONTENT_TYPES.includes(input.contentType)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid content type.',
      });
    }
    if (!input.profileId) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Profile ID is required.',
      });
    }
  }

  private validateTitle(title: string): void {
    if (!title.trim()) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Vault item title is required.',
      });
    }
    if (title.trim().length > 200) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Title is too long (max 200 characters).',
      });
    }
  }

  /** Cleanup subscription — call when service is no longer needed. */
  dispose(): void {
    this.lockUnsub?.();
    this.lockUnsub = null;
  }
}
