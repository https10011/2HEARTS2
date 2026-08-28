/**
 * Profile Settings (Stage 15 — Settings + App Customization).
 *
 * Edit the owner profile through RelationshipService (the same boundary
 * onboarding uses): display name + optional birthday. Saves persist into
 * the SQLite domain layer and survive restarts.
 */

import { useEffect, useState, useCallback } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input, DatePicker, IconFileText, Modal } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';
import { ProfilePhotoService } from '../../services/profile/profilePhotoService.ts';
import type { Profile } from '../../data/relationship/relationshipTypes.ts';
import { SettingsScreen, InfoCard } from './settingsUi.tsx';

export function ProfileSettingsScreen() {
  const [owner, setOwner] = useState<Profile | null>(null);
  const [name, setName] = useState('');
  const [birthday, setBirthday] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [photoLoading, setPhotoLoading] = useState(false);
  const [photoService, setPhotoService] = useState<ProfilePhotoService | null>(null);
  const [showPhotoMenu, setShowPhotoMenu] = useState(false);

  // Initialize photo service and load current photo
  useEffect(() => {
    const relationship = coreServices.relationship;
    const mediaStorage = coreServices.mediaStorage;
    if (relationship && mediaStorage) {
      setPhotoService(new ProfilePhotoService(mediaStorage, relationship));
    }
  }, []);

  // Load photo URL when owner profile loads
  useEffect(() => {
    if (!owner?.photoRef || !photoService) {
      setPhotoUrl(null);
      return;
    }
    let cancelled = false;
    photoService.resolvePhotoUrl(owner.photoRef).then((url) => {
      if (!cancelled) setPhotoUrl(url);
    }).catch(() => { if (!cancelled) setPhotoUrl(null); });
    return () => { cancelled = true; };
  }, [owner?.photoRef, photoService]);

  const handlePhotoAction = useCallback(async (action: 'pick' | 'remove') => {
    if (!photoService || !owner) return;
    setShowPhotoMenu(false);
    setPhotoLoading(true);
    setError(null);
    try {
      if (action === 'pick') {
        const result = await photoService.selectAndSavePhoto('owner');
        if (result) {
          setPhotoUrl(result.dataUrl);
          // Reload profile to get updated photoRef
          const updated = await coreServices.relationship?.getOwner();
          if (updated) setOwner(updated);
        }
      } else if (action === 'remove') {
        await photoService.removePhoto('owner');
        setPhotoUrl(null);
        const updated = await coreServices.relationship?.getOwner();
        if (updated) setOwner(updated);
      }
    } catch (cause) {
      setError(safeUserMessage(cause));
    } finally {
      setPhotoLoading(false);
    }
  }, [photoService, owner]);

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
      <p style={{ marginTop: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
        Make TwoHearts feel more personal.
      </p>

      {/* Profile photo section */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Your Photo
      </div>
      <div className="th-settings-group--enhanced" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: 'var(--th-space-6) var(--th-space-4)', gap: 'var(--th-space-4)' }}>
        <button
          type="button"
          className="th-profile-avatar th-profile-avatar--large"
          style={{ width: 96, height: 96, cursor: 'pointer', border: 'none' }}
          onClick={() => setShowPhotoMenu(true)}
          aria-label={photoUrl ? 'Change profile photo' : 'Add profile photo'}
        >
          {photoUrl ? (
            <img src={photoUrl} alt="" className="th-profile-avatar__photo" draggable={false} style={{ width: '100%', height: '100%' }} />
          ) : owner?.displayName ? (
            <span className="th-profile-avatar__initial" style={{ fontSize: 'var(--th-font-size-3xl)' }}>
              {owner.displayName.trim().charAt(0).toUpperCase()}
            </span>
          ) : (
            <span className="th-profile-avatar__initial" style={{ fontSize: 'var(--th-font-size-3xl)' }}>?</span>
          )}
        </button>
        <p style={{ margin: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', textAlign: 'center' }}>
          {photoLoading ? 'Saving photo…' : photoUrl ? 'Tap to change your photo' : 'Tap to add a photo'}
        </p>
      </div>

      {/* Photo action menu */}
      <Modal open={showPhotoMenu} onClose={() => setShowPhotoMenu(false)}>
        <div style={{ padding: 'var(--th-space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
          <h3 style={{ margin: 0, fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-lg)', fontWeight: 'var(--th-font-weight-semibold)', textAlign: 'center' }}>
            Profile Photo
          </h3>
          <Button variant="primary" full onClick={() => handlePhotoAction('pick')}>
            {photoUrl ? 'Change Photo' : 'Choose Photo'}
          </Button>
          {photoUrl && (
            <Button variant="ghost" full onClick={() => handlePhotoAction('remove')}>
              Remove Photo
            </Button>
          )}
          <Button variant="ghost" full onClick={() => setShowPhotoMenu(false)}>
            Cancel
          </Button>
        </div>
      </Modal>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Your Details
      </div>
      <div className="th-settings-group--enhanced">
        <div className="th-form-group" style={{ padding: 'var(--th-space-4)' }}>
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

        <div className="th-form-group" style={{ padding: '0 var(--th-space-4) var(--th-space-4)' }}>
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
      </div>

      {error ? (
        <p role="alert" style={{ color: 'var(--th-color-error)', fontSize: 'var(--th-font-size-sm)', marginTop: 'var(--th-space-3)' }}>
          {error}
        </p>
      ) : null}
      {saved ? (
        <p role="status" style={{ color: 'var(--th-color-success)', fontSize: 'var(--th-font-size-sm)', marginTop: 'var(--th-space-3)' }}>
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
          icon={<IconFileText size={16} />}
        />
      </div>

      {owner ? null : (
        <div style={{ marginTop: 'var(--th-space-4)' }}>
          <InfoCard
            title="Complete your profile"
            text="Add a few details to make TwoHearts feel more personal."
            icon={<IconFileText size={16} />}
          />
        </div>
      )}
    </SettingsScreen>
  );
}
