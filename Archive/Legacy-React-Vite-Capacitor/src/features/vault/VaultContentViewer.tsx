/**
 * VaultContentViewer — premium vault item detail view (Stage 12).
 *
 * Content-aware display with type icon, metadata, edit/delete actions,
 * confirmation dialog, toast feedback, and privacy footer.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, IconLock, IconBack, IconEdit, IconTrash } from '../../components/index.ts';
import { ConfirmDialog, useToast } from '../../components/index.ts';
import type { VaultItem } from '../../data/vault/vaultTypes.ts';
import { CONTENT_TYPE_META } from './contentTypeMeta.tsx';
import { formatVaultDate, securityLabel } from './vaultPresentation.ts';

interface VaultContentViewerProps {
  service?: {
    getById: (id: string) => Promise<VaultItem | null>;
    delete: (id: string) => Promise<void>;
    update: (id: string, input: { title?: string; description?: string | null; content?: string | null }) => Promise<VaultItem>;
  };
}

export function VaultContentViewer({ service }: VaultContentViewerProps) {
  const navigate = useNavigate();
  const { itemId } = useParams<{ itemId: string }>();
  const toast = useToast();
  const [item, setItem] = useState<VaultItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
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
    setDeleting(true);
    try {
      await service.delete(itemId);
      toast.success('Vault item deleted');
      navigate(RoutePath.appVault);
    } catch {
      toast.error('Could not delete item');
      setShowDeleteConfirm(false);
    } finally {
      setDeleting(false);
    }
  }, [itemId, service, navigate, toast]);

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
      toast.success('Vault item updated');
    } catch {
      toast.error('Could not update item');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading…</p>
        </div>
      </div>
    );
  }

  if (!item) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconLock size={42} />
          </div>
          <h3 className="th-empty-emotional__title">Item not found</h3>
          <p className="th-empty-emotional__message">
            This vault item may have been deleted.
          </p>
          <div className="th-empty-emotional__action">
            <button
              className="th-btn th-btn--primary"
              onClick={() => navigate(RoutePath.appVault)}
            >
              Back to Vault
            </button>
          </div>
        </div>
      </div>
    );
  }

  const Meta = CONTENT_TYPE_META[item.contentType] ?? CONTENT_TYPE_META.file;

  return (
    <div className="th-content-pad">
      {/* Back + edit header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          type="button"
          className="th-icon-button"
          onClick={() => editing ? setEditing(false) : navigate(-1)}
          aria-label={editing ? 'Cancel editing' : 'Go back'}
        >
          <IconBack size={20} />
        </button>
        <div style={{ flex: 1 }} />
        {!editing && (
          <button
            type="button"
            className="th-icon-button"
            onClick={() => setEditing(true)}
            aria-label="Edit item"
          >
            <IconEdit size={20} />
          </button>
        )}
      </div>

      {editing ? (
        /* --- Edit mode --- */
        <div className="th-vault-form">
          <div className="th-form-group">
            <label className="th-label" htmlFor="edit-title">Title</label>
            <input
              id="edit-title"
              type="text"
              className="th-input"
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              maxLength={200}
            />
          </div>
          {item.contentType === 'note' && (
            <div className="th-form-group">
              <label className="th-label" htmlFor="edit-content">Content</label>
              <textarea
                id="edit-content"
                className="th-input"
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                rows={8}
                style={{ resize: 'vertical' }}
              />
            </div>
          )}
          <div className="th-vault-actions">
            <button
              type="button"
              className="th-btn th-btn--outline"
              onClick={() => setEditing(false)}
            >
              Cancel
            </button>
            <button
              type="button"
              className="th-btn th-btn--primary"
              onClick={handleSaveEdit}
              disabled={saving || !editTitle.trim()}
            >
              {saving ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </div>
      ) : (
        /* --- View mode --- */
        <>
          {/* Header */}
          <div className="th-vault-detail-header">
            <div className="th-vault-detail-header__icon" aria-hidden="true">
              <Meta.Icon size={24} />
            </div>
            <div className="th-vault-detail-header__body">
              <h1 className="th-vault-detail-header__title">{item.title}</h1>
              <span className="th-vault-detail-header__type">
                {securityLabel(item.contentType)}
              </span>
            </div>
          </div>

          {/* Content card */}
          <div className="th-vault-content-card">
            {item.contentType === 'note' && item.content ? (
              <div className="th-vault-content-card__body">{item.content}</div>
            ) : (
              <div className="th-vault-content-card__placeholder">
                <div className="th-vault-content-card__placeholder-icon" aria-hidden="true">
                  <Meta.Icon size={48} />
                </div>
                <p style={{ fontSize: 'var(--th-font-size-sm)', margin: 0 }}>
                  {item.contentType === 'photo' && 'Protected photo content'}
                  {item.contentType === 'video' && 'Protected video content'}
                  {item.contentType === 'file' && 'Secured file content'}
                </p>
              </div>
            )}

            {/* Description */}
            {item.description && (
              <div className="th-vault-content-card__description">
                {item.description}
              </div>
            )}

            {/* Metadata */}
            <div className="th-vault-content-card__meta">
              <p style={{ margin: 0 }}>
                Added {formatVaultDate(item.createdAt)}
              </p>
              {item.updatedAt !== item.createdAt && (
                <p style={{ margin: '2px 0 0' }}>
                  Updated {formatVaultDate(item.updatedAt)}
                </p>
              )}
            </div>
          </div>

          {/* Delete zone */}
          <div className="th-vault-danger-zone">
            <Button
              variant="danger"
              full
              onClick={() => setShowDeleteConfirm(true)}
            >
              <IconTrash size={16} />
              Delete from Vault
            </Button>
          </div>
        </>
      )}

      {/* Delete confirmation */}
      <ConfirmDialog
        open={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        label="Delete vault item"
        title="Delete this item?"

        description="This item will be removed permanently. This action cannot be undone."
        actionLabel="Delete"
        onAction={handleDelete}
        busy={deleting}
        busyLabel="Deleting…"
      />
    </div>
  );
}
