/**
 * CreatePlace / EditPlace (Phase 14).
 *
 * Form for creating or editing a place. Supports all place fields:
 * name, address, city, state, country, coordinates, notes, category.
 * Uses real persisted data via PlaceService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';
import { PlaceService } from '../../services/place/placeService.ts';
import { AppError } from '../../services/errors/appError.ts';
import { useToast } from '../../components/index.ts';


const CATEGORY_OPTIONS = ['Restaurant', 'Vacation', 'Home', 'Adventure', 'Special', 'Other'] as const;

let _placeService: PlaceService | null = null;
async function getPlaceService(): Promise<PlaceService> {
  if (!_placeService) {
    const repo = new PlaceRepository(await getDatabase());
    _placeService = new PlaceService(repo);
  }
  return _placeService;
}

export function CreatePlace() {
  const navigate = useNavigate();
  const { placeId } = useParams<{ placeId: string }>();
  const isEditing = Boolean(placeId);
  const toast = useToast();

  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [country, setCountry] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [notes, setNotes] = useState('');
  const [category, setCategory] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingPlace, setLoadingPlace] = useState(isEditing);

  useEffect(() => {
    if (!placeId) return;
    const load = async () => {
      try {
        const service = await getPlaceService();
        const place = await service.getById(placeId);
        if (place) {
          setName(place.name);
          setAddress(place.address ?? '');
          setCity(place.city ?? '');
          setState(place.state ?? '');
          setCountry(place.country ?? '');
          setLatitude(place.latitude != null ? String(place.latitude) : '');
          setLongitude(place.longitude != null ? String(place.longitude) : '');
          setNotes(place.notes ?? '');
          setCategory(place.category ?? '');
        }
      } catch {
        setError('Could not load place.');
      } finally {
        setLoadingPlace(false);
      }
    };
    load();
  }, [placeId]);

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      const service = await getPlaceService();
      const data = {
        name,
        address: address || null,
        city: city || null,
        state: state || null,
        country: country || null,
        latitude: latitude ? parseFloat(latitude) : null,
        longitude: longitude ? parseFloat(longitude) : null,
        notes: notes || null,
        category: category || null,
      };

      if (isEditing && placeId) {
        await service.update(placeId, data);
        toast.success('Place updated');
      } else {
        await service.create(data);
        toast.success('Place saved');
      }
      navigate(RoutePath.appPlaces);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
      }
      toast.error('Could not save place');
    } finally {
      setSaving(false);
    }
  }, [name, address, city, state, country, latitude, longitude, notes, category, isEditing, placeId, navigate, toast]);

  if (loadingPlace) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading place...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        {isEditing ? 'Edit Place' : 'Add Place'}
      </h1>

      {error && (
        <div className="th-error-banner" style={{ marginBottom: 'var(--th-space-4)' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-4)' }}>
        {/* Name */}
        <div>
          <label className="th-label">Place Name *</label>
          <input
            type="text"
            className="th-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g., Our favorite café"
            required
            maxLength={200}
          />
        </div>

        {/* Category */}
        <div>
          <label className="th-label">Category</label>
          <div style={{ display: 'flex', gap: 'var(--th-space-2)', flexWrap: 'wrap' }}>
            {CATEGORY_OPTIONS.map((cat) => (
              <button
                key={cat}
                type="button"
                className={`th-btn th-btn--sm ${category === cat ? 'th-btn--primary' : 'th-btn--outline'}`}
                onClick={() => setCategory(category === cat ? '' : cat)}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* Address */}
        <div>
          <label className="th-label">Address</label>
          <input
            type="text"
            className="th-input"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Street address"
          />
        </div>

        {/* City / State / Country row */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--th-space-3)' }}>
          <div>
            <label className="th-label">City</label>
            <input
              type="text"
              className="th-input"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="City"
            />
          </div>
          <div>
            <label className="th-label">State</label>
            <input
              type="text"
              className="th-input"
              value={state}
              onChange={(e) => setState(e.target.value)}
              placeholder="State"
            />
          </div>
        </div>

        <div>
          <label className="th-label">Country</label>
          <input
            type="text"
            className="th-input"
            value={country}
            onChange={(e) => setCountry(e.target.value)}
            placeholder="Country"
          />
        </div>

        {/* Coordinates */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--th-space-3)' }}>
          <div>
            <label className="th-label">Latitude</label>
            <input
              type="number"
              step="any"
              className="th-input"
              value={latitude}
              onChange={(e) => setLatitude(e.target.value)}
              placeholder="e.g., 40.7128"
            />
          </div>
          <div>
            <label className="th-label">Longitude</label>
            <input
              type="number"
              step="any"
              className="th-input"
              value={longitude}
              onChange={(e) => setLongitude(e.target.value)}
              placeholder="e.g., -74.0060"
            />
          </div>
        </div>

        {/* Notes */}
        <div>
          <label className="th-label">Notes</label>
          <textarea
            className="th-input"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Why is this place special?"
            rows={3}
            style={{ resize: 'vertical' }}
          />
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 'var(--th-space-3)', marginTop: 'var(--th-space-2)' }}>
          <button
            type="button"
            className="th-btn th-btn--outline"
            onClick={() => navigate(-1)}
            style={{ flex: 1 }}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="th-btn th-btn--primary"
            disabled={saving || !name.trim()}
            style={{ flex: 1 }}
          >
            {saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Add Place'}
          </button>
        </div>
      </form>
    </div>
  );
}
