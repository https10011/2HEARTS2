/**
 * Profile Settings (Phase 19 — roadmap screen 81).
 *
 * Edit the owner profile through RelationshipService (the same boundary
 * onboarding uses): display name + optional birthday. Saves persist into
 * the SQLite domain layer and survive restarts.
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input, DatePicker } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';
import type { Profile } from '../../data/relationship/relationshipTypes.ts';
import { SettingsScreen, InfoCard } from './settingsUi.tsx';

export function ProfileSettingsScreen() {
  const [owner, setOwner] = useState<Profile | null>(null);
  const [name, setName] = useState('');
  const [birthday, setBirthday] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const relationship = coreServices.relationship;
    if (relationship) {
      relationship
        .getOwner()
        .then((profile) => {
          if (cancelled || !profile) return;
          setOwner(profile);
          setName(profile.displayName);
          setBirthday(profile.birthDate ?? '');
        })
        .catch(() => undefined);
    }
    return () => {
      cancelled = true;
    };
  }, []);

  const handleSave = async () => {
    const relationship = coreServices.relationship;
    if (!relationship) {
      setError('Profile service is unavailable.');
      return;
    }
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      const updated = await relationship.saveOwner({
        displayName: name,
        birthDate: birthday.trim() === '' ? null : birthday,
      });
      setOwner(updated);
      setSaved(true);
    } catch (cause) {
      setError(safeUserMessage(cause));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SettingsScreen title="Profile Settings" backTo={RoutePath.appMoreSettings}>
      <div className="th-form-group">
        <label className="th-form-label" htmlFor="profile-name">
          Name
        </label>
        <Input
          id="profile-name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Your name"
          maxLength={40}
        />
      </div>

      <div className="th-form-group">
        <label className="th-form-label" htmlFor="profile-birthday">
          Birthday (optional)
        </label>
        <DatePicker
          value={birthday}
          onChange={setBirthday}
          label="Your birthday"
          placeholder="Tap to choose a date"
        />
      </div>

      {error ? (
        <p role="alert" style={{ color: 'var(--th-color-error)', fontSize: 'var(--th-font-size-sm)' }}>
          {error}
        </p>
      ) : null}
      {saved ? (
        <p role="status" style={{ color: 'var(--th-color-success)', fontSize: 'var(--th-font-size-sm)' }}>
          Saved.
        </p>
      ) : null}

      <Button variant="primary" full onClick={handleSave} disabled={saving || name.trim() === ''}>
        {saving ? 'Saving…' : 'Save Changes'}
      </Button>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Your profile is private"
          text="Your profile information is stored locally on this device."
        />
      </div>

      {owner ? null : (
        <div style={{ marginTop: 'var(--th-space-4)' }}>
          <InfoCard
            title="Complete your profile"
            text="Add a few details to make TwoHearts feel more personal."
          />
        </div>
      )}
    </SettingsScreen>
  );
}
