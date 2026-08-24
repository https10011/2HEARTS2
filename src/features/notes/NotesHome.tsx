/**
 * NotesHome (Phase 8, productized in Stage 6).
 *
 * Private notes home — branded serif header, warm search, category
 * chips, and paper-inspired note cards with circular category icon
 * badges. Love letters get a blush "keepsake" treatment. Local-first:
 * all data flows through useNoteService → NoteService → SQLite.
 */

import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconPlus,
  IconSearch,
  IconFileText,
  IconButton,
  RoseLilyDecoration,
} from '../../components/index.ts';
import { useNoteService } from './useNoteService.ts';
import type { NoteView } from '../../services/note/noteService.ts';
import {
  NOTE_CATEGORY_LABELS,
} from './categoryMeta.ts';
import { NOTE_CATEGORY_ICONS } from './categoryIcons.tsx';
import { formatRelativeTime } from './noteTime.ts';

const CATEGORY_LABELS: Record<string, string> = {
  all: 'All',
  ...NOTE_CATEGORY_LABELS,
};

export function NotesHome() {
  const navigate = useNavigate();
  const { notes, loading, error, counts, loadNotes } = useNoteService();
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
      {/* Branded header */}
      <div className="th-notes-header">
        <RoseLilyDecoration variant={14} size={120} position="top-right" opacity={0.14} />
        <div className="th-notes-header__copy">
          <h1 className="th-notes-title">Notes</h1>
          <p className="th-notes-subtitle">Keep the little thoughts that matter.</p>
        </div>
        <IconButton label="Add note" onClick={() => navigate(RoutePath.appNotesAdd)}>
          <IconPlus />
        </IconButton>
      </div>

      {/* Search bar */}
      <div className="th-notes-search">
        <IconSearch size={16} />
        <input
          type="text"
          placeholder="Search notes..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="th-notes-search__input"
          aria-label="Search notes"
        />
      </div>

      {/* Category chips */}
      <div className="th-notes-chips" role="listbox" aria-label="Filter by category">
        {categories.map((cat) => {
          const count = counts[cat] ?? 0;
          if (cat !== 'all' && count === 0) return null;
          return (
            <button
              key={cat}
              role="option"
              aria-selected={activeCategory === cat}
              className={`th-notes-chip ${activeCategory === cat ? 'th-notes-chip--active' : ''}`}
              onClick={() => setActiveCategory(cat)}
            >
              {CATEGORY_LABELS[cat]}
              {cat !== 'all' && <span className="th-notes-chip__count">{count}</span>}
            </button>
          );
        })}
      </div>

      {/* Error state */}
      {error && (
        <div className="th-error-message">
          <p>{error}</p>
          <button className="th-btn th-btn--secondary" onClick={() => void loadNotes()}>
            Try again
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
              : 'A private place for the thoughts you keep for each other.'}
          </p>
          <div className="th-empty-emotional__action">
            {!searchQuery && (
              <button
                className="th-btn th-btn--primary"
                onClick={() => navigate(RoutePath.appNotesAdd)}
              >
                Write your first note
              </button>
            )}
          </div>
        </div>
      )}

      {/* Notes list */}
      {!loading && filteredNotes.length > 0 && (
        <>
          <h2 className="th-notes-section">All Notes</h2>
          <div className="th-notes-list">
            {filteredNotes.map((note) => (
              <NoteCard key={note.id} note={note} />
            ))}
          </div>
        </>
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
  const CategoryIcon = NOTE_CATEGORY_ICONS[note.category] ?? IconFileText;
  const isLoveLetter = note.category === 'love-letter';

  return (
    <div
      className={`th-note-card th-stagger-item ${isLoveLetter ? 'th-note-card--keepsake' : ''}`}
      onClick={() => navigate(`/app/notes/${note.id}`)}
      role="button"
      tabIndex={0}
      aria-label={`Note: ${note.title}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          navigate(`/app/notes/${note.id}`);
        }
      }}
    >
      <span className="th-note-card__badge" aria-hidden="true">
        <CategoryIcon size={20} />
      </span>
      <div className="th-note-card__body">
        <div className="th-note-card__top">
          <h3 className="th-note-card__title">{note.title}</h3>
          <span className="th-note-card__time">{formatRelativeTime(note.updatedAt)}</span>
        </div>
        {note.excerpt && (
          <p className="th-note-card__excerpt">{note.excerpt}</p>
        )}
      </div>
    </div>
  );
}
