/**
 * MemoryDetail (Stage 5).
 *
 * A memory opens like a shared moment: hero media first, serif title with
 * a heart accent, the date, then the story. Additional media renders as a
 * thumb strip that swaps the hero. Edit routes to the shared add/edit
 * form; delete confirms through the existing Modal + toast layer.
 * Media resolves through MemoryService → MediaStorage `data:` URLs.
 */

import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { MemoryWithMedia } from '../../services/memory/memoryService.ts';
import { useMemoryService } from './useMemoryService.ts';
import { formatDateKey } from './memoryFilters.ts';
import {
  Button,
  Header,
  IconBack,
  IconButton,
  IconCamera,
  IconVideo,
  IconHeart,
  IconTrash,
  IconEdit,
  IconCalendar,
  LoadingState,
  RoseLilyDecoration,
  ConfirmDialog,
  useToast,
} from '../../components/index.ts';

export function MemoryDetail() {
  const { memoryId } = useParams<{ memoryId: string }>();
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const toast = useToast();
  const [memory, setMemory] = useState<MemoryWithMedia | null>(null);
  const [mediaUrls, setMediaUrls] = useState<Record<string, string>>({});
  const [heroIndex, setHeroIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const loadMemory = useCallback(async () => {
    if (!memoryService || !memoryId) return;
    setLoading(true);
    setError(null);
    try {
      const result = await memoryService.getMemory(memoryId);
      setMemory(result);
      const urls: Record<string, string> = {};
      await Promise.all(
        result.mediaReferences.map(async (ref) => {
          try {
            urls[ref.id] = await memoryService.resolveMediaUrl(ref.id);
          } catch {
            // Missing bytes — tile falls back to a warm placeholder.
          }
        }),
      );
      setMediaUrls(urls);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Memory not found.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [memoryService, memoryId]);

  useEffect(() => {
    loadMemory();
  }, [loadMemory]);

  const handleDelete = async () => {
    if (!memoryService || !memoryId) return;
    setDeleting(true);
    try {
      await memoryService.deleteMemory(memoryId);
      toast.success('Memory deleted');
      navigate(RoutePath.appMemories, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to delete memory.';
      setError(message);
      setShowDeleteConfirm(false);
      toast.error('Could not delete memory');
    } finally {
      setDeleting(false);
    }
  };

  if (loading || !memoryService) {
    return (
      <div className="th-screen">
        <Header
          title="Memory"
          left={
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          }
        />
        <LoadingState label="Loading memory…" />
      </div>
    );
  }

  if (error || !memory) {
    return (
      <div className="th-screen">
        <Header
          title="Memory"
          left={
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          }
        />
        <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
          <p style={{ color: 'var(--th-color-error)', marginBottom: 'var(--th-space-4)' }}>
            {error || 'Memory not found.'}
          </p>
          <Button variant="secondary" onClick={() => navigate(RoutePath.appMemories)}>
            Back to memories
          </Button>
        </div>
      </div>
    );
  }

  const media = memory.mediaReferences;
  const heroRef = media[Math.min(heroIndex, media.length - 1)];
  const heroUrl = heroRef ? mediaUrls[heroRef.id] : undefined;

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="top-right" opacity={0.1} />
      <Header
        title="Memory"
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
        right={
          <IconButton
            label="Delete memory"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash />
          </IconButton>
        }
      />

      {/* Content */}
      <div className="th-scroll th-content-pad">
        {/* Hero media */}
        {media.length > 0 && (
          <div className="th-memory-hero">
            {heroRef && heroUrl && heroRef.kind === 'photo' && (
              <img src={heroUrl} alt={`${memory.title} photo`} className="th-memory-hero__image" />
            )}
            {heroRef && heroUrl && heroRef.kind === 'video' && (
              <video
                src={heroUrl}
                controls
                className="th-memory-hero__image"
                aria-label={`${memory.title} video`}
              />
            )}
            {heroRef && !heroUrl && (
              <div className="th-memory-hero__placeholder" aria-hidden="true">
                {heroRef.kind === 'photo' ? <IconCamera size={40} /> : <IconVideo size={40} />}
              </div>
            )}

            {media.length > 1 && (
              <div className="th-memory-strip">
                {media.map((ref, index) => (
                  <button
                    key={ref.id}
                    type="button"
                    className={`th-memory-strip__item ${index === heroIndex ? 'th-memory-strip__item--active' : ''}`}
                    onClick={() => setHeroIndex(index)}
                    aria-label={`Show media ${index + 1} of ${media.length}`}
                    aria-pressed={index === heroIndex}
                  >
                    {mediaUrls[ref.id] && ref.kind === 'photo' ? (
                      <img src={mediaUrls[ref.id]} alt="" className="th-memory-strip__img" />
                    ) : (
                      <span className="th-memory-strip__fallback" aria-hidden="true">
                        {ref.kind === 'photo' ? <IconCamera size={16} /> : <IconVideo size={16} />}
                      </span>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Title + heart accent */}
        <div className="th-memory-title-row">
          <h2 className="th-memory-title">{memory.title}</h2>
          <span className="th-memory-heart" aria-hidden="true">
            <IconHeart size={22} />
          </span>
        </div>

        {memory.memoryDate && (
          <p className="th-mem-date th-mem-date--large">
            <IconCalendar size={16} />
            {formatDateKey(memory.memoryDate)}
          </p>
        )}

        {memory.caption && (
          <p className="th-memory-story">{memory.caption}</p>
        )}

        {/* Actions */}
        <div className="th-memory-actions">
          <Button
            variant="primary"
            full
            onClick={() => navigate(`${RoutePath.appMemories}/${memory.id}/edit`)}
          >
            <IconEdit size={18} /> Edit Memory
          </Button>
        </div>

        {/* Metadata footer */}
        <div className="th-memory-meta">
          <p>
            Added {new Date(memory.createdAt).toLocaleDateString()}
            {memory.updatedAt !== memory.createdAt &&
              ` · Updated ${new Date(memory.updatedAt).toLocaleDateString()}`}
          </p>
        </div>
      </div>

      {/* Delete confirmation */}
      <ConfirmDialog
        open={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        label="Delete memory"
        title="Delete this memory?"
        description={`“${memory.title}” will be removed permanently. This action cannot be undone.`}
        actionLabel="Delete"
        onAction={handleDelete}
        busy={deleting}
        busyLabel="Deleting…"
      />
    </div>
  );
}
