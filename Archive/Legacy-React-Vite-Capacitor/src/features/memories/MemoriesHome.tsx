/**
 * MemoriesHome (Stage 5).
 *
 * Photo-first gallery: most-recent memory leads as a hero card, the rest
 * fill a 2-column photo grid. Year chips filter in memory (no new schema).
 * Photos resolve through MemoryService → MediaStorage `data:` URLs; a warm
 * blush fallback covers memories whose media is missing or video-only.
 */

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { MemoryWithMedia } from '../../services/memory/memoryService.ts';
import { useMemoryService } from './useMemoryService.ts';
import { collectYears, filterByYear, formatDateKey, byNewestFirst } from './memoryFilters.ts';
import {
  Button,
  IconPlus,
  IconImage,
  IconVideo,
  IconCalendar,
  LoadingState,
  RoseLilyDecoration,
} from '../../components/index.ts';

export function MemoriesHome() {
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const [memories, setMemories] = useState<MemoryWithMedia[]>([]);
  const [thumbnails, setThumbnails] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [yearFilter, setYearFilter] = useState<number | null>(null);

  const loadMemories = useCallback(async () => {
    if (!memoryService) return;
    setLoading(true);
    setError(null);
    try {
      const result = await memoryService.listMemories();
      setMemories(result);
      // Resolve grid thumbnails (first photo per memory) through the
      // existing local media architecture — failures degrade to fallback.
      const resolved: Record<string, string> = {};
      await Promise.all(
        result.map(async (memory) => {
          const photo = memory.mediaReferences.find((ref) => ref.kind === 'photo');
          if (!photo) return;
          try {
            resolved[memory.id] = await memoryService.resolveMediaUrl(photo.id);
          } catch {
            // Missing/broken media — the card renders its warm fallback.
          }
        }),
      );
      setThumbnails(resolved);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load memories.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [memoryService]);

  useEffect(() => {
    loadMemories();
  }, [loadMemories]);

  const years = useMemo(() => collectYears(memories), [memories]);
  const visible = useMemo(
    () => byNewestFirst(filterByYear(memories, yearFilter)),
    [memories, yearFilter],
  );

  if (loading || !memoryService) {
    return <LoadingState label="Loading memories…" />;
  }

  if (error) {
    return (
      <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
        <p style={{ color: 'var(--th-color-error)', marginBottom: 'var(--th-space-4)' }}>{error}</p>
        <Button variant="secondary" onClick={loadMemories}>Try again</Button>
      </div>
    );
  }

  const header = (
    <header className="th-mem-header">
      <RoseLilyDecoration variant={3} size={110} position="top-right" opacity={0.14} />
      <div className="th-mem-header__copy">
        <h1 className="th-mem-title">Memories</h1>
        <p className="th-mem-subtitle">Little moments worth keeping.</p>
      </div>
      <Button
        variant="ghost"
        aria-label="Add memory"
        onClick={() => navigate(RoutePath.appMemoriesAdd)}
        style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
      >
        <IconPlus size={24} />
      </Button>
    </header>
  );

  // Empty state — an invitation, not an error.
  if (memories.length === 0) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconImage size={42} />
          </div>
          <h3 className="th-empty-emotional__title">No memories yet</h3>
          <p className="th-empty-emotional__message">
            This is a place waiting for your favorite moments together
          </p>
          <div className="th-empty-emotional__action">
            <Button variant="primary" onClick={() => navigate(RoutePath.appMemoriesAdd)}>
              <IconPlus size={18} /> Add your first memory
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
      {header}

      {years.length > 1 && (
        <div className="th-mem-chips" role="listbox" aria-label="Filter by year">
          <button
            type="button"
            role="option"
            aria-selected={yearFilter === null}
            className={`th-mem-chip ${yearFilter === null ? 'th-mem-chip--active' : ''}`}
            onClick={() => setYearFilter(null)}
          >
            All
          </button>
          {years.map((year) => (
            <button
              key={year}
              type="button"
              role="option"
              aria-selected={year === yearFilter}
              className={`th-mem-chip ${year === yearFilter ? 'th-mem-chip--active' : ''}`}
              onClick={() => setYearFilter(year === yearFilter ? null : year)}
            >
              {year}
            </button>
          ))}
        </div>
      )}

      {hero && (
        <Link
          to={`${RoutePath.appMemories}/${hero.id}`}
          className={`th-mem-hero th-stagger-item ${thumbnails[hero.id] ? '' : 'th-mem-hero--fallback'}`}
        >
          {thumbnails[hero.id] ? (
            <img
              src={thumbnails[hero.id]}
              alt={`${hero.title} photo`}
              className="th-mem-hero__image"
              loading="lazy"
            />
          ) : (
            <div className="th-mem-hero__placeholder" aria-hidden="true">
              {hero.mediaReferences.some((ref) => ref.kind === 'video')
                ? <IconVideo size={42} />
                : <IconImage size={42} />}
            </div>
          )}
          <div className="th-mem-hero__overlay">
            <h2 className="th-mem-hero__title">{hero.title}</h2>
            {hero.caption && <p className="th-mem-hero__caption">{hero.caption}</p>}
            {hero.memoryDate && (
              <p className="th-mem-date">
                <IconCalendar size={14} />
                {formatDateKey(hero.memoryDate)}
              </p>
            )}
          </div>
        </Link>
      )}

      {rest.length > 0 && <h2 className="th-mem-section">All Memories</h2>}
      <div className="th-photo-grid">
        {rest.map((memory) => (
          <Link
            key={memory.id}
            to={`${RoutePath.appMemories}/${memory.id}`}
            className="th-photo-card th-stagger-item"
          >
            <div className="th-photo-card__frame">
              {thumbnails[memory.id] ? (
                <img
                  src={thumbnails[memory.id]}
                  alt={`${memory.title} photo`}
                  className="th-photo-card__image"
                  loading="lazy"
                />
              ) : (
                <div className="th-photo-card__placeholder" aria-hidden="true">
                  {memory.mediaReferences.some((ref) => ref.kind === 'video')
                    ? <IconVideo size={26} />
                    : <IconImage size={26} />}
                </div>
              )}
            </div>
            <div className="th-photo-card__meta">
              <div className="th-photo-card__title">{memory.title}</div>
              {memory.memoryDate && (
                <div className="th-mem-date">
                  <IconCalendar size={13} />
                  {formatDateKey(memory.memoryDate)}
                </div>
              )}
            </div>
          </Link>
        ))}
      </div>

      {/* Duplicate-year empty result (rare: filtered year deleted) */}
      {visible.length === 0 && (
        <p className="th-mem-empty-filter">No memories in {yearFilter}.</p>
      )}

      {/* FAB */}
      <button
        className="th-fab"
        onClick={() => navigate(RoutePath.appMemoriesAdd)}
        aria-label="Add memory"
      >
        <IconPlus size={24} />
      </button>
    </div>
  );
}
