/**
 * Relationship Settings (Stage 15 — Settings + App Customization).
 *
 * Manage the partner profile (name, optional birthday) and the
 * relationship start date through RelationshipService; links to the
 * Important Dates manager (Phase 4 relationship foundation).
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input, IconHeart, IconCalendar, DatePicker } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';
import { SettingsScreen, SettingRow, InfoCard } from './settingsUi.tsx';

export function RelationshipSettingsScreen() {
  const [name, setName] = useState('');
  const [birthday, setBirthday] = useState('');
  const [startDate, setStartDate] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const relationship = coreServices.relationship;
    if (!relationship) return;
    Promise.all([relationship.getPartner(), relationship.getSummary()])
      .then(([partnerProfile, summary]) => {
        if (cancelled) return;
        if (partnerProfile) {
          setName(partnerProfile.displayName);
          setBirthday(partnerProfile.birthDate ?? '');
        }
        setStartDate(summary.startDate ?? '');
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
    };
  }, []);

  const handleSave = async () => {
    const relationship = coreServices.relationship;
    if (!relationship) {
      setError('Relationship service is unavailable.');
      return;
    }
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      if (name.trim() !== '') {
        await relationship.savePartner({
          displayName: name,
          birthDate: birthday.trim() === '' ? null : birthday,
        });
      }
      await relationship.setStartDate(startDate.trim() === '' ? null : startDate);
      setSaved(true);
    } catch (cause) {
      setError(safeUserMessage(cause));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SettingsScreen title="Relationship Settings" backTo={RoutePath.appMoreSettings}>
      <p style={{ marginTop: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
        Manage the details you share with TwoHearts.
      </p>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Special Someone
      </div>
      <div className="th-settings-group--enhanced">
        <div className="th-form-group" style={{ padding: 'var(--th-space-4)' }}>
          <label className="th-form-label" htmlFor="partner-name">
            Name
          </label>
          <Input
            id="partner-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Your special someone's name"
            maxLength={40}
          />
        </div>

        <div className="th-form-group" style={{ padding: '0 var(--th-space-4) var(--th-space-4)' }}>
          <label className="th-form-label" htmlFor="partner-birthday">
            Birthday (optional)
          </label>
          <DatePicker
            value={birthday}
            onChange={setBirthday}
            label="Partner birthday"
            placeholder="Tap to choose a date"
          />
        </div>

        <div className="th-form-group" style={{ padding: '0 var(--th-space-4) var(--th-space-4)' }}>
          <label className="th-form-label" htmlFor="start-date">
            Relationship start date
          </label>
          <DatePicker
            value={startDate}
            onChange={setStartDate}
            label="Relationship start date"
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

      <Button
        variant="primary"
        full
        onClick={handleSave}
        disabled={saving || (name.trim() === '' && startDate.trim() === '')}
      >
        {saving ? 'Saving…' : 'Save Changes'}
      </Button>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Relationship
      </div>
      <div className="th-settings-group--enhanced">
        <SettingRow
          to={RoutePath.appUsReminders}
          icon={<IconCalendar size={18} />}
          label="Important Dates"
          description="Manage anniversaries and special dates"
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Relationship information is private"
          text="Your relationship details are stored locally on this device."
          icon={<IconHeart size={16} />}
        />
      </div>
    </SettingsScreen>
  );
}
