/**
 * Place service (Phase 14).
 *
 * Application-facing boundary for the local places system.
 * Manages: create, update, delete, validation, error normalization,
 * search integration.
 *
 * Architecture: UI → PlaceService → PlaceRepository → Local persistence.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import {
  type Place,
  type NewPlace,
} from '../../data/place/placeTypes.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';

export interface CreatePlaceInput {
  name: string;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  notes?: string | null;
  category?: string | null;
  photoRef?: string | null;
  memoryId?: string | null;
}

export interface UpdatePlaceInput {
  name?: string;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  notes?: string | null;
  category?: string | null;
  photoRef?: string | null;
  memoryId?: string | null;
}

export class PlaceService {
  constructor(
    private readonly repository: PlaceRepository,
  ) {}

  // -----------------------------------------------------------------------
  // Create
  // -----------------------------------------------------------------------

  async create(input: CreatePlaceInput): Promise<Place> {
    this.validateInput(input);

    const data: NewPlace = {
      name: input.name.trim(),
      address: input.address?.trim() ?? null,
      city: input.city?.trim() ?? null,
      state: input.state?.trim() ?? null,
      country: input.country?.trim() ?? null,
      latitude: input.latitude ?? null,
      longitude: input.longitude ?? null,
      notes: input.notes?.trim() ?? null,
      category: input.category?.trim() ?? null,
      photoRef: input.photoRef ?? null,
      memoryId: input.memoryId ?? null,
    };

    try {
      return await this.repository.create(data);
    } catch (cause) {
      throw normalizeAppError(cause, 'validation', 'create-failed', {
        recoverable: false,
        userMessage: 'Could not create place.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, input: UpdatePlaceInput): Promise<Place> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Place not found.',
      });
    }

    if (input.name !== undefined) this.validateName(input.name);

    try {
      const updated = await this.repository.update(id, {
        ...input,
        name: input.name?.trim(),
        address: input.address?.trim(),
        city: input.city?.trim(),
        state: input.state?.trim(),
        country: input.country?.trim(),
        notes: input.notes?.trim(),
        category: input.category?.trim(),
      });
      if (!updated) throw new Error('Update returned null');
      return updated;
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'update-failed', {
        recoverable: false,
        userMessage: 'Could not update place.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Delete
  // -----------------------------------------------------------------------

  async delete(id: string): Promise<void> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Place not found.',
      });
    }

    try {
      await this.repository.delete(id);
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'delete-failed', {
        recoverable: false,
        userMessage: 'Could not delete place.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<Place | null> {
    return this.repository.getById(id);
  }

  async list(): Promise<Place[]> {
    return this.repository.list();
  }

  async listByCategory(category: string): Promise<Place[]> {
    return this.repository.listByCategory(category);
  }

  async search(query: string): Promise<Place[]> {
    if (!query.trim()) return [];
    return this.repository.search(query.trim());
  }

  async count(): Promise<number> {
    return this.repository.count();
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  private validateInput(input: CreatePlaceInput): void {
    this.validateName(input.name);
  }

  private validateName(name: string): void {
    if (!name.trim()) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Place name is required.',
      });
    }
    if (name.trim().length > 200) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Place name is too long (max 200 characters).',
      });
    }
  }
}
