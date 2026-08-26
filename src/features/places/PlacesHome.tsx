/**
 * PlacesHome (Stage 9).
 *
 * "Places we've shared" — the couple's saved spots presented as part of
 * their story, not a location database. The most recently saved place
 * leads as a featured card; the rest fill a photo grid. Category chips
 * and search filter in memory (no schema change). Photos resolve through
 * PlaceService → MediaStorage `data:` URLs; places without a photo (or
 * with missing bytes) render a warm pin fallback.
 */

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { Place } from '../../data/place/placeTypes.ts';
import { usePlaceService } from './usePlaceService.ts';
import {
  byNewestFirst,
  collectCategories,
  filterPlaces,
  formatAddedAgo,
  formatLocationLine,
} from './placePresentation.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconCalendar,
  IconMapPin,
  IconPlus,
  IconSearch,
  LoadingState,
  RoseLilyDecoration,
} from '../../components/index.ts';

export function PlacesHome() {
  const navigate = useNavigate();
  const service = usePlaceService();
  const [places, setPlaces] = useState<Place[]>([]);
  const [photoUrls, setPhotoUrls] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState<string | null>(null);

  const loadPlaces = useCallback(async () => {
    if (!service) return;
    setLoading(true);
    setError(null);
    try {
      const result = await service.list();
      setPlaces(result);
      // Resolve card photos through the existing local media architecture —
      // failures degrade to the warm pin fallback.
      const resolved: Record<string, string> = {};
      await Promise.all(
        result.map(async (place) => {
          if (!place.photoRef) return;
          const url = await service.resolvePhotoUrl(place.id);
          if (url) resolved[place.id] = url;
        }),
      );
      setPhotoUrls(resolved);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load places.');
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    loadPlaces();
  }, [loadPlaces]);

  const categories = useMemo(() => collectCategories(places), [places]);
  const visible = useMemo(
    () => byNewestFirst(filterPlaces(places, { category, query })),
    [places, category, query],
  );

  if (loading || !service) {
    return <LoadingState label="Gathering your places…" />;
  }

  const header = (
    <header className="th-places-header">
      <IconButton label="Go back" onClick={() => navigate(-1)}>
        <IconBack />
      </IconButton>
      <div className="th-places-header__copy">
        <h1 className="th-places-title">Our Places</h1>
        <p className="th-places-subtitle">Places that mean something to us.</p>
      </div>
      <IconButton label="Add place" onClick={() => navigate(RoutePath.appPlacesAdd)}>
        <IconPlus />
      </IconButton>
    </header>
  );

  if (error) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-places-error" role="alert">
          <p>We couldn&apos;t gather your places just now.</p>
          <Button variant="secondary" onClick={loadPlaces}>Try again</Button>
        </div>
      </div>
    );
  }

  // Empty state — an invitation, not an error.
  if (places.length === 0) {
    return (
      <div className="th-content-pad th-screen-warm">
        <RoseLilyDecoration variant={9} size={110} position="bottom-left" opacity={0.12} />
        {header}
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconMapPin size={42} />
          </div>
          <h3 className="th-empty-emotional__title">No places yet</h3>
          <p className="th-empty-emotional__message">
            Save the little places that became part of your story — the café
            from your first date, the beach you always go back to.
          </p>
          <div className="th-empty-emotional__action">
            <Button variant="primary" onClick={() => navigate(RoutePath.appPlacesAdd)}>
              <IconPlus size={18} /> Add your first place
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const hero = visible[0] ?? null;
  const rest = visible.slice(1);

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={14} size={120} position="top-right" opacity={0.12} />
      {header}

      {/* Story banner */}
      <section className="th-places-banner th-stagger-item">
        <div className="th-places-banner__copy">
          <h2 className="th-places-banner__title">Places we&apos;ve shared</h2>
          <p className="th-places-banner__text">
            Keep the little places that became part of your story.
          </p>
        </div>
        <span className="th-places-banner__medallion" aria-hidden="true">
          <IconMapPin size={26} />
        </span>
      </section>

      {/* Search */}
      <div className="th-notes-search th-stagger-item">
        <IconSearch size={16} />
        <input
          type="text"
          placeholder="Search places..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="th-notes-search__input"
          aria-label="Search places"
        />
      </div>

      {/* Category chips — built from the couple's actual categories */}
      {categories.length > 0 && (
        <div className="th-places-chips" role="group" aria-label="Filter by category">
          <button
            type="button"
            className={`th-option-chip ${category === null ? 'th-option-chip--active' : ''}`}
            aria-pressed={category === null}
            onClick={() => setCategory(null)}
          >
            All
          </button>
          {categories.map((cat) => (
            <button
              key={cat}
              type="button"
              className={`th-option-chip ${category === cat ? 'th-option-chip--active' : ''}`}
              aria-pressed={category === cat}
              onClick={() => setCategory(category === cat ? null : cat)}
            >
              {cat}
            </button>
          ))}
        </div>
      )}

      {visible.length === 0 ? (
        <p className="th-places-filter-empty">No places match that right now.</p>
      ) : (
        <>
          {/* Featured place — the most recently saved */}
          {hero && (
            <Link
              to={`${RoutePath.appPlaces}/${hero.id}`}
              className="th-places-hero th-stagger-item"
            >
              <span className="th-places-hero__frame">
                {photoUrls[hero.id] ? (
                  <img
                    src={photoUrls[hero.id]}
                    alt={`${hero.name} photo`}
                    className="th-places-hero__image"
                    loading="lazy"
                  />
                ) : (
                  <span className="th-places-hero__placeholder" aria-hidden="true">
                    <IconMapPin size={30} />
                  </span>
                )}
              </span>
              <span className="th-places-hero__info">
                <span className="th-places-hero__name">{hero.name}</span>
                {formatLocationLine(hero) && (
                  <span className="th-places-location">
                    <IconMapPin size={13} />
                    {formatLocationLine(hero)}
                  </span>
                )}
                {hero.category && (
                  <span className="th-places-pill">{hero.category}</span>
                )}
                <span className="th-places-hero__added">
                  <IconCalendar size={13} />
                  {formatAddedAgo(hero.createdAt)}
                </span>
              </span>
            </Link>
          )}

          {rest.length > 0 && <h2 className="th-places-section">Our places</h2>}
          <div className="th-places-grid">
            {rest.map((place) => (
              <Link
                key={place.id}
                to={`${RoutePath.appPlaces}/${place.id}`}
                className="th-places-card th-stagger-item"
              >
                <span className="th-places-card__frame">
                  {photoUrls[place.id] ? (
                    <img
                      src={photoUrls[place.id]}
                      alt={`${place.name} photo`}
                      className="th-places-card__image"
                      loading="lazy"
                    />
                  ) : (
                    <span className="th-places-card__placeholder" aria-hidden="true">
                      <IconMapPin size={26} />
                    </span>
                  )}
                </span>
                <span className="th-places-card__meta">
                  <span className="th-places-card__name">{place.name}</span>
                  {formatLocationLine(place) && (
                    <span className="th-places-location">
                      <IconMapPin size={12} />
                      {formatLocationLine(place)}
                    </span>
                  )}
                  <span className="th-places-card__footer">
                    {place.category && (
                      <span className="th-places-pill">{place.category}</span>
                    )}
                    <span className="th-places-card__added">
                      {formatAddedAgo(place.createdAt)}
                    </span>
                  </span>
                </span>
              </Link>
            ))}
          </div>
        </>
      )}

      {/* Closing invitation */}
      <section className="th-places-cta th-stagger-item">
        <div className="th-places-cta__copy">
          <h2 className="th-places-cta__title">Have a special place in mind?</h2>
          <p className="th-places-cta__text">Save it here, just for the two of you.</p>
        </div>
        <Button variant="primary" onClick={() => navigate(RoutePath.appPlacesAdd)}>
          Add Place
        </Button>
      </section>

      <p className="th-places-footer">Every place holds a piece of your story.</p>
    </div>
  );
}
