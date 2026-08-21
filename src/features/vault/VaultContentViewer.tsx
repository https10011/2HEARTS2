/**
 * VaultContentViewer (Phase 17).
 *
 * Displays a single vault item with full content, edit/delete actions.
 * Uses real persisted data via VaultService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconLock } from '../../components/index.ts';
import type { VaultItem, VaultContentType } from '../../data/vault/vaultTypes.ts';
import { CONTENT_TYPE_META } from './contentTypeMeta.tsx';

interface VaultContentViewerProps {
  service?: {
    getById: (id: string) => Promise<VaultItem | null>;
    delete: (id: string) => Promise<void>;
    update: (id: string, input: { title?: string; description?: string | null; content?: string | null }) => Promise<VaultItem>;
  };
}

function TypeIcon({ contentType, size }: { contentType: VaultContentType; size: number }) {
  const Meta = CONTENT_TYPE_META[contentType] ?? CONTENT_TYPE_META.file;
  return <Meta.Icon size={size} />;
}

export function VaultContentViewer({ service }: VaultContentViewerProps) {
  const navigate = useNavigate();
  const { itemId } = useParams<{ itemId: string }>();
  const [item, setItem] = useState<VaultItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editContent, setEditContent] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!itemId || !service) return;
    const load = async () => {
      try {
        const data = await service.getById(itemId);
        setItem(data);
        if (data) {
          setEditTitle(data.title);
          setEditContent(data.content ?? '');
        }
      } catch {
        setItem(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [itemId, service]);

  const handleDelete = useCallback(async () => {
    if (!itemId || !service) return;
    try {
      await service.delete(itemId);
      navigate(RoutePath.appVault);
    } catch {
      setShowDeleteConfirm(false);
    }
  }, [itemId, service, navigate]);

  const handleSaveEdit = async () => {
    if (!itemId || !service) return;
    setSaving(true);
    try {
      const updated = await service.update(itemId, {
        title: editTitle,
        content: editContent || null,
      });
      setItem(updated);
      setEditing(false);
    } catch {
      // Silently handle
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  if (!item) {
    return (
      <div className="th-content-pad">
        <div className="th-card" style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ marginBottom: 'var(--th-space-2)', color: 'var(--th-color-rose-muted)' }}>
            <IconLock size={48} />
          </div>
          <h3 style={{ marginBottom: 'var(--th-space-2)' }}>Item not found</h3>
          <p style={{ color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
            This vault item may have been deleted.
          </p>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appVault)}>
            Back to Vault
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--th-space-4)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}>
          <TypeIcon contentType={item.contentType} size={24} />
          <h1 className="th-screen-title" style={{ margin: 0 }}>
            {editing ? 'Edit Item' : item.title}
          </h1>
        </div>
        <div style={{ display: 'flex', gap: 'var(--th-space-2)' }}>
          {!editing && (
            <button
              className="th-btn th-btn--outline th-btn--sm"
              onClick={() => setEditing(true)}
            >
              Edit
            </button>
          )}
        </div>
      </div>

      {editing ? (
        /* Edit mode */
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-4)' }}>
          <div>
            <label className="th-label">Title</label>
            <input
              type="text"
              className="th-input"
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              maxLength={200}
            />
          </div>
          {item.contentType === 'note' && (
            <div>
              <label className="th-label">Content</label>
              <textarea
                className="th-input"
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                rows={6}
                style={{ resize: 'vertical' }}
              />
            </div>
          )}
          <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
            <button className="th-btn th-btn--outline" onClick={() => setEditing(false)} style={{ flex: 1 }}>
              Cancel
            </button>
            <button
              className="th-btn th-btn--primary"
              onClick={handleSaveEdit}
              disabled={saving || !editTitle.trim()}
              style={{ flex: 1 }}
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </div>
      ) : (
        /* View mode */
        <div>
          {/* Content card */}
          <div className="th-card" style={{ padding: 'var(--th-space-4)', marginBottom: 'var(--th-space-4)' }}>
            {item.contentType === 'note' && item.content && (
              <div style={{ marginBottom: 'var(--th-space-3)' }}>
                <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{item.content}</p>
              </div>
            )}

            {item.contentType !== 'note' && (
              <div style={{ textAlign: 'center', padding: 'var(--th-space-4)' }}>
                <div style={{ marginBottom: 'var(--th-space-2)', color: 'var(--th-color-burgundy)' }}>
                  <TypeIcon contentType={item.contentType} size={48} />
                </div>
                <p style={{ color: 'var(--th-color-text-secondary)' }}>
                  {item.contentType === 'photo' && 'Photo content'}
                  {item.contentType === 'video' && 'Video content'}
                  {item.contentType === 'file' && 'File content'}
                </p>
              </div>
            )}

            {item.description && (
              <div style={{ borderTop: '1px solid var(--th-color-border)', paddingTop: 'var(--th-space-3)', marginTop: 'var(--th-space-3)' }}>
                <p style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', margin: 0, fontStyle: 'italic' }}>
                  {item.description}
                </p>
              </div>
            )}

            {/* Metadata */}
            <div style={{ borderTop: '1px solid var(--th-color-border)', paddingTop: 'var(--th-space-3)', marginTop: 'var(--th-space-3)' }}>
              <p style={{ margin: 0, fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                Added {new Date(item.createdAt).toLocaleDateString()}
              </p>
              {item.updatedAt !== item.createdAt && (
                <p style={{ margin: 0, fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                  Updated {new Date(item.updatedAt).toLocaleDateString()}
                </p>
              )}
            </div>
          </div>

          {/* Delete */}
          <div style={{ marginTop: 'var(--th-space-6)' }}>
            {showDeleteConfirm ? (
              <div className="th-card" style={{ padding: 'var(--th-space-4)', textAlign: 'center' }}>
                <p style={{ marginBottom: 'var(--th-space-3)' }}>Delete this item permanently? This cannot be undone.</p>
                <div style={{ display: 'flex', gap: 'var(--th-space-3)', justifyContent: 'center' }}>
                  <button className="th-btn th-btn--outline th-btn--sm" onClick={() => setShowDeleteConfirm(false)}>
                    Cancel
                  </button>
                  <button className="th-btn th-btn--danger th-btn--sm" onClick={handleDelete}>
                    Delete
                  </button>
                </div>
              </div>
            ) : (
              <button
                className="th-btn th-btn--danger th-btn--outline"
                onClick={() => setShowDeleteConfirm(true)}
                style={{ width: '100%' }}
              >
                Delete from Vault
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
