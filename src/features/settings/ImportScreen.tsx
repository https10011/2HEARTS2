/**
 * ImportScreen (Stage 7 — Import / Data Portability System).
 *
 * Full import flow: file selection → validation → preview → confirm → import → results.
 * Supports TwoHearts JSON import files containing notes and reminders.
 * Profile photos and Vault items use their existing dedicated flows.
 */

import { useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  IconCheck,
  IconFile,
  IconFileText,
  IconInfo,
  OnboardingArt,
  RoseLilyDecoration,
} from '../../components/index.ts';
import {
  type ImportFile,
  type ImportNote,
  type ImportReminder,
  type ImportResult,
  type ValidationError,
  validateImportFile,
  executeImport,
} from '../../services/import/importService.ts';
import { useToast } from '../../components/toast.tsx';

type ImportStep = 'select' | 'preview' | 'importing' | 'result';

export function ImportScreen() {
  const navigate = useNavigate();
  const toast = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [step, setStep] = useState<ImportStep>('select');
  const [parsedFile, setParsedFile] = useState<ImportFile | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);
  const [importResult, setImportResult] = useState<ImportResult | null>(null);
  const [busy, setBusy] = useState(false);

  // -----------------------------------------------------------------------
  // File selection
  // -----------------------------------------------------------------------

  const handleFileSelect = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.name.endsWith('.json')) {
      setValidationErrors([{ field: 'file', message: 'Please select a JSON file.' }]);
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      try {
        const data = JSON.parse(reader.result as string);
        const result = validateImportFile(data);
        if (result.valid && result.parsed) {
          setParsedFile(result.parsed);
          setValidationErrors([]);
          setStep('preview');
        } else {
          setParsedFile(null);
          setValidationErrors(result.errors);
        }
      } catch {
        setParsedFile(null);
        setValidationErrors([{ field: 'file', message: 'Could not read the file. Make sure it is valid JSON.' }]);
      }
    };
    reader.readAsText(file);
    // Reset input so same file can be re-selected
    e.target.value = '';
  }, []);

  // -----------------------------------------------------------------------
  // Import execution
  // -----------------------------------------------------------------------

  const handleImport = useCallback(async () => {
    if (!parsedFile) return;
    setBusy(true);
    setStep('importing');
    try {
      const result = await executeImport(parsedFile);
      setImportResult(result);
      setStep('result');

      const totalImported = result.notes.imported + result.reminders.imported;
      const totalFailed = result.notes.failed + result.reminders.failed;

      if (totalFailed === 0 && totalImported > 0) {
        toast.success(`${totalImported} item${totalImported > 1 ? 's' : ''} imported successfully.`);
      } else if (totalImported > 0) {
        toast.info(`${totalImported} imported, ${totalFailed} failed.`);
      } else {
        toast.error('Import failed. Please check the file and try again.');
      }
    } catch (cause) {
      setStep('result');
      setImportResult({
        notes: { imported: 0, failed: 0, errors: [cause instanceof Error ? cause.message : 'Unknown error'] },
        reminders: { imported: 0, failed: 0, errors: [] },
      });
      toast.error('Import failed. Please check the file and try again.');
    } finally {
      setBusy(false);
    }
  }, [parsedFile, toast]);

  // -----------------------------------------------------------------------
  // Step: Select
  // -----------------------------------------------------------------------

  if (step === 'select') {
    return (
      <div className="th-screen th-import-screen">
        <header className="th-screen-header">
          <button className="th-icon-button" onClick={() => navigate(RoutePath.appMoreSettings)} aria-label="Back">
            <span className="th-icon-button__icon" aria-hidden="true">←</span>
          </button>
          <h1 className="th-screen-header__title">Import Data</h1>
          <div style={{ width: 44 }} />
        </header>

        <div className="th-import-content">
          <RoseLilyDecoration variant={5} size={100} position="top-right" opacity={0.08} />

          <div className="th-import-hero th-stagger-item">
            <div className="th-welcome-illustration" aria-hidden="true">
              <OnboardingArt variant="security-lock" size={64} />
            </div>
            <h2 className="th-import-hero__title">Bring your data in</h2>
            <p className="th-import-hero__description">
              Import notes and reminders from a TwoHearts export file.
              Everything stays on your device.
            </p>
          </div>

          <div className="th-card th-import-info-card th-stagger-item">
            <h3 className="th-import-info-card__title">
              <IconInfo size={16} /> Supported formats
            </h3>
            <ul className="th-import-info-card__list">
              <li className="th-import-info-card__item">
                <IconFileText size={14} />
                <span>Notes (JSON)</span>
              </li>
              <li className="th-import-info-card__item">
                <IconFileText size={14} />
                <span>Reminders (JSON)</span>
              </li>
              <li className="th-import-info-card__item">
                <IconFile size={14} />
                <span>Profile photos (via system picker)</span>
              </li>
              <li className="th-import-info-card__item">
                <IconFile size={14} />
                <span>Vault files (via Vault import)</span>
              </li>
            </ul>
            <p className="th-import-info-card__note">
              Notes and reminders must be in TwoHearts JSON format.
              Profile photos and Vault files have their own import flows.
            </p>
          </div>

          {validationErrors.length > 0 && (
            <div className="th-import-errors th-stagger-item" role="alert">
              {validationErrors.map((err, i) => (
                <p key={i} className="th-import-error">
                  <strong>{err.field}:</strong> {err.message}
                </p>
              ))}
            </div>
          )}

          <div className="th-import-actions th-stagger-item">
            <input
              ref={fileInputRef}
              type="file"
              accept=".json"
              onChange={handleFileSelect}
              style={{ display: 'none' }}
              aria-label="Select import file"
            />
            <Button variant="primary" full onClick={() => fileInputRef.current?.click()}>
              <IconFile size={16} />
              Select File
            </Button>
            <Button variant="ghost" full onClick={() => navigate(RoutePath.appMoreSettings)}>
              Cancel
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // -----------------------------------------------------------------------
  // Step: Preview
  // -----------------------------------------------------------------------

  if (step === 'preview' && parsedFile) {
    const notes = parsedFile.content.notes ?? [];
    const reminders = parsedFile.content.reminders ?? [];
    const totalItems = notes.length + reminders.length;

    return (
      <div className="th-screen th-import-screen">
        <header className="th-screen-header">
          <button className="th-icon-button" onClick={() => setStep('select')} aria-label="Back">
            <span className="th-icon-button__icon" aria-hidden="true">←</span>
          </button>
          <h1 className="th-screen-header__title">Preview Import</h1>
          <div style={{ width: 44 }} />
        </header>

        <div className="th-import-content">
          <div className="th-import-preview-summary th-stagger-item">
            <p className="th-import-preview-summary__text">
              Found <strong>{totalItems}</strong> item{totalItems !== 1 ? 's' : ''} to import:
            </p>
            {notes.length > 0 && (
              <span className="th-import-preview-badge">
                <IconFileText size={14} /> {notes.length} note{notes.length !== 1 ? 's' : ''}
              </span>
            )}
            {reminders.length > 0 && (
              <span className="th-import-preview-badge">
                <IconFileText size={14} /> {reminders.length} reminder{reminders.length !== 1 ? 's' : ''}
              </span>
            )}
          </div>

          {/* Notes preview */}
          {notes.length > 0 && (
            <div className="th-import-preview-section th-stagger-item">
              <h3 className="th-import-preview-section__title">Notes</h3>
              <div className="th-import-preview-list">
                {notes.slice(0, 10).map((note: ImportNote, i: number) => (
                  <div key={i} className="th-import-preview-item">
                    <span className="th-import-preview-item__title">{note.title}</span>
                    {note.category && (
                      <span className="th-import-preview-item__meta">{note.category}</span>
                    )}
                  </div>
                ))}
                {notes.length > 10 && (
                  <p className="th-import-preview-more">…and {notes.length - 10} more</p>
                )}
              </div>
            </div>
          )}

          {/* Reminders preview */}
          {reminders.length > 0 && (
            <div className="th-import-preview-section th-stagger-item">
              <h3 className="th-import-preview-section__title">Reminders</h3>
              <div className="th-import-preview-list">
                {reminders.slice(0, 10).map((reminder: ImportReminder, i: number) => (
                  <div key={i} className="th-import-preview-item">
                    <span className="th-import-preview-item__title">{reminder.title}</span>
                    <span className="th-import-preview-item__meta">
                      {reminder.scheduledDate} at {reminder.scheduledTime}
                    </span>
                  </div>
                ))}
                {reminders.length > 10 && (
                  <p className="th-import-preview-more">…and {reminders.length - 10} more</p>
                )}
              </div>
            </div>
          )}

          <div className="th-import-actions th-stagger-item">
            <Button variant="primary" full onClick={() => void handleImport()} disabled={busy || totalItems === 0}>
              {busy ? 'Importing…' : `Import ${totalItems} Item${totalItems !== 1 ? 's' : ''}`}
            </Button>
            <Button variant="ghost" full onClick={() => setStep('select')} disabled={busy}>
              Choose Different File
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // -----------------------------------------------------------------------
  // Step: Importing
  // -----------------------------------------------------------------------

  if (step === 'importing') {
    return (
      <div className="th-screen th-import-screen">
        <header className="th-screen-header">
          <div style={{ width: 44 }} />
          <h1 className="th-screen-header__title">Importing…</h1>
          <div style={{ width: 44 }} />
        </header>

        <div className="th-import-content" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flex: 1 }}>
          <div style={{ textAlign: 'center' }}>
            <div className="th-spinner" aria-label="Importing" />
            <p className="th-import-progress-text">Importing your data…</p>
          </div>
        </div>
      </div>
    );
  }

  // -----------------------------------------------------------------------
  // Step: Result
  // -----------------------------------------------------------------------

  if (step === 'result' && importResult) {
    const totalImported = importResult.notes.imported + importResult.reminders.imported;
    const totalFailed = importResult.notes.failed + importResult.reminders.failed;
    const allErrors = [...importResult.notes.errors, ...importResult.reminders.errors];
    const success = totalFailed === 0 && totalImported > 0;

    return (
      <div className="th-screen th-import-screen">
        <header className="th-screen-header">
          <div style={{ width: 44 }} />
          <h1 className="th-screen-header__title">Import Complete</h1>
          <div style={{ width: 44 }} />
        </header>

        <div className="th-import-content">
          <div className="th-import-result th-stagger-item">
            <div className="th-import-result__icon" aria-hidden="true">
              {success ? <IconCheck size={48} /> : <IconInfo size={48} />}
            </div>
            <h2 className="th-import-result__title">
              {success ? 'Import successful' : 'Import finished'}
            </h2>
            <p className="th-import-result__description">
              {totalImported > 0 && (
                <>Imported {totalImported} item{totalImported !== 1 ? 's' : ''}.</>
              )}
              {totalFailed > 0 && (
                <> {totalFailed} item{totalFailed !== 1 ? 's' : ''} failed.</>
              )}
              {totalImported === 0 && totalFailed === 0 && (
                <>No items were found in the file.</>
              )}
            </p>
          </div>

          {/* Per-category breakdown */}
          {(importResult.notes.imported > 0 || importResult.reminders.imported > 0) && (
            <div className="th-card th-import-result-card th-stagger-item">
              <h3 className="th-import-result-card__title">Imported</h3>
              <ul className="th-import-result-card__list">
                {importResult.notes.imported > 0 && (
                  <li className="th-import-result-card__item">
                    <IconFileText size={14} /> {importResult.notes.imported} note{importResult.notes.imported !== 1 ? 's' : ''}
                  </li>
                )}
                {importResult.reminders.imported > 0 && (
                  <li className="th-import-result-card__item">
                    <IconFileText size={14} /> {importResult.reminders.imported} reminder{importResult.reminders.imported !== 1 ? 's' : ''}
                  </li>
                )}
              </ul>
            </div>
          )}

          {/* Errors */}
          {allErrors.length > 0 && (
            <div className="th-card th-import-result-errors th-stagger-item" role="alert">
              <h3 className="th-import-result-errors__title">Errors</h3>
              <ul className="th-import-result-errors__list">
                {allErrors.map((err, i) => (
                  <li key={i} className="th-import-result-errors__item">{err}</li>
                ))}
              </ul>
            </div>
          )}

          <div className="th-import-actions th-stagger-item">
            <Button variant="primary" full onClick={() => navigate(RoutePath.appMoreSettings)}>
              Done
            </Button>
            <Button variant="ghost" full onClick={() => { setStep('select'); setParsedFile(null); setImportResult(null); setValidationErrors([]); }}>
              Import Another File
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return null;
}
