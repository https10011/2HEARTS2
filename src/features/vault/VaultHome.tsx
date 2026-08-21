/**
 * VaultHome (Phase 17).
 *
 * Displays vault content in a grid/list. Shows empty state when vault is empty.
 * All content is protected — must only show when vault is unlocked.
 * Uses real persisted data via VaultService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import type { VaultService } from '../../services/vault/vaultService.ts';
import { IconPlus, IconLock } from '../../components/index.ts';
import type { VaultItem, VaultContentType } from '../../data/vault/vaultTypes.ts';
import { CONTENT_TYPE_META, CONTENT_TYPE_ORDER } from './contentTypeMeta.tsx';

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
          <p>Loading vault...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Private Vault</h1>
        <button
          className="th-btn th-btn--primary th-btn--sm"
          onClick={() => navigate(RoutePath.appVaultAdd)}
          style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}
        >
          <IconPlus size={16} />
          Add
        </button>
      </div>

      {/* Filter tabs */}
      <div style={{ display: 'flex', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-4)', overflowX: 'auto' }}>
        {(['all', ...CONTENT_TYPE_ORDER] as const).map((type) => (
          <button
            key={type}
            className={`th-btn th-btn--sm ${filter === type ? 'th-btn--primary' : 'th-btn--outline'}`}
            onClick={() => setFilter(type)}
            style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            {type === 'all' ? 'All' : CONTENT_TYPE_META[type].label}
          </button>
        ))}
      </div>

      {/* Empty state */}
      {items.length === 0 && (
        <div className="th-card" style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ marginBottom: 'var(--th-space-2)', color: 'var(--th-color-rose-muted)' }}>
            <IconLock size={48} />
          </div>
          <h3 style={{ marginBottom: 'var(--th-space-2)' }}>
            {filter === 'all' ? 'Vault is empty' : `No ${CONTENT_TYPE_META[filter].label} items`}
          </h3>
          <p style={{ color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
            Add private photos, videos, notes, or files to your secure vault.
          </p>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appVaultAdd)}
          >
            Add Content
          </button>
        </div>
      )}

      {/* Vault items grid */}
      {items.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 'var(--th-space-3)' }}>
          {items.map((item) => (
            <button
              key={item.id}
              className="th-card th-card--clickable"
              onClick={() => navigate(RoutePath.appVaultDetail.replace(':itemId', item.id))}
              style={{ padding: 'var(--th-space-3)', textAlign: 'left' }}
            >
              {(() => {
                const Meta = CONTENT_TYPE_META[item.contentType];
                return (
                  <div style={{ marginBottom: 'var(--th-space-2)', color: 'var(--th-color-burgundy)' }}>
                    <Meta.Icon size={28} />
                  </div>
                );
              })()}
              <div style={{ fontWeight: 500, fontSize: 'var(--th-font-size-sm)', marginBottom: 'var(--th-space-1)' }}>
                {item.title}
              </div>
              <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                {new Date(item.createdAt).toLocaleDateString()}
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
