/**
 * MemoriesHome (Phase 7).
 *
 * Main memories screen showing a gallery list of all memories.
 * Supports empty state, loading, error state, and add-memory entry point.
 */

import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { MemoryWithMedia } from '../../services/memory/memoryService.ts';
import { useMemoryService } from './useMemoryService.ts';
import { Button, IconPlus, IconImage, IconChevronRight, LoadingState } from '../../components/index.ts';

export function MemoriesHome() {
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const [memories, setMemories] = useState<MemoryWithMedia[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadMemories = useCallback(async () => {
    if (!memoryService) return;
    setLoading(true);
    setError(null);
    try {
      const result = await memoryService.listMemories();
      setMemories(result);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load memories.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [memoryService]);

  useEffect(() => {
    loadMemories();
  }, [loadMemories]);

  if (loading || !memoryService) {
    return <LoadingState label="Loading memories…" />;
  }

  if (error) {
    return (
      <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
        <p style={{ color: 'var(--th-color-error)', marginBottom: 'var(--th-space-4)' }}>{error}</p>
        <Button variant="secondary" onClick={loadMemories}>Try again</Button>
      </div>
    );
  }

  // Empty state
  if (memories.length === 0) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <IconImage size={36} />
          </div>
          <h3 className="th-empty-state__title">No memories yet</h3>
          <p className="th-empty-state__desc">
            Capture your favorite moments together
          </p>
          <Button variant="primary" onClick={() => navigate(RoutePath.appMemoriesAdd)}>
            <IconPlus size={18} /> Add your first memory
          </Button>
        </div>
      </div>
    );
  }

  // Memory list
  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Memories</h1>
        <Button variant="ghost" onClick={() => navigate(RoutePath.appMemoriesAdd)} style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}>
          <IconPlus size={20} />
        </Button>
      </div>

      <div className="th-hub-grid">
        {memories.map((memory) => (
          <Link
            key={memory.id}
            to={`${RoutePath.appMemories}/${memory.id}`}
            className="th-feature-card th-memory-card--enhanced th-stagger-item"
          >
            {/* Placeholder thumbnail */}
            <div style={{
              width: '80px',
              height: '80px',
              flex: '0 0 auto',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: 'var(--th-color-blush)',
              borderRadius: 'var(--th-radius-md) 0 0 var(--th-radius-md)',
              color: 'var(--th-color-rose-muted)',
            }}>
              <IconImage size={24} />
            </div>

            <div className="th-feature-card__body" style={{ padding: 'var(--th-space-3)' }}>
              <div className="th-feature-card__title">{memory.title}</div>
              {memory.memoryDate && (
                <div className="th-feature-card__desc">{memory.memoryDate}</div>
              )}
              {memory.mediaReferences.length > 0 && (
                <div className="th-feature-card__desc">
                  {memory.mediaReferences.length} {memory.mediaReferences.length === 1 ? 'item' : 'items'}
                </div>
              )}
            </div>
            <IconChevronRight size={18} className="th-feature-card__chevron" style={{ marginRight: 'var(--th-space-3)' }} />
          </Link>
        ))}
      </div>
    </div>
  );
}
