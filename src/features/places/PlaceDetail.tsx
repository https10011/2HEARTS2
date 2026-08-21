/**
 * PlaceDetail (Phase 14).
 *
 * Displays full place information with edit and delete actions.
 * Uses real persisted data via PlaceService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';
import { PlaceService } from '../../services/place/placeService.ts';
import { IconMapPin } from '../../components/index.ts';
import type { Place } from '../../data/place/placeTypes.ts';

let _placeService: PlaceService | null = null;
async function getPlaceService(): Promise<PlaceService> {
  if (!_placeService) {
    const repo = new PlaceRepository(await getDatabase());
    _placeService = new PlaceService(repo);
  }
  return _placeService;
}

export function PlaceDetail() {
  const navigate = useNavigate();
  const { placeId } = useParams<{ placeId: string }>();
  const [place, setPlace] = useState<Place | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    if (!placeId) return;
    const load = async () => {
      try {
        const service = await getPlaceService();
        const data = await service.getById(placeId);
        setPlace(data);
      } catch {
        setPlace(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [placeId]);

  const handleDelete = useCallback(async () => {
    if (!placeId) return;
    try {
      const service = await getPlaceService();
      await service.delete(placeId);
      navigate(RoutePath.appPlaces);
    } catch {
      setShowDeleteConfirm(false);
    }
  }, [placeId, navigate]);

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading place...</p>
        </div>
      </div>
    );
  }

  if (!place) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state">
          <div className="th-empty-state__icon" style={{ color: 'var(--th-color-rose-muted)' }}>
            <IconMapPin size={44} />
          </div>
          <h3 className="th-empty-state__title">Place not found</h3>
          <p className="th-empty-state__message">This place may have been deleted.</p>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appPlaces)}>
            Back to Places
          </button>
        </div>
      </div>
    );
  }

  const locationParts = [place.city, place.state, place.country].filter(Boolean);
  const hasCoordinates = place.latitude != null && place.longitude != null;

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--th-space-4)' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-1)' }}>
            <span style={{ color: 'var(--th-color-burgundy)', display: 'inline-flex' }}>
              <IconMapPin size={24} />
            </span>
            <h1 className="th-screen-title" style={{ margin: 0 }}>{place.name}</h1>
          </div>
          {place.category && (
            <span className="th-badge">{place.category}</span>
          )}
        </div>
        <div style={{ display: 'flex', gap: 'var(--th-space-2)' }}>
          <button
            className="th-btn th-btn--outline th-btn--sm"
            onClick={() => navigate(RoutePath.appPlacesEdit.replace(':placeId', place.id))}
          >
            Edit
          </button>
        </div>
      </div>

      {/* Details card */}
      <div className="th-card" style={{ padding: 'var(--th-space-4)' }}>
        {/* Address */}
        {place.address && (
          <div style={{ marginBottom: 'var(--th-space-3)' }}>
            <p className="th-label" style={{ marginBottom: 'var(--th-space-1)' }}>Address</p>
            <p style={{ margin: 0 }}>{place.address}</p>
          </div>
        )}

        {/* Location */}
        {locationParts.length > 0 && (
          <div style={{ marginBottom: 'var(--th-space-3)' }}>
            <p className="th-label" style={{ marginBottom: 'var(--th-space-1)' }}>Location</p>
            <p style={{ margin: 0 }}>{locationParts.join(', ')}</p>
          </div>
        )}

        {/* Coordinates */}
        {hasCoordinates && (
          <div style={{ marginBottom: 'var(--th-space-3)' }}>
            <p className="th-label" style={{ marginBottom: 'var(--th-space-1)' }}>Coordinates</p>
            <p style={{ margin: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
              {place.latitude?.toFixed(6)}, {place.longitude?.toFixed(6)}
            </p>
          </div>
        )}

        {/* Notes */}
        {place.notes && (
          <div style={{ marginBottom: 'var(--th-space-3)' }}>
            <p className="th-label" style={{ marginBottom: 'var(--th-space-1)' }}>Notes</p>
            <p style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{place.notes}</p>
          </div>
        )}

        {/* Metadata */}
        <div style={{ borderTop: '1px solid var(--th-color-border)', paddingTop: 'var(--th-space-3)', marginTop: 'var(--th-space-2)' }}>
          <p style={{ margin: 0, fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
            Added {new Date(place.createdAt).toLocaleDateString()}
          </p>
          {place.updatedAt !== place.createdAt && (
            <p style={{ margin: 0, fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
              Updated {new Date(place.updatedAt).toLocaleDateString()}
            </p>
          )}
        </div>
      </div>

      {/* Delete */}
      <div style={{ marginTop: 'var(--th-space-6)' }}>
        {showDeleteConfirm ? (
          <div className="th-card" style={{ padding: 'var(--th-space-4)', textAlign: 'center' }}>
            <p style={{ marginBottom: 'var(--th-space-3)' }}>Delete this place? This cannot be undone.</p>
            <div style={{ display: 'flex', gap: 'var(--th-space-3)', justifyContent: 'center' }}>
              <button className="th-btn th-btn--outline th-btn--sm" onClick={() => setShowDeleteConfirm(false)}>
                Cancel
              </button>
              <button className="th-btn th-btn--danger th-btn--sm" onClick={handleDelete}>
                Delete Place
              </button>
            </div>
          </div>
        ) : (
          <button
            className="th-btn th-btn--danger th-btn--outline"
            onClick={() => setShowDeleteConfirm(true)}
            style={{ width: '100%' }}
          >
            Delete Place
          </button>
        )}
      </div>
    </div>
  );
}
