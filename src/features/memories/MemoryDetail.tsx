/**
 * MemoryDetail (Phase 7).
 *
 * Displays a single memory's information, media, and provides
 * edit/delete actions. Handles missing records and media gracefully.
 */

import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { MemoryWithMedia } from '../../services/memory/memoryService.ts';
import { useMemoryService } from './useMemoryService.ts';
import { Button, IconButton, IconBack, LoadingState } from '../../components/index.ts';
import { Modal } from '../../components/index.ts';

export function MemoryDetail() {
  const { memoryId } = useParams<{ memoryId: string }>();
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const [memory, setMemory] = useState<MemoryWithMedia | null>(null);
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
      navigate(RoutePath.appMemories, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to delete memory.';
      setError(message);
      setShowDeleteConfirm(false);
    } finally {
      setDeleting(false);
    }
  };

  if (loading || !memoryService) {
    return (
      <div className="th-screen">
        <header className="th-app-header">
          <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)' }}>
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          </div>
          <h1 className="th-app-header__title">Memory</h1>
          <div style={{ minWidth: 'var(--th-touch-target-min)' }} />
        </header>
        <LoadingState label="Loading memory…" />
      </div>
    );
  }

  if (error || !memory) {
    return (
      <div className="th-screen">
        <header className="th-app-header">
          <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)' }}>
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          </div>
          <h1 className="th-app-header__title">Memory</h1>
          <div style={{ minWidth: 'var(--th-touch-target-min)' }} />
        </header>
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

  return (
    <div className="th-screen">
      {/* Header */}
      <header className="th-app-header">
        <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)' }}>
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        </div>
        <h1 className="th-app-header__title">Memory</h1>
        <div style={{ minWidth: 'var(--th-touch-target-min)' }}>
          <Button
            variant="ghost"
            onClick={() => setShowDeleteConfirm(true)}
            style={{ minWidth: 'auto', padding: 'var(--th-space-2)', color: 'var(--th-color-error)' }}
          >
            Delete
          </Button>
        </div>
      </header>

      {/* Content */}
      <div className="th-scroll th-content-pad">
        <h2 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>
          {memory.title}
        </h2>

        {memory.memoryDate && (
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginBottom: 'var(--th-space-4)' }}>
            {memory.memoryDate}
          </p>
        )}

        {memory.caption && (
          <p style={{ color: 'var(--th-color-text-primary)', fontSize: 'var(--th-font-size-md)', lineHeight: 'var(--th-line-height-relaxed)', marginBottom: 'var(--th-space-6)' }}>
            {memory.caption}
          </p>
        )}

        {/* Media section */}
        {memory.mediaReferences.length > 0 && (
          <div style={{ marginBottom: 'var(--th-space-6)' }}>
            <p style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-2)' }}>
              {memory.mediaReferences.length} {memory.mediaReferences.length === 1 ? 'item' : 'items'}
            </p>
            <div style={{ display: 'flex', gap: 'var(--th-space-2)', flexWrap: 'wrap' }}>
              {memory.mediaReferences.map((media) => (
                <div
                  key={media.id}
                  style={{
                    width: '100px',
                    height: '100px',
                    borderRadius: 'var(--th-radius-md)',
                    backgroundColor: 'var(--th-color-blush)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--th-color-rose-muted)',
                  }}
                >
                  {media.kind === 'photo' ? '📷' : '🎬'}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Metadata */}
        <div style={{ borderTop: '1px solid var(--th-color-divider)', paddingTop: 'var(--th-space-4)' }}>
          <p style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
            Created: {new Date(memory.createdAt).toLocaleDateString()}
          </p>
          {memory.updatedAt !== memory.createdAt && (
            <p style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
              Updated: {new Date(memory.updatedAt).toLocaleDateString()}
            </p>
          )}
        </div>
      </div>

      {/* Delete confirmation modal */}
      <Modal open={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)} label="Delete memory">
        <div style={{ padding: 'var(--th-space-2) 0' }}>
          <h3 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-lg)', marginBottom: 'var(--th-space-2)' }}>
            Delete this memory?
          </h3>
          <p style={{ color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-6)' }}>
            This action cannot be undone.
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
