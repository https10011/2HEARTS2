/**
 * HomeScreen (Phase 5).
 *
 * Placeholder home screen — the destination after onboarding completes.
 * Phase 6+ will build the real home/greeting experience.
 */

import { useEffect, useState } from 'react';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService } from '../../services/relationship/relationshipService.ts';
import { Header } from '../../components/index.ts';
import { LoadingState } from '../../components/index.ts';

export function HomeScreen() {
  const [greeting, setGreeting] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const loadGreeting = async () => {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) {
        if (!cancelled) {
          setGreeting('Welcome to TwoHearts');
          setLoading(false);
        }
        return;
      }

      try {
        const summary = await svc.getSummary();
        if (cancelled) return;
        if (summary.owner) {
          setGreeting(`Hi, ${summary.owner.displayName}!`);
        } else {
          setGreeting('Welcome to TwoHearts');
        }
      } catch {
        if (!cancelled) setGreeting('Welcome to TwoHearts');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadGreeting();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return (
      <div className="th-screen">
        <Header title="TwoHearts" />
        <LoadingState label="Loading your space…" />
      </div>
    );
  }

  return (
    <div className="th-screen">
      <Header title="TwoHearts" />
      <div className="th-scroll" style={{ padding: 'var(--th-space-6)' }}>
        <div style={{ textAlign: 'center', padding: 'var(--th-space-8) 0' }}>
          <h2
            style={{
              fontFamily: 'var(--th-font-family-display)',
              fontSize: 'var(--th-font-size-2xl)',
              color: 'var(--th-color-text-primary)',
              marginBottom: 'var(--th-space-2)',
            }}
          >
            {greeting}
          </h2>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)' }}>
            Your private couple space is ready.
          </p>
        </div>
      </div>
    </div>
  );
}
