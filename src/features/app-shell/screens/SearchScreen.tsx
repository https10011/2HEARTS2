/**
 * SearchScreen (Phase 18).
 *
 * Global search across all features. Uses the existing Phase 3 search
 * engine with registered feature providers.
 */

import { useState, useCallback, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Screen } from '../../../components/Screen.tsx';
import { Header } from '../../../components/Header.tsx';
import { IconButton } from '../../../components/IconButton.tsx';
import { Input } from '../../../components/Input.tsx';
import { EmptyState } from '../../../components/EmptyState.tsx';
import type { SearchMatch, SearchResults } from '../../../services/search/searchEngine.ts';
import { RoutePath } from '../../../navigation/routes.ts';

const KIND_LABELS: Record<string, string> = {
  memory: 'Memory',
  timeline: 'Timeline',
  place: 'Place',
  reminder: 'Reminder',
  note: 'Note',
};

const KIND_ICONS: Record<string, string> = {
  memory: '📸',
  timeline: '📅',
  place: '📍',
  reminder: '⏰',
  note: '📝',
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

      <div className="search-container" style={{ padding: '16px' }}>
        <div style={{ position: 'relative' }}>
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search memories, notes, places..."
            style={{ paddingLeft: '36px' } as React.CSSProperties}
          />
          <span
            style={{
              position: 'absolute',
              left: '12px',
              top: '50%',
              transform: 'translateY(-50%)',
              opacity: 0.5,
              fontSize: '16px',
            }}
          >
            🔍
          </span>
        </div>

        {loading && (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--th-color-text-secondary)' }}>
            Searching...
          </div>
        )}

        {!loading && searched && results.length === 0 && (
          <EmptyState
            title="No results found"
            description={`No matches for "${query}"`}
          />
        )}

        {!loading && !searched && (
          <EmptyState
            title="Search your content"
            description="Find memories, notes, places, reminders, and more"
          />
        )}

        {!loading && results.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {results.map((match) => (
              <button
                key={match.id}
                onClick={() => handleResultPress(match)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '12px',
                  borderRadius: '12px',
                  border: '1px solid var(--th-color-border)',
                  background: 'var(--th-color-surface)',
                  cursor: 'pointer',
                  textAlign: 'left',
                  width: '100%',
                }}
              >
                <span style={{ fontSize: '24px' }}>
                  {KIND_ICONS[match.kind] ?? '📄'}
                </span>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{
                    fontWeight: 600,
                    fontSize: '14px',
                    color: 'var(--th-color-text-primary)',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}>
                    {match.title}
                  </div>
                  {match.snippet && (
                    <div style={{
                      fontSize: '12px',
                      color: 'var(--th-color-text-secondary)',
                      marginTop: '2px',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}>
                      {match.snippet}
                    </div>
                  )}
                  <span style={{
                    display: 'inline-block',
                    fontSize: '10px',
                    fontWeight: 500,
                    color: 'var(--th-color-primary)',
                    marginTop: '4px',
                    padding: '2px 6px',
                    borderRadius: '4px',
                    background: 'var(--th-color-primary-subtle)',
                  }}>
                    {KIND_LABELS[match.kind] ?? match.kind}
                  </span>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </Screen>
  );
}
