/**
 * PlaceDetail (Stage 9).
 *
 * One chapter from the couple's adventures: a photo hero (or warm pin
 * fallback), serif title, location hierarchy, the "why this place is
 * special" story card, saved-location details, quiet metadata, and
 * deliberate Edit/Delete actions. Delete confirmation uses the
 * centralized Modal bottom-sheet (no second modal framework). When the
 * place is linked to a memory, a connected-memory card deep-links to it.
 */

import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { Place } from '../../data/place/placeTypes.ts';
import { usePlaceService } from './usePlaceService.ts';
import { useMemoryService } from '../memories/useMemoryService.ts';
import { formatLocationLine, formatPlaceDate } from './placePresentation.ts';
import {
  Button,
  Header,
  IconBack,
  IconButton,
  IconCalendar,
  IconChevronRight,
  IconEdit,
  IconHeart,
  IconMapPin,
  IconTrash,
  LoadingState,
  ConfirmDialog,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';

export function PlaceDetail() {
  const navigate = useNavigate();
  const { placeId } = useParams<{ placeId: string }>();
  const service = usePlaceService();
  const memoryService = useMemoryService();
  const toast = useToast();

  const [place, setPlace] = useState<Place | null>(null);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [memoryTitle, setMemoryTitle] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!service || !placeId) return;
    let cancelled = false;
    (async () => {
      try {
        const data = await service.getById(placeId);
        if (cancelled) return;
        setPlace(data);
        if (data?.photoRef) {
          const url = await service.resolvePhotoUrl(data.id);
          if (!cancelled) setPhotoUrl(url);
        }
      } catch {
        if (!cancelled) setPlace(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [service, placeId]);

  /* Resolve the linked memory title (model-supported connection). */
  useEffect(() => {
    if (!memoryService || !place?.memoryId) return;
    let cancelled = false;
    memoryService.getMemory(place.memoryId)
      .then((memory) => { if (!cancelled) setMemoryTitle(memory.title); })
      .catch(() => { /* Linked memory unavailable — card stays hidden. */ });
    return () => { cancelled = true; };
  }, [memoryService, place?.memoryId]);

  const handleDelete = useCallback(async () => {
    if (!service || !placeId) return;
    setDeleting(true);
    try {
      await service.delete(placeId);
      toast.success('Place deleted');
      navigate(RoutePath.appPlaces, { replace: true });
    } catch {
      setDeleting(false);
      setShowDeleteConfirm(false);
      toast.error('Could not delete place');
    }
  }, [service, placeId, navigate, toast]);

  const backButton = (
    <IconButton label="Go back" onClick={() => navigate(-1)}>
      <IconBack />
    </IconButton>
  );

  if (loading || !service) {
    return (
      <div className="th-screen">
        <Header title="Place Details" left={backButton} />
        <LoadingState label="Opening this place…" />
      </div>
    );
  }

  if (!place) {
    return (
      <div className="th-screen">
        <Header title="Place Details" left={backButton} />
        <div className="th-scroll th-content-pad">
          <div className="th-empty-state th-empty-state--enhanced">
            <div className="th-empty-state__visual">
              <IconMapPin size={36} />
            </div>
            <h3 className="th-empty-state__title">Place not found</h3>
            <p className="th-empty-state__desc">This place may have been deleted.</p>
            <Button variant="primary" onClick={() => navigate(RoutePath.appPlaces)}>
              Back to Places
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const locationLine = formatLocationLine(place);
  const hasCoordinates = place.latitude != null && place.longitude != null;

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.08} />
      <Header
        title="Place Details"
        left={backButton}
        right={
          <IconButton label="Delete place" onClick={() => setShowDeleteConfirm(true)}>
            <IconTrash />
          </IconButton>
        }
      />

      <div className="th-scroll th-content-pad">
        {/* Photo hero — or a warm pin fallback when there is no photo */}
        <div className={`th-places-detail-hero ${photoUrl ? '' : 'th-places-detail-hero--fallback'}`}>
          {photoUrl ? (
            <img src={photoUrl} alt={`${place.name} photo`} className="th-places-detail-hero__image" />
          ) : (
            <span className="th-places-detail-hero__placeholder" aria-hidden="true">
              <IconMapPin size={44} />
            </span>
          )}
        </div>

        {/* Identity */}
        <h1 className="th-places-detail__title">{place.name}</h1>
        <div className="th-places-detail__identity">
          {locationLine && (
            <span className="th-places-location th-places-detail__location">
              <IconMapPin size={15} />
              {locationLine}
            </span>
          )}
          {place.category && (
            <span className="th-places-pill">{place.category}</span>
          )}
        </div>

        {/* Saved location */}
        {(place.address || locationLine || hasCoordinates) && (
          <section className="th-places-info-card">
            <span className="th-places-info-card__medallion" aria-hidden="true">
              <IconMapPin size={20} />
            </span>
            <div className="th-places-info-card__body">
              <h2 className="th-places-info-card__label">Saved location</h2>
              {place.address && <p className="th-places-info-card__text">{place.address}</p>}
              {locationLine && <p className="th-places-info-card__text">{locationLine}</p>}
              {hasCoordinates && (
                <p className="th-places-info-card__quiet">
                  {place.latitude?.toFixed(4)}, {place.longitude?.toFixed(4)} — kept on this device
                </p>
              )}
            </div>
          </section>
        )}

        {/* Why this place is special */}
        <section className="th-places-info-card">
          <span className="th-places-info-card__medallion" aria-hidden="true">
            <IconHeart size={20} />
          </span>
          <div className="th-places-info-card__body">
            <h2 className="th-places-info-card__label">Why this place is special</h2>
            {place.notes ? (
              <p className="th-places-info-card__text th-places-info-card__text--story">
                {place.notes}
              </p>
            ) : (
              <p className="th-places-info-card__empty">
                No story written yet — edit this place to add one.
              </p>
            )}
          </div>
        </section>

        {/* Connected memory (when the place is linked to one) */}
        {place.memoryId && memoryTitle && (
          <>
            <h2 className="th-places-detail__section">Connected memory</h2>
            <Link
              to={`${RoutePath.appMemories}/${place.memoryId}`}
              className="th-places-memory-card"
            >
              <span className="th-places-memory-card__medallion" aria-hidden="true">
                <IconHeart size={18} />
              </span>
              <span className="th-places-memory-card__body">
                <span className="th-places-memory-card__title">{memoryTitle}</span>
                <span className="th-places-memory-card__hint">From your memories</span>
              </span>
              <IconChevronRight size={18} className="th-places-memory-card__chevron" />
            </Link>
          </>
        )}

        {/* Actions */}
        <div className="th-places-detail__actions">
          <Button
            variant="primary"
            full
            onClick={() => navigate(`${RoutePath.appPlaces}/${place.id}/edit`)}
          >
            <IconEdit size={18} /> Edit Place
          </Button>
          <button
            className="th-places-detail__delete"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash size={16} /> Delete this place
          </button>
        </div>

        {/* Quiet metadata */}
        <p className="th-places-detail__meta">
          <IconCalendar size={13} />
          Added {formatPlaceDate(place.createdAt)}
          {place.updatedAt !== place.createdAt &&
            ` · Updated ${formatPlaceDate(place.updatedAt)}`}
        </p>
      </div>

      {/* Delete confirmation */}
      <ConfirmDialog
        open={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        label="Delete place"
        title="Delete this place?"
        description={`“${place.name}” will be removed from your places permanently. This action cannot be undone.`}
        actionLabel="Delete place"
        onAction={handleDelete}
        busy={deleting}
        busyLabel="Deleting…"
      />
    </div>
  );
}
