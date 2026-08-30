/**
 * AddVaultContent — premium vault item creation form (Stage 12).
 *
 * Branded content type picker, clean form layout, validation,
 * success/error feedback via toast, cancel behavior preserved.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { AppError } from '../../services/errors/appError.ts';
import { useToast } from '../../components/index.ts';
import type { VaultContentType } from '../../data/vault/vaultTypes.ts';
import { CONTENT_TYPE_META, CONTENT_TYPE_ORDER } from './contentTypeMeta.tsx';

interface AddVaultContentProps {
  service?: {
    create: (input: { title: string; contentType: VaultContentType; content?: string | null; description?: string | null; profileId: string }) => Promise<unknown>;
  };
}

export function AddVaultContent({ service }: AddVaultContentProps) {
  const navigate = useNavigate();
  const toast = useToast();
  const [title, setTitle] = useState('');
  const [contentType, setContentType] = useState<VaultContentType>('note');
  const [content, setContent] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      if (!service) {
        setError('Vault service not available.');
        return;
      }
      const profileId = 'owner';
      await service.create({
        title,
        contentType,
        content: contentType === 'note' ? content : null,
        description: description || null,
        profileId,
      });
      toast.success('Added to vault');
      navigate(RoutePath.appVault);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
        toast.error(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
        toast.error('Could not add vault content');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="th-content-pad">
      {/* Header */}
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>
        Add to Vault
      </h1>
      <p className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-5)' }}>
        Store something private — only you can see it.
      </p>

      {error && (
        <div className="th-form-error th-form-error--global" style={{ marginBottom: 'var(--th-space-4)' }} role="alert">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="th-vault-form">
        {/* Content type selection */}
        <div className="th-form-group">
          <label className="th-label">Content Type</label>
          <div className="th-vault-type-grid" role="radiogroup" aria-label="Content type">
            {CONTENT_TYPE_ORDER.map((value) => {
              const meta = CONTENT_TYPE_META[value];
              return (
                <button
                  key={value}
                  type="button"
                  role="radio"
                  aria-checked={contentType === value}
                  className={`th-vault-type-btn${contentType === value ? ' th-vault-type-btn--active' : ''}`}
                  onClick={() => setContentType(value)}
                >
                  <span className="th-vault-type-btn__icon" aria-hidden="true">
                    <meta.Icon size={20} />
                  </span>
                  <span>{meta.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Title */}
        <div className="th-form-group">
          <label className="th-label" htmlFor="vault-title">Title</label>
          <input
            id="vault-title"
            type="text"
            className="th-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Give it a name"
            required
            maxLength={200}
          />
        </div>

        {/* Note content (only for note type) */}
        {contentType === 'note' && (
          <div className="th-form-group">
            <label className="th-label" htmlFor="vault-content">Content</label>
            <textarea
              id="vault-content"
              className="th-input"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Write your private note…"
              rows={6}
              style={{ resize: 'vertical' }}
            />
          </div>
        )}

        {/* Non-note types show placeholder */}
        {contentType !== 'note' && (
          <div className="th-vault-placeholder">
            <div className="th-vault-placeholder__icon" aria-hidden="true">
              {(() => {
                const meta = CONTENT_TYPE_META[contentType];
                return <meta.Icon size={36} />;
              })()}
            </div>
            <p style={{ fontSize: 'var(--th-font-size-sm)', margin: 0 }}>
              {contentType === 'photo' && 'Photo upload will be available in a future update.'}
              {contentType === 'video' && 'Video upload will be available in a future update.'}
              {contentType === 'file' && 'File upload will be available in a future update.'}
            </p>
          </div>
        )}

        {/* Description */}
        <div className="th-form-group">
          <label className="th-label" htmlFor="vault-desc">
            Description <span className="th-form-optional">(optional)</span>
          </label>
          <input
            id="vault-desc"
            type="text"
            className="th-input"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Brief description"
            maxLength={500}
          />
        </div>

        {/* Actions */}
        <div className="th-vault-actions">
          <button
            type="button"
            className="th-btn th-btn--outline"
            onClick={() => navigate(-1)}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="th-btn th-btn--primary"
            disabled={saving || !title.trim()}
          >
            {saving ? 'Adding…' : 'Add to Vault'}
          </button>
        </div>
      </form>

      {/* Privacy footer */}
      <div className="th-vault-footer" style={{ marginTop: 'var(--th-space-6)' }}>
        <span>Stored locally on this device only</span>
      </div>
    </div>
  );
}
