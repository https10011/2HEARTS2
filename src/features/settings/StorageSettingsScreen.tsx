/**
 * Storage Settings (Phase 19 — roadmap screen 86).
 *
 * Honest local-storage picture + management, through the Phase 19
 * DataManagementService: per-feature row counts, stored media bytes,
 * pending local notifications. "Clear Cache" removes only unreferenced
 * (temporary) media. "Clear Local Data" is the destructive full reset —
 * double-confirmed in the UI, then performed by the service (domain rows,
 * media files, notifications, app-lock PIN, settings → onboarding fresh).
 * Everything stays on-device; nothing is uploaded anywhere.
 */

import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Modal } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';
import type { StorageReport } from '../../services/maintenance/dataManagementService.ts';
import { SettingsScreen, SettingRow, InfoCard } from './settingsUi.tsx';

/** Human labels for known domain tables; anything else falls under Other. */
const TABLE_LABELS: Record<string, string> = {
  memories: 'Memories',
  memory_media: 'Memories',
  notes: 'Notes',
  timeline_events: 'Timeline',
  vault_items: 'Private Vault',
  reminders: 'Reminders',
  places: 'Our Places',
  mood_entries: 'Mood',
  period_entries: 'Period Tracker',
  period_settings: 'Period Tracker',
  profiles: 'Relationship',
  couple_relationship: 'Relationship',
  important_dates: 'Important Dates',
  notification_center: 'Notification Center',
};

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
}

export function StorageSettingsScreen() {
  const navigate = useNavigate();
  const [report, setReport] = useState<StorageReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [confirmCache, setConfirmCache] = useState(false);
  const [confirmReset, setConfirmReset] = useState(false);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(async () => {
    const service = coreServices.dataManagement;
    if (!service) {
      setError('Storage information is unavailable.');
      return;
    }
    try {
      setReport(await service.getStorageReport());
      setError(null);
    } catch (cause) {
      setError(safeUserMessage(cause));
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const clearCache = async () => {
    const service = coreServices.dataManagement;
    if (!service) return;
    setBusy(true);
    try {
      const result = await service.clearCache();
      setMessage(
        result.mediaFilesRemoved > 0
          ? `Removed ${result.mediaFilesRemoved} unreferenced media file${result.mediaFilesRemoved === 1 ? '' : 's'}.`
          : 'Nothing to clear — no temporary files found.',
      );
      setConfirmCache(false);
      await refresh();
    } catch (cause) {
      setError(safeUserMessage(cause));
    } finally {
      setBusy(false);
    }
  };

  const resetAll = async () => {
    const service = coreServices.dataManagement;
    if (!service) return;
    setBusy(true);
    try {
      await service.resetAllLocalData();
      // Settings now read 'fresh' onboarding — return to first-launch flow.
      navigate(RoutePath.onboardingWelcome, { replace: true });
    } catch (cause) {
      setError(safeUserMessage(cause));
      setBusy(false);
      setConfirmReset(false);
    }
  };

  const breakdown = (() => {
    if (!report) return [];
    const byLabel = new Map<string, number>();
    let other = 0;
    for (const { table, rows } of report.tables) {
      const label = TABLE_LABELS[table];
      if (rows === 0) continue;
      if (label) byLabel.set(label, (byLabel.get(label) ?? 0) + rows);
      else other += rows;
    }
    if (other > 0) byLabel.set('Other', other);
    return [...byLabel.entries()];
  })();

  return (
    <SettingsScreen title="Storage" backTo={RoutePath.appMoreSettings}>
      <div className="th-settings-info" style={{ marginBottom: 'var(--th-space-4)' }}>
        <div>
          <p className="th-settings-info__title">TwoHearts Storage</p>
          <p className="th-settings-info__text">
            {report
              ? `${formatBytes(report.mediaBytes)} of media · ${report.domainRows} saved item${report.domainRows === 1 ? '' : 's'} · ${report.pendingNotifications} scheduled reminder${report.pendingNotifications === 1 ? '' : 's'}`
              : 'Calculating space used on this device…'}
          </p>
        </div>
      </div>

      {error ? (
        <p role="alert" style={{ color: 'var(--th-color-error)', fontSize: 'var(--th-font-size-sm)' }}>
          {error}
        </p>
      ) : null}
      {message ? (
        <p role="status" style={{ color: 'var(--th-color-success)', fontSize: 'var(--th-font-size-sm)' }}>
          {message}
        </p>
      ) : null}

      {breakdown.length > 0 ? (
        <>
          <p className="th-settings-section">Storage Breakdown</p>
          <div className="th-settings-group">
            {breakdown.map(([label, rows]) => (
              <SettingRow
                key={label}
                label={label}
                static
                trailing={
                  <span>
                    {rows} {rows === 1 ? 'item' : 'items'}
                  </span>
                }
              />
            ))}
          </div>
        </>
      ) : null}

      <p className="th-settings-section">Local Data</p>
      <div className="th-settings-group">
        <SettingRow
          label="App Data"
          description="Your TwoHearts information is stored on this device."
          static
        />
        <SettingRow
          label="Clear Cache"
          description="Remove temporary files without deleting your saved data."
          onClick={() => setConfirmCache(true)}
        />
      </div>

      <p className="th-settings-section">Danger Zone</p>
      <div className="th-settings-group">
        <SettingRow
          label="Clear Local Data"
          description="Delete TwoHearts data stored on this device."
          danger
          onClick={() => setConfirmReset(true)}
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Your data stays on your device"
          text="TwoHearts V1 is designed to keep your saved app data local."
        />
      </div>

      {/* Clear cache confirmation */}
      <Modal open={confirmCache} onClose={() => setConfirmCache(false)} label="Clear cache">
        <h2 style={{ marginTop: 0 }}>Clear cache?</h2>
        <p style={{ color: 'var(--th-color-text-secondary)' }}>
          Removes temporary media files that are no longer referenced. Your memories, notes, and
          other saved data are not affected.
        </p>
        <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
          <Button variant="ghost" full onClick={() => setConfirmCache(false)} disabled={busy}>
            Cancel
          </Button>
          <Button variant="primary" full onClick={() => void clearCache()} disabled={busy}>
            {busy ? 'Clearing…' : 'Clear Cache'}
          </Button>
        </div>
      </Modal>

      {/* Full reset confirmation */}
      <Modal open={confirmReset} onClose={() => setConfirmReset(false)} label="Clear local data">
        <h2 style={{ marginTop: 0 }}>Clear all local data?</h2>
        <p style={{ color: 'var(--th-color-text-secondary)' }}>
          This permanently deletes everything TwoHearts stored on this device: profiles,
          relationship details, memories, notes, timeline events, reminders, places, mood and
          period entries, vault content, media files, scheduled reminders, your App Lock PIN, and
          all preferences. This cannot be undone.
        </p>
        <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
          <Button variant="ghost" full onClick={() => setConfirmReset(false)} disabled={busy}>
            Cancel
          </Button>
          <Button variant="primary" full onClick={() => void resetAll()} disabled={busy}>
            {busy ? 'Deleting…' : 'Delete Everything'}
          </Button>
        </div>
      </Modal>
    </SettingsScreen>
  );
}
