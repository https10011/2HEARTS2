/**
 * NoteDetail (Phase 8, productized in Stage 6).
 *
 * Individual note view — serif title with category icon accent,
 * "last edited" line, comfortable reading typography, and a quiet
 * action row (Edit / Delete). Delete confirms through the existing
 * centralized Modal + toast layer.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useNoteService } from './useNoteService.ts';
import type { NoteView } from '../../services/note/noteService.ts';
import {
  Button,
  Header,
  IconButton,
  IconBack,
  IconTrash,
  IconEdit,
  IconFileText,
  LoadingState,
  Modal,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';
import {
  NOTE_CATEGORY_LABELS as CATEGORY_LABELS,
} from './categoryMeta.ts';
import { NOTE_CATEGORY_ICONS } from './categoryIcons.tsx';
import { formatLastEdited } from './noteTime.ts';

export function NoteDetail() {
  const navigate = useNavigate();
  const { noteId } = useParams<{ noteId: string }>();
  const { getNote, deleteNote } = useNoteService();
  const toast = useToast();

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
      toast.success('Note deleted');
      navigate(RoutePath.appNotes, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete note.');
      setDeleting(false);
      setShowDeleteConfirm(false);
      toast.error('Could not delete note');
    }
  }, [noteId, deleteNote, navigate, toast]);

  if (loading) {
    return (
      <div className="th-screen">
        <Header
          title="Note"
          left={
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          }
        />
        <LoadingState label="Loading note…" />
      </div>
    );
  }

  if (error || !note) {
    return (
      <div className="th-screen">
        <Header
          title="Note"
          left={
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          }
        />
        <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
          <div className="th-empty-state th-empty-state--enhanced">
            <div className="th-empty-state__visual">
              <IconFileText size={36} />
            </div>
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
      </div>
    );
  }

  const CategoryIcon = NOTE_CATEGORY_ICONS[note.category] ?? IconFileText;

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.08} />
      <Header
        title="Note"
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
        right={
          <IconButton
            label="Delete note"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash />
          </IconButton>
        }
      />

      <div className="th-scroll th-content-pad">
        {/* Category + title */}
        <p className="th-note-detail__category">
          <CategoryIcon size={14} />
          {CATEGORY_LABELS[note.category]}
        </p>
        <h1 className="th-note-detail__title">{note.title}</h1>
        <p className="th-note-detail__edited">{formatLastEdited(note.updatedAt)}</p>

        {/* Body */}
        <div className="th-note-detail__content">
          {note.content ? (
            note.content.split('\n').map((line, i) => (
              <p key={i}>{line || ' '}</p>
            ))
          ) : (
            <p className="th-note-detail__empty">No content</p>
          )}
        </div>

        {/* Actions */}
        <div className="th-note-detail__actions">
          <Button
            variant="primary"
            full
            onClick={() => navigate(`/app/notes/${note.id}/edit`)}
          >
            <IconEdit size={18} /> Edit Note
          </Button>
          <button
            className="th-note-detail__delete"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash size={16} /> Delete this note
          </button>
        </div>

        {/* Quiet metadata */}
        <p className="th-note-detail__meta">
          Written {new Date(note.createdAt).toLocaleDateString()}
          {note.updatedAt !== note.createdAt &&
            ` · Updated ${new Date(note.updatedAt).toLocaleDateString()}`}
        </p>
      </div>

      {/* Delete confirmation modal */}
      <Modal open={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)} label="Delete note">
        <div style={{ padding: 'var(--th-space-2) 0' }}>
          <h3 className="th-note-confirm-title">Delete this note?</h3>
          <p className="th-note-confirm-copy">
            “{note.title}” will be removed permanently. This action cannot be undone.
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
            <Button variant="primary" full onClick={handleDelete} disabled={deleting}>
              {deleting ? 'Deleting…' : 'Delete'}
            </Button>
            <Button variant="ghost" full onClick={() => setShowDeleteConfirm(false)} disabled={deleting}>
              Cancel
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
