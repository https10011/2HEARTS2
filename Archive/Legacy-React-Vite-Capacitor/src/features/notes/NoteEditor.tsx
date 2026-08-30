/**
 * NoteEditor (Phase 8, productized in Stage 6).
 *
 * Create/edit note screen — app header with back + Save, warm title
 * field, "last edited" line, and a paper-card writing surface.
 * Validates input and persists through NoteService (local-first).
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useNoteService } from './useNoteService.ts';
import {
  Header,
  IconButton,
  IconBack,
  LoadingState,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';
import type { NoteCategory } from '../../data/note/noteTypes.ts';
import { NOTE_CATEGORIES } from '../../data/note/noteTypes.ts';
import { NOTE_CATEGORY_LABELS as CATEGORY_LABELS } from './categoryMeta.ts';
import { formatLastEdited } from './noteTime.ts';

export function NoteEditor() {
  const navigate = useNavigate();
  const { noteId } = useParams<{ noteId: string }>();
  const { createNote, updateNote, getNote, validateNote } = useNoteService();
  const toast = useToast();

  const isEditing = Boolean(noteId);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState<NoteCategory>('general');
  const [updatedAt, setUpdatedAt] = useState<string | null>(null);
  const [errors, setErrors] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEditing);

  useEffect(() => {
    if (!noteId) return;
    let cancelled = false;
    (async () => {
      try {
        const note = await getNote(noteId);
        if (cancelled) return;
        setTitle(note.title);
        setContent(note.content);
        setCategory(note.category);
        setUpdatedAt(note.updatedAt);
      } catch {
        if (!cancelled) {
          navigate(RoutePath.appNotes, { replace: true });
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [noteId, getNote, navigate]);

  const handleSave = useCallback(async () => {
    const validation = validateNote({ title, content, category });
    if (!validation.ok) {
      setErrors(validation.errors);
      return;
    }

    setSaving(true);
    setErrors([]);

    try {
      if (isEditing && noteId) {
        await updateNote(noteId, { title: title.trim(), content, category });
        toast.success('Note updated');
      } else {
        await createNote({ title: title.trim(), content, category });
        toast.success('Note saved');
      }
      navigate(RoutePath.appNotes, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save note.';
      setErrors([message]);
      toast.error('Could not save note');
    } finally {
      setSaving(false);
    }
  }, [title, content, category, isEditing, noteId, validateNote, createNote, updateNote, navigate, toast]);

  if (loading) {
    return (
      <div className="th-screen">
        <Header
          title="Edit Note"
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

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.08} />
      <Header
        title={isEditing ? 'Edit Note' : 'New Note'}
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
        right={
          <button
            className="th-notes-save"
            onClick={handleSave}
            disabled={saving || !title.trim()}
          >
            {saving ? 'Saving…' : 'Save'}
          </button>
        }
      />

      <div className="th-scroll th-content-pad">
        {errors.length > 0 && (
          <div className="th-form-errors" role="alert">
            {errors.map((err, i) => (
              <p key={i} className="th-form-error">{err}</p>
            ))}
          </div>
        )}

        {/* Title */}
        <div className="th-form-group">
          <label className="th-label" htmlFor="note-title">Title</label>
          <input
            id="note-title"
            type="text"
            className="th-input th-notes-title-input"
            placeholder="Give your note a title"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
            maxLength={200}
            autoFocus={!isEditing}
            aria-invalid={errors.length > 0}
          />
        </div>

        <p className="th-notes-edited-line">
          {isEditing && updatedAt ? formatLastEdited(updatedAt) : 'A fresh page, just for the two of you.'}
        </p>

        {/* Paper writing surface */}
        <div className="th-notes-paper">
          <textarea
            id="note-content"
            className="th-notes-paper__textarea"
            placeholder="Start writing..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={12}
            aria-label="Note content"
          />
        </div>

        {/* Category */}
        <div className="th-form-group" style={{ marginTop: 'var(--th-space-5)' }}>
          <span className="th-label" id="note-category-label">Category</span>
          <div className="th-note-category-select" role="listbox" aria-labelledby="note-category-label">
            {NOTE_CATEGORIES.map((cat) => (
              <button
                key={cat}
                type="button"
                role="option"
                aria-selected={category === cat}
                className={`th-notes-chip ${category === cat ? 'th-notes-chip--active' : ''}`}
                onClick={() => setCategory(cat)}
              >
                {CATEGORY_LABELS[cat]}
              </button>
            ))}
          </div>
        </div>

        <div className="th-note-editor-actions">
          <button
            className="th-btn th-btn--ghost"
            onClick={() => navigate(-1)}
            disabled={saving}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
