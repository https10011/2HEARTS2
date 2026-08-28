/**
 * VaultHome — premium vault content display (Stage 12).
 *
 * Hero band with lock icon + item count, filter chips, two-column card grid,
 * branded empty state, and privacy footer. Uses the centralized th-vault-*
 * CSS vocabulary. All data through VaultService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { VaultService } from '../../services/vault/vaultService.ts';
import { IconPlus, IconLock } from '../../components/index.ts';
import type { VaultItem, VaultContentType } from '../../data/vault/vaultTypes.ts';
import { CONTENT_TYPE_META } from './contentTypeMeta.tsx';
import { itemCountText, relativeVaultDate, VAULT_FILTER_OPTIONS } from './vaultPresentation.ts';

export function VaultHome({ service }: { service?: VaultService }) {
  const navigate = useNavigate();
  const [items, setItems] = useState<VaultItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<VaultContentType | 'all'>('all');

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      if (!service) {
        setItems([]);
        return;
      }
      const profileId = 'owner';
      const data = filter === 'all'
        ? await service.list(profileId)
        : await service.listByType(profileId, filter);
      setItems(data);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [service, filter]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading vault…</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Hero band */}
      <div className="th-vault-hero" role="banner" aria-label="Private Vault">
        <div className="th-vault-hero__icon th-scale-in" aria-hidden="true">
          <IconLock size={32} />
        </div>
        <h1 className="th-vault-hero__title">Private Vault</h1>
        <p className="th-vault-hero__subtitle">
          Your most private moments, protected locally
        </p>
        <span className="th-vault-hero__count" aria-label={`${items.length} items`}>
          {itemCountText(items.length)}
        </span>
      </div>

      {/* Filter chips */}
      <div className="th-vault-filters" role="tablist" aria-label="Filter vault items">
        {VAULT_FILTER_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            role="tab"
            aria-selected={filter === opt.value}
            className={`th-option-chip${filter === opt.value ? ' th-option-chip--active' : ''}`}
            onClick={() => setFilter(opt.value)}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {/* Empty state */}
      {items.length === 0 && (
        <div className="th-empty-emotional" style={{ marginTop: 'var(--th-space-4)' }}>
          <div className="th-empty-emotional__visual th-scale-in">
            <IconLock size={42} />
          </div>
          <h3 className="th-empty-emotional__title">
            {filter === 'all' ? 'Your vault is empty' : `No ${filter} items yet`}
          </h3>
          <p className="th-empty-emotional__message">
            A secure place for your most private moments together
          </p>
          <div className="th-empty-emotional__action">
            <button
              className="th-btn th-btn--primary"
              onClick={() => navigate(RoutePath.appVaultAdd)}
            >
              <IconPlus size={16} />
              Add First Item
            </button>
          </div>
        </div>
      )}

      {/* Vault items grid */}
      {items.length > 0 && (
        <div className="th-vault-grid" role="list">
          {items.map((item) => (
            <button
              key={item.id}
              className="th-vault-card"
              role="listitem"
              onClick={() => navigate(RoutePath.appVaultDetail.replace(':itemId', item.id))}
              aria-label={`${item.title} — ${CONTENT_TYPE_META[item.contentType].label}`}
            >
              <div className="th-vault-card__icon" aria-hidden="true">
                {(() => {
                  const Meta = CONTENT_TYPE_META[item.contentType];
                  return <Meta.Icon size={22} />;
                })()}
              </div>
              <div className="th-vault-card__title">{item.title}</div>
              <div className="th-vault-card__meta">
                <span className="th-vault-card__type-badge">
                  {CONTENT_TYPE_META[item.contentType].label}
                </span>
                <span>{relativeVaultDate(item.createdAt)}</span>
              </div>
            </button>
          ))}
        </div>
      )}

      {/* Floating add button when items exist */}
      {items.length > 0 && (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 'var(--th-space-5)' }}>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appVaultAdd)}
          >
            <IconPlus size={16} />
            Add Content
          </button>
        </div>
      )}

      {/* Security footer */}
      <div className="th-vault-footer">
        <IconLock size={12} className="th-vault-footer__icon" />
        <span>Stored locally on this device only</span>
      </div>
    </div>
  );
}
