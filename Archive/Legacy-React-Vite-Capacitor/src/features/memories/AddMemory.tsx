/**
 * AddMemory (Stage 5).
 *
 * Create/edit form for a memory. Photo-first: a warm media dropzone opens
 * the local file picker (photos + videos), selections preview inline and
 * can be removed before saving. All bytes stay on-device through
 * MemoryService → MediaStorage — no remote upload, no external URLs.
 *
 * Edit mode reuses this screen at `/app/memories/:memoryId/edit`:
 * fields prefill, existing media renders with per-item removal, and new
 * selections are attached on save.
 */

import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  DatePicker,
  Header,
  IconBack,
  IconButton,
  IconCamera,
  IconClose,
  IconImage,
  IconVideo,
  Input,
  LoadingState,
  useToast,
} from '../../components/index.ts';
import type { MediaKind } from '../../data/media/mediaTypes.ts';
import type { MediaReference } from '../../data/media/mediaStorage.ts';
import { useMemoryService } from './useMemoryService.ts';

interface PendingMedia {
  kind: MediaKind;
  mimeType: string;
  data: Uint8Array;
  /** Object URL for local preview (revoked on unmount/remove). */
  previewUrl: string;
}

function readFileBytes(file: File): Promise<Uint8Array> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Could not read the selected file.'));
    reader.onload = () => resolve(new Uint8Array(reader.result as ArrayBuffer));
    reader.readAsArrayBuffer(file);
  });
}

export function AddMemory() {
  const { memoryId } = useParams<{ memoryId?: string }>();
  const editing = Boolean(memoryId);
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [caption, setCaption] = useState('');
  const [memoryDate, setMemoryDate] = useState<string | null>(null);
  const [existingMedia, setExistingMedia] = useState<MediaReference[]>([]);
  const [existingUrls, setExistingUrls] = useState<Record<string, string>>({});
  const [pendingMedia, setPendingMedia] = useState<PendingMedia[]>([]);
  const [loading, setLoading] = useState(editing);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ title?: string }>({});
  const fileInputRef = useRef<HTMLInputElement>(null);

  /* Prefill fields + existing media in edit mode. */
  useEffect(() => {
    if (!editing || !memoryService || !memoryId) return;
    let cancelled = false;
    (async () => {
      try {
        const memory = await memoryService.getMemory(memoryId);
        if (cancelled) return;
        setTitle(memory.title);
        setCaption(memory.caption ?? '');
        setMemoryDate(memory.memoryDate);
        setExistingMedia(memory.mediaReferences);
        const urls: Record<string, string> = {};
        for (const ref of memory.mediaReferences) {
          if (ref.kind !== 'photo') continue;
          try {
            urls[ref.id] = await memoryService.resolveMediaUrl(ref.id);
          } catch {
            // Missing media bytes — fallback tile renders instead.
          }
        }
        if (!cancelled) setExistingUrls(urls);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Memory not found.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [editing, memoryId, memoryService]);

  /* Revoke object URLs when pending media changes/unmounts. */
  useEffect(() => {
    return () => {
      pendingMedia.forEach((item) => URL.revokeObjectURL(item.previewUrl));
    };
  }, [pendingMedia]);

  const acceptAttr = 'image/jpeg,image/png,image/webp,image/gif,video/mp4,video/webm';

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    setError(null);
    for (const file of Array.from(files)) {
      try {
        const kind: MediaKind = file.type.startsWith('video/') ? 'video' : 'photo';
        const data = await readFileBytes(file);
        setPendingMedia((prev) => [
          ...prev,
          { kind, mimeType: file.type, data, previewUrl: URL.createObjectURL(file) },
        ]);
      } catch {
        setError(`Could not read “${file.name}”. Try a different file.`);
      }
    }
    // Reset so re-selecting the same file still fires onChange.
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const removePending = (previewUrl: string) => {
    setPendingMedia((prev) => prev.filter((item) => item.previewUrl !== previewUrl));
  };

  const removeExisting = async (assetId: string) => {
    if (!memoryService || !memoryId) return;
    try {
      await memoryService.removeMedia(memoryId, assetId);
      setExistingMedia((prev) => prev.filter((ref) => ref.id !== assetId));
      toast.success('Photo removed');
    } catch {
      toast.error('Could not remove photo');
    }
  };

  const totalMedia = existingMedia.length + pendingMedia.length;

  const handleSave = async () => {
    if (!memoryService) {
      setError('Service not available. Please restart the app.');
      return;
    }

    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setFieldErrors({ title: 'Please enter a title for this memory.' });
      return;
    }
    if (trimmedTitle.length > 100) {
      setFieldErrors({ title: 'Title must be 100 characters or fewer.' });
      return;
    }
    setFieldErrors({});

    setSaving(true);
    setError(null);
    try {
      const mediaItems = pendingMedia.map(({ kind, mimeType, data }) => ({ kind, mimeType, data }));
      if (editing && memoryId) {
        await memoryService.updateMemory(memoryId, {
          title: trimmedTitle,
          caption: caption.trim() || null,
          memoryDate: memoryDate || null,
        });
        for (const item of mediaItems) {
          await memoryService.addMedia(memoryId, item.kind, item.mimeType, item.data);
        }
        toast.success('Memory updated');
        navigate(`${RoutePath.appMemories}/${memoryId}`, { replace: true });
      } else {
        const memory = await memoryService.createMemory(
          {
            title: trimmedTitle,
            caption: caption.trim() || null,
            memoryDate: memoryDate || null,
          },
          mediaItems,
        );
        toast.success('Memory saved');
        navigate(`${RoutePath.appMemories}/${memory.id}`, { replace: true });
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save memory.';
      setError(message);
      toast.error(editing ? 'Could not update memory' : 'Could not save memory');
    } finally {
      setSaving(false);
    }
  };

  const heading = editing ? 'Edit Memory' : 'New Memory';
  const description = editing
    ? 'Update this moment from your story'
    : 'Capture a special moment';

  return (
    <div className="th-screen th-screen-warm">
      <Header
        title={heading}
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
      />

      {loading ? (
        <LoadingState label="Loading memory…" />
      ) : (
        <div className="th-scroll th-content-pad">
          <p className="th-mem-form-subtitle">{description}</p>

          {/* Media dropzone — photos/videos stay local */}
          <button
            type="button"
            className="th-media-dropzone"
            onClick={() => fileInputRef.current?.click()}
            disabled={saving}
            aria-label="Add photos or videos"
          >
            <span className="th-media-dropzone__icon" aria-hidden="true">
              <IconCamera size={30} />
            </span>
            <span className="th-media-dropzone__title">
              {totalMedia > 0 ? 'Add more photos' : 'Add a photo'}
            </span>
            <span className="th-media-dropzone__hint">
              {totalMedia > 0
                ? `${totalMedia} ${totalMedia === 1 ? 'item' : 'items'} selected`
                : 'Capture the moment'}
            </span>
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept={acceptAttr}
            multiple
            hidden
            onChange={(e) => { void handleFiles(e.target.files); }}
          />

          {(existingMedia.length > 0 || pendingMedia.length > 0) && (
            <div className="th-media-previews">
              {existingMedia.map((ref) => (
                <div key={ref.id} className="th-media-thumb">
                  {existingUrls[ref.id] ? (
                    <img src={existingUrls[ref.id]} alt="Saved media" className="th-media-thumb__img" />
                  ) : (
                    <span className="th-media-thumb__fallback" aria-hidden="true">
                      {ref.kind === 'photo' ? <IconImage size={20} /> : <IconVideo size={20} />}
                    </span>
                  )}
                  <button
                    type="button"
                    className="th-media-thumb__remove"
                    onClick={() => void removeExisting(ref.id)}
                    aria-label="Remove media"
                  >
                    <IconClose size={14} />
                  </button>
                </div>
              ))}
              {pendingMedia.map((item) => (
                <div key={item.previewUrl} className="th-media-thumb">
                  {item.kind === 'photo' ? (
                    <img src={item.previewUrl} alt="Selected media" className="th-media-thumb__img" />
                  ) : (
                    <span className="th-media-thumb__fallback" aria-hidden="true">
                      <IconVideo size={20} />
                    </span>
                  )}
                  <button
                    type="button"
                    className="th-media-thumb__remove"
                    onClick={() => removePending(item.previewUrl)}
                    aria-label="Remove media"
                  >
                    <IconClose size={14} />
                  </button>
                </div>
              ))}
            </div>
          )}

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="memory-title">
              Memory title
            </label>
            <Input
              id="memory-title"
              placeholder="Give this memory a name"
              value={title}
              onChange={(e) => {
                setTitle(e.target.value);
                if (fieldErrors.title) setFieldErrors({});
              }}
              maxLength={100}
              disabled={saving}
              aria-invalid={!!fieldErrors.title}
            />
            {fieldErrors.title && (
              <p className="th-form-error" role="alert">{fieldErrors.title}</p>
            )}
          </div>

          <div className="th-form-group">
            <label className="th-form-label" htmlFor="memory-caption">
              What's the story? <span className="th-form-optional">(optional)</span>
            </label>
            <Input
              id="memory-caption"
              multiline
              placeholder="Write something you'll want to remember…"
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              maxLength={2000}
              disabled={saving}
              style={{ minHeight: '100px', resize: 'vertical' }}
            />
          </div>

          <div className="th-form-group">
            <label className="th-form-label">
              Date <span className="th-form-optional">(optional)</span>
            </label>
            <DatePicker
              value={memoryDate}
              onChange={(iso) => setMemoryDate(iso || null)}
              label="Memory date"
              placeholder="When did this happen?"
              disabled={saving}
              minYear={1950}
              maxYear={new Date().getFullYear()}
            />
          </div>

          {error && (
            <p className="th-form-error th-form-error--global" role="alert">
              {error}
            </p>
          )}

          <div className="th-onboarding-actions" style={{ marginTop: 'var(--th-space-4)' }}>
            <Button
              variant="primary"
              full
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? 'Saving…' : editing ? 'Save changes' : 'Save Memory'}
            </Button>
            <Button
              variant="ghost"
              full
              onClick={() => navigate(-1)}
              disabled={saving}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
