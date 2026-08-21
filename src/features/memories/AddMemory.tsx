/**
 * AddMemory (Phase 7).
 *
 * Screen for creating a new memory with title, optional caption,
 * optional date. Media upload is a future enhancement (camera/gallery
 * integration in a later phase). For now, memories are created with
 * text content only.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input, useToast } from '../../components/index.ts';
import { OnboardingLayout } from '../onboarding/OnboardingLayout.tsx';
import { useMemoryService } from './useMemoryService.ts';

export function AddMemory() {
  const navigate = useNavigate();
  const memoryService = useMemoryService();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [caption, setCaption] = useState('');
  const [memoryDate, setMemoryDate] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ title?: string }>({});

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
      const memory = await memoryService.createMemory({
        title: trimmedTitle,
        caption: caption.trim() || null,
        memoryDate: memoryDate || null,
      });
      toast.success('Memory saved');
      navigate(`${RoutePath.appMemories}/${memory.id}`, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save memory.';
      setError(message);
      toast.error('Could not save memory');
    } finally {
      setSaving(false);
    }
  };

  return (
    <OnboardingLayout currentPath={RoutePath.appMemoriesAdd} showBack>
      <div className="th-onboarding-form">
        <h2 className="th-onboarding-heading">New Memory</h2>
        <p className="th-onboarding-description">
          Capture a special moment
        </p>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="memory-title">
            Title
          </label>
          <Input
            id="memory-title"
            placeholder="Give this memory a title"
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
            Caption <span className="th-form-optional">(optional)</span>
          </label>
          <Input
            id="memory-caption"
            multiline
            placeholder="Add details about this memory…"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            maxLength={2000}
            disabled={saving}
            style={{ minHeight: '100px', resize: 'vertical' }}
          />
        </div>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="memory-date">
            Date <span className="th-form-optional">(optional)</span>
          </label>
          <Input
            id="memory-date"
            type="date"
            value={memoryDate}
            onChange={(e) => setMemoryDate(e.target.value)}
            disabled={saving}
          />
        </div>

        {error && (
          <p className="th-form-error th-form-error--global" role="alert">
            {error}
          </p>
        )}

        <div className="th-onboarding-actions">
          <Button
            variant="primary"
            full
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? 'Saving…' : 'Save Memory'}
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
    </OnboardingLayout>
  );
}
