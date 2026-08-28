/**
 * Styled-component-free design: components consume CSS custom properties
 * directly via className. This file exports class-name strings for the
 * most reused primitive styles so consumers stay consistent without a
 * CSS-in-JS dependency.
 *
 * These are intentionally framework-light to avoid dependency bloat
 * (MasterPrompt §14 Sustainability).
 */

export const componentClassNames = {
  button: 'th-button',
  buttonPrimary: 'th-button--primary',
  buttonSecondary: 'th-button--secondary',
  buttonGhost: 'th-button--ghost',
  buttonDanger: 'th-button--danger',
  buttonFull: 'th-button--full',
  card: 'th-card',
  input: 'th-input',
  header: 'th-header',
  iconButton: 'th-icon-button',
  divider: 'th-divider',
  emptyState: 'th-empty-state',
  loadingState: 'th-loading-state',
  // App shell (Phase 6)
  appShell: 'th-app-shell',
  appContent: 'th-app-content',
  bottomNav: 'th-bottom-nav',
  bottomNavItem: 'th-bottom-nav-item',
  bottomNavItemActive: 'th-bottom-nav-item--active',
  bottomNavLabel: 'th-bottom-nav-label',
  appHeader: 'th-app-header',
  featureCard: 'th-feature-card',
  moreItem: 'th-more-item',
  placeholder: 'th-placeholder',
} as const;
