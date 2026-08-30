/**
 * SearchScreen (Stage 14 — Search + Notification Center Visual Productization).
 *
 * Global search with branded field, polished result cards, clear
 * empty/no-result/loading states. Architecture unchanged.
 */

import { useState, useCallback, useEffect, useRef, type ComponentType } from 'react';
import { useNavigate } from 'react-router-dom';
import { Screen } from '../../../components/Screen.tsx';
import { Header } from '../../../components/Header.tsx';
import { IconButton } from '../../../components/IconButton.tsx';
import {
  IconCamera, IconCalendar, IconMapPin, IconBell, IconFileText, IconFile, IconSearch, IconClose,
  RoseLilyDecoration,
  type IconProps,
} from '../../../components/index.ts';
import type { SearchMatch, SearchResults } from '../../../services/search/searchEngine.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  searchHint,
  kindLabel,
  noResultMessage,
  resultCountText,
} from '../../notifications/searchNotificationPresentation.ts';

const KIND_ICONS: Record<string, ComponentType<IconProps>> = {
  memory: IconCamera,
  timeline: IconCalendar,
  place: IconMapPin,
  reminder: IconBell,
  note: IconFileText,
};

/** Resolve navigation route for a search match. */
function resolveRoute(match: SearchMatch): string | null {
  switch (match.kind) {
    case 'memory':
      return `${RoutePath.appMemories}/${match.id}`;
    case 'timeline':
      return `${RoutePath.appTimelineRoot}/${match.id}`;
    case 'place':
      return `${RoutePath.appPlaces}/${match.id}`;
    case 'reminder':
      return `${RoutePath.appReminders}/${match.id}`;
    case 'note':
      return `${RoutePath.appNotes}/${match.id}`;
    default:
      return null;
  }
}

interface SearchScreenProps {
  onSearch?: (query: string) => Promise<SearchResults>;
}

export function SearchScreen({ onSearch }: SearchScreenProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchMatch[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const navigate = useNavigate();
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const performSearch = useCallback(async (q: string) => {
    if (!q.trim()) {
      setResults([]);
      setSearched(false);
      return;
    }
    setLoading(true);
    try {
      const searchResults = onSearch
        ? await onSearch(q)
        : { query: { text: q, tokens: [] }, matches: [] };
      setResults(searchResults.matches);
      setSearched(true);
    } catch {
      setResults([]);
      setSearched(true);
    } finally {
      setLoading(false);
    }
  }, [onSearch]);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      void performSearch(query);
    }, 300);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [query, performSearch]);

  const handleClear = useCallback(() => {
    setQuery('');
    setResults([]);
    setSearched(false);
    inputRef.current?.focus();
  }, []);

  const handleResultPress = useCallback((match: SearchMatch) => {
    const route = resolveRoute(match);
    if (route) navigate(route);
  }, [navigate]);

  return (
    <Screen>
      <Header
        title="Search"
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            ←
          </IconButton>
        }
      />

      <div className="th-content-pad">
        {/* Subtle decorative accent */}
        <RoseLilyDecoration variant={7} size={80} position="top-right" opacity={0.06} />

        {/* Branded search field */}
        <div className="th-search-field">
          <span className="th-search-field__icon">
            <IconSearch size={18} />
          </span>
          <input
            ref={inputRef}
            className="th-search-field__input"
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={searchHint()}
            autoFocus
            autoComplete="off"
          />
          {query.length > 0 && (
            <button
              className="th-search-field__clear"
              onClick={handleClear}
              aria-label="Clear search"
            >
              <IconClose size={14} />
            </button>
          )}
        </div>

        {/* Loading state */}
        {loading && (
          <div className="th-search-loading">
            <div className="th-search-loading__spinner" />
            <span>Searching...</span>
          </div>
        )}

        {/* No results after search */}
        {!loading && searched && results.length === 0 && (
          <div className="th-search-no-results th-game-enter">
            <div className="th-search-no-results__query">{noResultMessage(query)}</div>
            <h3 className="th-search-no-results__title">No matches found</h3>
            <p className="th-search-no-results__desc">
              Try a different search term or check your content.
            </p>
            <button className="th-btn th-btn--secondary" onClick={handleClear} style={{ marginTop: 'var(--th-space-4)' }}>
              Clear search
            </button>
          </div>
        )}

        {/* Empty/default state */}
        {!loading && !searched && (
          <div className="th-search-empty th-game-enter">
            <div className="th-search-empty__icon">
              <IconSearch size={28} />
            </div>
            <h3 className="th-search-empty__title">Search your content</h3>
            <p className="th-search-empty__desc">
              Find memories, notes, places, reminders, and more — all stored privately on your device.
            </p>
          </div>
        )}

        {/* Results */}
        {!loading && results.length > 0 && (
          <>
            <div className="th-search-count">{resultCountText(results.length)}</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
              {results.map((match, i) => {
                const KindIcon = KIND_ICONS[match.kind] ?? IconFile;
                return (
                  <button
                    key={match.id}
                    onClick={() => handleResultPress(match)}
                    className="th-search-result th-game-stagger"
                    style={{ animationDelay: `${i * 40}ms` }}
                  >
                    <div className="th-search-result__icon">
                      <KindIcon size={20} />
                    </div>
                    <div className="th-search-result__body">
                      <div className="th-search-result__title">{match.title}</div>
                      {match.snippet && (
                        <div className="th-search-result__snippet">{match.snippet}</div>
                      )}
                      <span className="th-search-result__kind">{kindLabel(match.kind)}</span>
                    </div>
                  </button>
                );
              })}
            </div>
          </>
        )}
      </div>
    </Screen>
  );
}
