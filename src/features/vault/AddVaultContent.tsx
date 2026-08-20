/**
 * AddVaultContent (Phase 17).
 *
 * Form for adding content to the private vault.
 * Supports text notes, with placeholders for photo/video/file upload.
 * Uses real persisted data via VaultService.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { AppError } from '../../services/errors/appError.ts';
import type { VaultContentType } from '../../data/vault/vaultTypes.ts';

interface AddVaultContentProps {
  service?: {
    create: (input: { title: string; contentType: VaultContentType; content?: string | null; description?: string | null; profileId: string }) => Promise<any>;
  };
}

const CONTENT_TYPE_OPTIONS: Array<{ value: VaultContentType; label: string; icon: string; description: string }> = [
  { value: 'note', label: 'Private Note', icon: '📝', description: 'Text content stored securely' },
  { value: 'photo', label: 'Photo', icon: '🖼️', description: 'Protected photo (coming soon)' },
  { value: 'video', label: 'Video', icon: '🎬', description: 'Protected video (coming soon)' },
  { value: 'file', label: 'File', icon: '📁', description: 'Any file type (coming soon)' },
];

export function AddVaultContent({ service }: AddVaultContentProps) {
  const navigate = useNavigate();
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
      navigate(RoutePath.appVault);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        Add to Vault
      </h1>

      {error && (
        <div className="th-error-banner" style={{ marginBottom: 'var(--th-space-4)' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-4)' }}>
        {/* Content type selection */}
        <div>
          <label className="th-label">Content Type *</label>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--th-space-2)' }}>
            {CONTENT_TYPE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                className={`th-btn th-btn--sm ${contentType === opt.value ? 'th-btn--primary' : 'th-btn--outline'}`}
                onClick={() => setContentType(opt.value)}
                style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--th-space-1)', padding: 'var(--th-space-3)' }}
              >
                <span style={{ fontSize: '1.5rem' }}>{opt.icon}</span>
                <span style={{ fontSize: 'var(--th-text-xs)' }}>{opt.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Title */}
        <div>
          <label className="th-label">Title *</label>
          <input
            type="text"
            className="th-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Enter a title"
            required
            maxLength={200}
          />
        </div>

        {/* Note content (only for note type) */}
        {contentType === 'note' && (
          <div>
            <label className="th-label">Content</label>
            <textarea
              className="th-input"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Write your private note..."
              rows={6}
              style={{ resize: 'vertical' }}
            />
          </div>
        )}

        {/* Non-note types show placeholder */}
        {contentType !== 'note' && (
          <div className="th-card" style={{ padding: 'var(--th-space-4)', textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', marginBottom: 'var(--th-space-2)' }}>
              {CONTENT_TYPE_OPTIONS.find(o => o.value === contentType)?.icon}
            </div>
            <p style={{ color: 'var(--th-text-secondary)', fontSize: 'var(--th-text-sm)' }}>
              {contentType === 'photo' && 'Photo upload will be available in a future update.'}
              {contentType === 'video' && 'Video upload will be available in a future update.'}
              {contentType === 'file' && 'File upload will be available in a future update.'}
            </p>
          </div>
        )}

        {/* Description */}
        <div>
          <label className="th-label">Description (optional)</label>
          <input
            type="text"
            className="th-input"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Brief description"
            maxLength={500}
          />
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 'var(--th-space-3)', marginTop: 'var(--th-space-2)' }}>
          <button
            type="button"
            className="th-btn th-btn--outline"
            onClick={() => navigate(-1)}
            style={{ flex: 1 }}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="th-btn th-btn--primary"
            disabled={saving || !title.trim()}
            style={{ flex: 1 }}
          >
            {saving ? 'Adding...' : 'Add to Vault'}
          </button>
        </div>
      </form>
    </div>
  );
}
