/**
 * NoteDetail (Phase 8).
 *
 * Individual note view with full content, edit, and delete actions.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useNoteService } from './useNoteService.ts';
import type { NoteView } from '../../services/note/noteService.ts';
import { IconTrash, IconEdit } from '../../components/index.ts';

const CATEGORY_LABELS: Record<string, string> = {
  general: 'General',
  shared: 'Shared',
  private: 'Private',
  'love-letter': 'Love Letter',
  gratitude: 'Gratitude',
  idea: 'Idea',
  reminder: 'Reminder',
};

const CATEGORY_COLORS: Record<string, string> = {
  general: 'var(--th-color-burgundy)',
  shared: '#8B5E3C',
  private: '#4A5568',
  'love-letter': '#C53030',
  gratitude: '#2F855A',
  idea: '#6B46C1',
  reminder: '#D69E2E',
};

export function NoteDetail() {
  const navigate = useNavigate();
  const { noteId } = useParams<{ noteId: string }>();
  const { getNote, deleteNote } = useNoteService();

  const [note, setNote] = useState<NoteView | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!noteId) {
      navigate(RoutePath.appNotes, { replace: true });
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const n = await getNote(noteId);
        if (!cancelled) setNote(n);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Note not found.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [noteId, getNote, navigate]);

  const handleDelete = useCallback(async () => {
    if (!noteId) return;
    setDeleting(true);
    try {
      await deleteNote(noteId);
      navigate(RoutePath.appNotes, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete note.');
      setDeleting(false);
      setShowDeleteConfirm(false);
    }
  }, [noteId, deleteNote, navigate]);

  const formatDate = (iso: string): string => {
    const d = new Date(iso);
    return d.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading-state">
          <div className="th-spinner" />
          <p>Loading note...</p>
        </div>
      </div>
    );
  }

  if (error || !note) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state">
          <div className="th-empty-state__icon">📄</div>
          <h3 className="th-empty-state__title">Note not found</h3>
          <p className="th-empty-state__desc">{error ?? 'This note may have been deleted.'}</p>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appNotes)}
          >
            Back to Notes
          </button>
        </div>
      </div>
    );
  }

  const color = CATEGORY_COLORS[note.category] || CATEGORY_COLORS.general;

  return (
    <div className="th-content-pad">
      {/* Header with actions */}
      <div className="th-note-detail-header">
        <button
          className="th-btn th-btn--secondary th-btn--sm"
          onClick={() => navigate(RoutePath.appNotes)}
        >
          ← Back
        </button>
        <div className="th-note-detail-actions">
          <button
            className="th-icon-btn"
            onClick={() => navigate(`/app/notes/${note.id}/edit`)}
            aria-label="Edit note"
          >
            <IconEdit size={18} />
          </button>
          <button
            className="th-icon-btn th-icon-btn--danger"
            onClick={() => setShowDeleteConfirm(true)}
            aria-label="Delete note"
          >
            <IconTrash size={18} />
          </button>
        </div>
      </div>

      {/* Note content */}
      <div className="th-note-detail">
        <div className="th-note-detail__category" style={{ color }}>
          {CATEGORY_LABELS[note.category]}
        </div>
        <h1 className="th-note-detail__title">{note.title}</h1>
        <div className="th-note-detail__meta">
          <span>Created: {formatDate(note.createdAt)}</span>
          {note.updatedAt !== note.createdAt && (
            <span>Updated: {formatDate(note.updatedAt)}</span>
          )}
        </div>
        <div className="th-note-detail__content">
          {note.content ? (
            note.content.split('\n').map((line, i) => (
              <p key={i}>{line || '\u00A0'}</p>
            ))
          ) : (
            <p className="th-note-detail__empty">No content</p>
          )}
        </div>
      </div>

      {/* Delete confirmation modal */}
      {showDeleteConfirm && (
        <div className="th-modal-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="th-modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="th-modal__title">Delete Note</h2>
            <p className="th-modal__text">
              Are you sure you want to delete &ldquo;{note.title}&rdquo;? This cannot be undone.
            </p>
            <div className="th-modal__actions">
              <button
                className="th-btn th-btn--secondary"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                className="th-btn th-btn--danger"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
