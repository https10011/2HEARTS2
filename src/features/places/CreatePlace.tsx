/**
 * CreatePlace / EditPlace (Stage 9).
 *
 * Branded composer for saving a place from the couple's story. A warm
 * photo dropzone opens the local file picker (photos only); the selection
 * previews inline and can be removed before saving. All bytes stay
 * on-device through PlaceService → MediaStorage — no remote upload, no
 * external URLs, no map SDK. Coordinates remain an honest, optional
 * local reference.
 *
 * Edit mode reuses this screen at `/app/places/:placeId/edit`: fields
 * prefill, the existing photo renders with removal, and a new selection
 * replaces it on save.
 */

import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { AppError } from '../../services/errors/appError.ts';
import { usePlaceService } from './usePlaceService.ts';
import {
  Button,
  Header,
  IconBack,
  IconButton,
  IconCamera,
  IconClose,
  Input,
  LoadingState,
  useToast,
} from '../../components/index.ts';

const CATEGORY_OPTIONS = ['Restaurant', 'Vacation', 'Home', 'Adventure', 'Special', 'Other'] as const;
const PHOTO_ACCEPT = 'image/jpeg,image/png,image/webp';
const NOTES_LIMIT = 500;

interface PendingPhoto {
  mimeType: string;
  data: Uint8Array;
  /** Object URL for local preview (revoked on replace/unmount). */
  previewUrl: string;
}

function readFileBytes(file: File): Promise<Uint8Array> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Could not read the selected file.'));
    reader.onload = () => resolve(new Uint8Array(reader.result as ArrayBuffer));
    reader.readAsArrayBuffer(file);
  });
}

export function CreatePlace() {
  const navigate = useNavigate();
  const { placeId } = useParams<{ placeId: string }>();
  const isEditing = Boolean(placeId);
  const service = usePlaceService();
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
  const [existingPhotoUrl, setExistingPhotoUrl] = useState<string | null>(null);
  const [photoRemoved, setPhotoRemoved] = useState(false);
  const [pendingPhoto, setPendingPhoto] = useState<PendingPhoto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [nameError, setNameError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [loadingPlace, setLoadingPlace] = useState(isEditing);
  const fileInputRef = useRef<HTMLInputElement>(null);

  /* Prefill fields + existing photo in edit mode. */
  useEffect(() => {
    if (!isEditing || !service || !placeId) return;
    let cancelled = false;
    (async () => {
      try {
        const place = await service.getById(placeId);
        if (cancelled) return;
        if (!place) {
          setError('This place may have been deleted.');
          return;
        }
        setName(place.name);
        setAddress(place.address ?? '');
        setCity(place.city ?? '');
        setState(place.state ?? '');
        setCountry(place.country ?? '');
        setLatitude(place.latitude != null ? String(place.latitude) : '');
        setLongitude(place.longitude != null ? String(place.longitude) : '');
        setNotes(place.notes ?? '');
        setCategory(place.category ?? '');
        if (place.photoRef) {
          const url = await service.resolvePhotoUrl(place.id);
          if (!cancelled && url) setExistingPhotoUrl(url);
        }
      } catch {
        if (!cancelled) setError('Could not load place.');
      } finally {
        if (!cancelled) setLoadingPlace(false);
      }
    })();
    return () => { cancelled = true; };
  }, [isEditing, placeId, service]);

  /* Revoke the pending preview URL on replace/unmount. */
  useEffect(() => {
    return () => {
      if (pendingPhoto) URL.revokeObjectURL(pendingPhoto.previewUrl);
    };
  }, [pendingPhoto]);

  const handleFile = async (files: FileList | null) => {
    const file = files?.[0];
    if (!file) return;
    setError(null);
    try {
      const data = await readFileBytes(file);
      setPendingPhoto((prev) => {
        if (prev) URL.revokeObjectURL(prev.previewUrl);
        return { mimeType: file.type, data, previewUrl: URL.createObjectURL(file) };
      });
      setPhotoRemoved(false);
    } catch {
      setError(`Could not read “${file.name}”. Try a different photo.`);
    }
    // Reset so re-selecting the same file still fires onChange.
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const clearPhoto = () => {
    if (pendingPhoto) {
      URL.revokeObjectURL(pendingPhoto.previewUrl);
      setPendingPhoto(null);
    }
    if (existingPhotoUrl) {
      setExistingPhotoUrl(null);
      setPhotoRemoved(true);
    }
  };

  const shownPhotoUrl = pendingPhoto?.previewUrl ?? existingPhotoUrl;

  const handleSave = async () => {
    if (!service) {
      setError('Service not available. Please restart the app.');
      return;
    }
    if (!name.trim()) {
      setNameError('Give this place a name.');
      return;
    }
    setNameError(null);

    const parsedLat = latitude.trim() ? Number(latitude) : null;
    const parsedLng = longitude.trim() ? Number(longitude) : null;
    if ((parsedLat !== null && Number.isNaN(parsedLat)) ||
        (parsedLng !== null && Number.isNaN(parsedLng))) {
      setError('Coordinates must be numbers — or leave them empty.');
      return;
    }

    setSaving(true);
    setError(null);
    try {
      const data = {
        name,
        address: address || null,
        city: city || null,
        state: state || null,
        country: country || null,
        latitude: parsedLat,
        longitude: parsedLng,
        notes: notes || null,
        category: category || null,
      };

      let savedId: string;
      if (isEditing && placeId) {
        const updated = await service.update(placeId, data);
        savedId = updated.id;
        if (photoRemoved) await service.removePhoto(savedId);
        if (pendingPhoto) {
          await service.setPhoto(savedId, pendingPhoto.mimeType, pendingPhoto.data);
        }
        toast.success('Place updated');
      } else {
        const created = await service.create(data);
        savedId = created.id;
        if (pendingPhoto) {
          await service.setPhoto(savedId, pendingPhoto.mimeType, pendingPhoto.data);
        }
        toast.success('Place saved');
      }
      navigate(`${RoutePath.appPlaces}/${savedId}`, { replace: true });
    } catch (err) {
      const message = err instanceof AppError
        ? err.userMessage
        : 'Could not save place.';
      setError(message);
      toast.error(isEditing ? 'Could not update place' : 'Could not save place');
      setSaving(false);
    }
  };

  const heading = isEditing ? 'Edit Place' : 'Add a Place';

  return (
    <div className="th-screen th-screen-warm">
      <Header
        title={heading}
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
      />

      {loadingPlace ? (
        <LoadingState label="Loading place…" />
      ) : (
        <div className="th-scroll th-content-pad">
          <p className="th-places-form-subtitle">
            {isEditing
              ? 'Update this place from your story'
              : 'Save a place that became part of your story.'}
          </p>

          {/* Photo — stays on this device */}
          {shownPhotoUrl ? (
            <div className="th-places-photo-preview">
              <img
                src={shownPhotoUrl}
                alt="Place photo"
                className="th-places-photo-preview__img"
              />
              <button
                type="button"
                className="th-media-thumb__remove th-places-photo-preview__remove"
                onClick={clearPhoto}
                disabled={saving}
                aria-label="Remove photo"
              >
                <IconClose size={14} />
              </button>
            </div>
          ) : (
            <button
              type="button"
              className="th-media-dropzone"
              onClick={() => fileInputRef.current?.click()}
              disabled={saving}
              aria-label="Add a photo"
            >
              <span className="th-media-dropzone__icon" aria-hidden="true">
                <IconCamera size={30} />
              </span>
              <span className="th-media-dropzone__title">Add a photo</span>
              <span className="th-media-dropzone__hint">
                Add a photo to remember this place.
              </span>
            </button>
          )}
          <input
            ref={fileInputRef}
            type="file"
            accept={PHOTO_ACCEPT}
            hidden
            onChange={(e) => { void handleFile(e.target.files); }}
          />

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="place-name">
              Place name
            </label>
            <Input
              id="place-name"
              placeholder="e.g. That Little Café"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (nameError) setNameError(null);
              }}
              maxLength={200}
              disabled={saving}
              aria-invalid={!!nameError}
            />
            {nameError && (
              <p className="th-form-error" role="alert">{nameError}</p>
            )}
          </div>

          <div className="th-form-group">
            <span className="th-form-label" id="place-category-label">
              What kind of place is this?
            </span>
            <div
              className="th-places-chips"
              role="group"
              aria-labelledby="place-category-label"
            >
              {CATEGORY_OPTIONS.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  className={`th-option-chip ${category === cat ? 'th-option-chip--active' : ''}`}
                  aria-pressed={category === cat}
                  onClick={() => setCategory(category === cat ? '' : cat)}
                  disabled={saving}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="place-address">
              Address <span className="th-form-optional">(optional)</span>
            </label>
            <Input
              id="place-address"
              placeholder="Street address"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              disabled={saving}
            />
          </div>

          <div className="th-places-field-row">
            <div className="th-form-group">
              <label className="th-form-label" htmlFor="place-city">City</label>
              <Input
                id="place-city"
                placeholder="City"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                disabled={saving}
              />
            </div>
            <div className="th-form-group">
              <label className="th-form-label" htmlFor="place-state">State</label>
              <Input
                id="place-state"
                placeholder="State"
                value={state}
                onChange={(e) => setState(e.target.value)}
                disabled={saving}
              />
            </div>
          </div>

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="place-country">Country</label>
            <Input
              id="place-country"
              placeholder="Country"
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              disabled={saving}
            />
          </div>

          <details className="th-places-coords">
            <summary className="th-places-coords__summary">
              Pinpoint it <span className="th-form-optional">(optional)</span>
            </summary>
            <p className="th-places-coords__hint">
              Exact coordinates, kept only on this device.
            </p>
            <div className="th-places-field-row">
              <div className="th-form-group">
                <label className="th-form-label" htmlFor="place-lat">Latitude</label>
                <Input
                  id="place-lat"
                  type="number"
                  step="any"
                  placeholder="e.g. 14.5995"
                  value={latitude}
                  onChange={(e) => setLatitude(e.target.value)}
                  disabled={saving}
                />
              </div>
              <div className="th-form-group">
                <label className="th-form-label" htmlFor="place-lng">Longitude</label>
                <Input
                  id="place-lng"
                  type="number"
                  step="any"
                  placeholder="e.g. 120.9842"
                  value={longitude}
                  onChange={(e) => setLongitude(e.target.value)}
                  disabled={saving}
                />
              </div>
            </div>
          </details>

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="place-notes">
              Why is this place special? <span className="th-form-optional">(optional)</span>
            </label>
            <Input
              id="place-notes"
              multiline
              placeholder="Write a little about this place…"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              maxLength={NOTES_LIMIT}
              disabled={saving}
              style={{ minHeight: '110px', resize: 'vertical' }}
            />
            <p className="th-places-char-count" aria-live="polite">
              {notes.length}/{NOTES_LIMIT}
            </p>
          </div>

          {error && (
            <p className="th-form-error th-form-error--global" role="alert">
              {error}
            </p>
          )}

          <div className="th-onboarding-actions" style={{ marginTop: 'var(--th-space-4)' }}>
            <Button
              variant="primary"
              full
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Save Place'}
            </Button>
            <Button
              variant="ghost"
              full
              onClick={() => navigate(-1)}
              disabled={saving}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
