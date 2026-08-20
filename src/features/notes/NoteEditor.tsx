/**
 * NoteEditor (Phase 8).
 *
 * Create/edit note screen with title, content, category.
 * Validates input and persists through NoteService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useNoteService } from './useNoteService.ts';
import type { NoteCategory } from '../../data/note/noteTypes.ts';
import { NOTE_CATEGORIES } from '../../data/note/noteTypes.ts';

const CATEGORY_LABELS: Record<string, string> = {
  general: 'General',
  shared: 'Shared',
  private: 'Private',
  'love-letter': 'Love Letter',
  gratitude: 'Gratitude',
  idea: 'Idea',
  reminder: 'Reminder',
};

export function NoteEditor() {
  const navigate = useNavigate();
  const { noteId } = useParams<{ noteId: string }>();
  const { createNote, updateNote, getNote, validateNote } = useNoteService();

  const isEditing = Boolean(noteId);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState<NoteCategory>('general');
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
      } else {
        await createNote({ title: title.trim(), content, category });
      }
      navigate(RoutePath.appNotes, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save note.';
      setErrors([message]);
    } finally {
      setSaving(false);
    }
  }, [title, content, category, isEditing, noteId, validateNote, createNote, updateNote, navigate]);

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

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        {isEditing ? 'Edit Note' : 'New Note'}
      </h1>

      {errors.length > 0 && (
        <div className="th-form-errors">
          {errors.map((err, i) => (
            <p key={i} className="th-form-error">{err}</p>
          ))}
        </div>
      )}

      <div className="th-form">
        <div className="th-form-group">
          <label className="th-label" htmlFor="note-title">Title *</label>
          <input
            id="note-title"
            type="text"
            className="th-input"
            placeholder="Note title..."
            value={title}
            onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
            maxLength={200}
            autoFocus
          />
          <span className="th-char-count">{title.length}/200</span>
        </div>

        <div className="th-form-group">
          <label className="th-label" htmlFor="note-category">Category</label>
          <div className="th-note-category-select">
            {NOTE_CATEGORIES.map((cat) => (
              <button
                key={cat}
                type="button"
                className={`th-chip ${category === cat ? 'th-chip--active' : ''}`}
                onClick={() => setCategory(cat)}
              >
                {CATEGORY_LABELS[cat]}
              </button>
            ))}
          </div>
        </div>

        <div className="th-form-group">
          <label className="th-label" htmlFor="note-content">Content</label>
          <textarea
            id="note-content"
            className="th-textarea"
            placeholder="Write your note..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={12}
          />
        </div>
      </div>

      <div className="th-note-editor-actions">
        <button
          className="th-btn th-btn--secondary"
          onClick={() => navigate(-1)}
          disabled={saving}
        >
          Cancel
        </button>
        <button
          className="th-btn th-btn--primary"
          onClick={handleSave}
          disabled={saving || !title.trim()}
        >
          {saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Create Note'}
        </button>
      </div>
    </div>
  );
}
