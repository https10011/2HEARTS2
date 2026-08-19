import { Component, type ErrorInfo, type ReactNode } from 'react';
import { uiStore } from './uiState';

interface Props {
  children: ReactNode;
}
interface State {
  hasError: boolean;
}

/**
 * Global error boundary — graceful failure handling (MasterPrompt §53).
 *
 * Never exposes raw errors/stack traces to the user. Renders a calm,
 * TwoHearts-styled fallback and offers a recovery path.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // Surface a friendly message; the raw error is never shown to users.
    uiStore.setGlobalError('Something went wrong. Please try again.');
    // eslint-disable-next-line no-console
    console.error('TwoHearts error boundary:', error, info);
  }

  handleReset = () => {
    uiStore.setGlobalError(null);
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <main
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 'var(--th-space-4)',
            padding: 'var(--th-space-12) var(--th-space-6)',
            textAlign: 'center',
            minHeight: '100dvh',
          }}
        >
          <h2 style={{ color: 'var(--th-color-burgundy)' }}>TwoHearts</h2>
          <p style={{ color: 'var(--th-color-text-secondary)', maxWidth: '30ch' }}>
            Something went wrong. The app will try to recover.
          </p>
          <button
            type="button"
            onClick={this.handleReset}
            className="th-button th-button--primary"
          >
            Try again
          </button>
        </main>
      );
    }
    return this.props.children;
  }
}
