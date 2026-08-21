/**
 * PlacesHome (Phase 14).
 *
 * Displays the couple's saved places as cards. Supports search
 * and category filtering. Uses real persisted data via PlaceService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';
import { PlaceService } from '../../services/place/placeService.ts';
import { IconPlus, IconChevronRight, IconMapPin } from '../../components/index.ts';
import type { Place } from '../../data/place/placeTypes.ts';

const CATEGORY_OPTIONS = ['All', 'Restaurant', 'Vacation', 'Home', 'Adventure', 'Special', 'Other'] as const;

let _placeService: PlaceService | null = null;
async function getPlaceService(): Promise<PlaceService> {
  if (!_placeService) {
    const repo = new PlaceRepository(await getDatabase());
    _placeService = new PlaceService(repo);
  }
  return _placeService;
}

export function PlacesHome() {
  const navigate = useNavigate();
  const [places, setPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');

  const loadPlaces = useCallback(async () => {
    try {
      setLoading(true);
      const service = await getPlaceService();
      const data = searchQuery.trim()
        ? await service.search(searchQuery)
        : activeCategory === 'All'
          ? await service.list()
          : await service.listByCategory(activeCategory);
      setPlaces(data);
    } catch {
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  }, [searchQuery, activeCategory]);

  useEffect(() => {
    loadPlaces();
  }, [loadPlaces]);

  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Our Places</h1>
        <button
          className="th-btn th-btn--primary th-btn--sm"
          onClick={() => navigate(RoutePath.appPlacesAdd)}
          style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}
        >
          <IconPlus size={16} />
          Add
        </button>
      </div>

      {/* Search */}
      <input
        type="text"
        className="th-input"
        placeholder="Search places..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        style={{ marginBottom: 'var(--th-space-3)' }}
      />

      {/* Category filter */}
      <div style={{ display: 'flex', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-4)', overflowX: 'auto', flexWrap: 'nowrap' }}>
        {CATEGORY_OPTIONS.map((cat) => (
          <button
            key={cat}
            className={`th-btn th-btn--sm ${activeCategory === cat ? 'th-btn--primary' : 'th-btn--outline'}`}
            onClick={() => setActiveCategory(cat)}
            style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Loading */}
      {loading && (
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading places...</p>
        </div>
      )}

      {/* Empty state */}
      {!loading && places.length === 0 && (
        <div className="th-empty-state">
          <div className="th-empty-state__icon" style={{ color: 'var(--th-color-rose-muted)' }}>
            <IconMapPin size={44} />
          </div>
          <h3 className="th-empty-state__title">
            {searchQuery ? 'No matching places' : 'No places yet'}
          </h3>
          <p className="th-empty-state__message">
            {searchQuery
              ? 'Try a different search term.'
              : 'Add your favorite places to remember the spots that matter to you both.'}
          </p>
          {!searchQuery && (
            <button
              className="th-btn th-btn--primary"
              onClick={() => navigate(RoutePath.appPlacesAdd)}
            >
              Add Your First Place
            </button>
          )}
        </div>
      )}

      {/* Place list */}
      {!loading && places.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
          {places.map((place) => (
            <button
              key={place.id}
              className="th-card th-card--clickable"
              onClick={() => navigate(RoutePath.appPlacesDetail.replace(':placeId', place.id))}
              style={{ textAlign: 'left', width: '100%', display: 'flex', alignItems: 'center', gap: 'var(--th-space-3)', padding: 'var(--th-space-4)' }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-1)' }}>
                  <span style={{ color: 'var(--th-color-burgundy)', display: 'inline-flex' }}>
                    <IconMapPin size={18} />
                  </span>
                  <h3 style={{ fontWeight: 600, fontSize: 'var(--th-font-size-md)' }}>{place.name}</h3>
                </div>
                {(place.city || place.state || place.country) && (
                  <p style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', margin: 0 }}>
                    {[place.city, place.state, place.country].filter(Boolean).join(', ')}
                  </p>
                )}
                {place.category && (
                  <span
                    className="th-badge"
                    style={{ marginTop: 'var(--th-space-1)', display: 'inline-block' }}
                  >
                    {place.category}
                  </span>
                )}
              </div>
              <IconChevronRight size={18} className="th-more-item__chevron" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
