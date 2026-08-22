/**
 * NotesHome (Phase 8).
 *
 * Main notes screen — list of notes with search, category filter,
 * empty state, and navigation to NoteEditor/NoteDetail.
 */

import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconPlus, IconSearch, IconChevronRight, IconFileText, RoseLilyDecoration } from '../../components/index.ts';
import { useNoteService } from './useNoteService.ts';
import type { NoteView } from '../../services/note/noteService.ts';
import { NOTE_CATEGORY_LABELS, NOTE_CATEGORY_COLORS } from './categoryMeta.ts';

const CATEGORY_LABELS: Record<string, string> = {
  all: 'All',
  ...NOTE_CATEGORY_LABELS,
};

const CATEGORY_COLORS = NOTE_CATEGORY_COLORS;

export function NotesHome() {
  const navigate = useNavigate();
  const { notes, loading, error, counts } = useNoteService();
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState('all');

  const filteredNotes = useMemo(() => {
    let result = notes;
    if (activeCategory !== 'all') {
      result = result.filter((n) => n.category === activeCategory);
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (n) =>
          n.title.toLowerCase().includes(q) ||
          n.content.toLowerCase().includes(q),
      );
    }
    return result;
  }, [notes, activeCategory, searchQuery]);

  const categories = ['all', 'general', 'shared', 'private', 'love-letter', 'gratitude', 'idea', 'reminder'];

  return (
    <div className="th-content-pad">
      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Notes</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Private notes and thoughts
          </p>
        </div>
      </div>

      {/* Search bar */}
      <div className="th-note-search">
        <IconSearch size={16} />
        <input
          type="text"
          placeholder="Search notes..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="th-note-search__input"
        />
      </div>

      {/* Category chips */}
      <div className="th-note-categories">
        {categories.map((cat) => {
          const count = counts[cat] ?? 0;
          if (cat !== 'all' && count === 0) return null;
          return (
            <button
              key={cat}
              className={`th-chip ${activeCategory === cat ? 'th-chip--active' : ''}`}
              onClick={() => setActiveCategory(cat)}
            >
              {CATEGORY_LABELS[cat]}
              {cat !== 'all' && <span className="th-chip__count">{count}</span>}
            </button>
          );
        })}
      </div>

      {/* Error state */}
      {error && (
        <div className="th-error-message">
          <p>{error}</p>
          <button className="th-btn th-btn--secondary" onClick={() => window.location.reload()}>
            Retry
          </button>
        </div>
      )}

      {/* Loading state */}
      {loading && (
        <div className="th-loading-state">
          <div className="th-spinner" />
          <p>Loading notes...</p>
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && filteredNotes.length === 0 && (
        <div className="th-empty-emotional" style={{ marginTop: 'var(--th-space-4)' }}>
          {!searchQuery && <RoseLilyDecoration variant={14} size={100} position="bottom-right" opacity={0.12} animated />}
          <div className="th-empty-emotional__visual th-scale-in">
            <IconFileText size={42} />
          </div>
          <h3 className="th-empty-emotional__title">
            {searchQuery ? 'No notes found' : 'No notes yet'}
          </h3>
          <p className="th-empty-emotional__message">
            {searchQuery
              ? 'Try a different search term.'
              : 'A quiet place for your thoughts, plans, and everything in between.'}
          </p>
          <div className="th-empty-emotional__action">
            {!searchQuery && (
              <button
                className="th-btn th-btn--primary"
                onClick={() => navigate(RoutePath.appNotesAdd)}
              >
                Create Note
              </button>
            )}
          </div>
        </div>
      )}

      {/* Notes list */}
      {!loading && filteredNotes.length > 0 && (
        <div className="th-note-list">
          {filteredNotes.map((note) => (
            <NoteCard key={note.id} note={note} />
          ))}
        </div>
      )}

      {/* FAB */}
      <button
        className="th-fab"
        onClick={() => navigate(RoutePath.appNotesAdd)}
        aria-label="Add note"
      >
        <IconPlus size={24} />
      </button>
    </div>
  );
}

function NoteCard({ note }: { note: NoteView }) {
  const navigate = useNavigate();

  const formatTime = (iso: string): string => {
    const d = new Date(iso);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}d ago`;
    return d.toLocaleDateString();
  };

  const color = CATEGORY_COLORS[note.category] || CATEGORY_COLORS.general;

  return (
    <div
      className="th-note-card th-note-card--enhanced th-stagger-item"
      onClick={() => navigate(`/app/notes/${note.id}`)}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          navigate(`/app/notes/${note.id}`);
        }
      }}
    >
      <div className="th-note-card__header">
        <div className="th-note-card__category" style={{ color }}>
          {note.category.replace('-', ' ')}
        </div>
        <div className="th-note-card__time">{formatTime(note.updatedAt)}</div>
      </div>
      <h3 className="th-note-card__title">{note.title}</h3>
      {note.excerpt && (
        <p className="th-note-card__excerpt">{note.excerpt}</p>
      )}
      <IconChevronRight size={16} className="th-note-card__chevron" />
    </div>
  );
}
