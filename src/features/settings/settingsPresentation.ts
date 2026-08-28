/**
 * Stage 15 — Settings presentation helpers.
 *
 * Pure-function helpers for the Settings visual productization:
 * theme labels, text size descriptions, status labels, section metadata.
 * No DOM, no sql.js, no mocks.
 */

import type { ThemeMode } from '../../core/appSettings.ts';
import type { TextSizeKey } from '../../theme/tokens.ts';

// ---------------------------------------------------------------------------
// Theme presentation
// ---------------------------------------------------------------------------

export interface ThemeOption {
  value: ThemeMode;
  label: string;
  description: string;
  preview: string;
}

export const THEME_OPTIONS: ThemeOption[] = [
  { value: 'light', label: 'Light', description: 'Warm and bright', preview: 'th-theme-preview--light' },
  { value: 'dark', label: 'Dark', description: 'Soft and low-light', preview: 'th-theme-preview--dark' },
  { value: 'system', label: 'System', description: 'Follow your device', preview: 'th-theme-preview--system' },
];

export function themeLabel(mode: ThemeMode): string {
  return THEME_OPTIONS.find((o) => o.value === mode)?.label ?? mode;
}

export function themeDescription(mode: ThemeMode): string {
  return THEME_OPTIONS.find((o) => o.value === mode)?.description ?? '';
}

// ---------------------------------------------------------------------------
// Text size presentation
// ---------------------------------------------------------------------------

export interface TextSizeOption {
  value: TextSizeKey;
  label: string;
  description: string;
}

export const TEXT_SIZE_OPTIONS: TextSizeOption[] = [
  { value: 'small', label: 'Small', description: 'Compact text' },
  { value: 'default', label: 'Default', description: 'Standard size' },
  { value: 'large', label: 'Large', description: 'Easier to read' },
  { value: 'extra-large', label: 'Extra Large', description: 'Maximum readability' },
];

export function textSizeLabel(size: TextSizeKey): string {
  return TEXT_SIZE_OPTIONS.find((o) => o.value === size)?.label ?? size;
}

export function textSizeDescription(size: TextSizeKey): string {
  return TEXT_SIZE_OPTIONS.find((o) => o.value === size)?.description ?? '';
}

// ---------------------------------------------------------------------------
// Motion presentation
// ---------------------------------------------------------------------------

export function motionLabel(reduceMotion: boolean): string {
  return reduceMotion ? 'Reduced' : 'Standard';
}

export function motionDescription(reduceMotion: boolean): string {
  return reduceMotion
    ? 'Simpler animations for a calmer experience'
    : 'Full animations and transitions';
}

// ---------------------------------------------------------------------------
// Notification status
// ---------------------------------------------------------------------------

export type PermissionStatus = 'granted' | 'denied' | 'prompt' | 'unavailable';

export function permissionStatusLabel(status: PermissionStatus): string {
  switch (status) {
    case 'granted': return 'Allowed';
    case 'denied': return 'Blocked';
    case 'prompt': return 'Not yet requested';
    case 'unavailable': return 'Not available';
  }
}

export function permissionStatusDescription(status: PermissionStatus): string {
  switch (status) {
    case 'granted': return 'TwoHearts can send notifications on this device.';
    case 'denied': return 'Notifications are blocked in your device settings.';
    case 'prompt': return 'TwoHearts has not asked for notification permission yet.';
    case 'unavailable': return 'Notifications are not available on this device.';
  }
}

// ---------------------------------------------------------------------------
// App Lock presentation
// ---------------------------------------------------------------------------

export function lockStatusLabel(enabled: boolean): string {
  return enabled ? 'Protected' : 'Unprotected';
}

export function lockMethodLabel(): string {
  return 'PIN';
}

// ---------------------------------------------------------------------------
// Section metadata
// ---------------------------------------------------------------------------

export interface SettingsSectionMeta {
  id: string;
  title: string;
  description: string;
}

export const SETTINGS_SECTIONS: SettingsSectionMeta[] = [
  { id: 'personal', title: 'Personal', description: 'Your profile and relationship' },
  { id: 'experience', title: 'Experience', description: 'Theme, text size, and motion' },
  { id: 'notifications', title: 'Notifications', description: 'Reminders and alerts' },
  { id: 'security', title: 'Privacy & Security', description: 'Protect your space' },
  { id: 'storage', title: 'Storage', description: 'Manage local data' },
  { id: 'about', title: 'About', description: 'Version and credits' },
];

// ---------------------------------------------------------------------------
// Info card messages
// ---------------------------------------------------------------------------

export function privacyInfoTitle(): string {
  return 'Your data stays with you';
}

export function privacyInfoText(): string {
  return 'TwoHearts stores your app data locally on this device. Nothing is sent to a server.';
}

export function securityInfoTitle(): string {
  return 'Private by design';
}

export function securityInfoText(): string {
  return 'Your App Lock settings stay on this device. PIN material never leaves your phone.';
}
