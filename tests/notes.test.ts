/**
 * Phase 8 — Notes Tests
 *
 * Tests the note data model, repository CRUD, service layer,
 * validation, migration, search provider, and route structure.
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import {
  initializeDatabase,
  finalizeDatabaseForTests,
  getDatabase,
} from '../src/data/database/connection.ts';
import { NoteRepository } from '../src/repositories/noteRepository.ts';
import { NoteService } from '../src/services/note/noteService.ts';
import { NoteSearchProvider } from '../src/services/search/noteSearchProvider.ts';
import { searchEngine } from '../src/services/search/searchEngine.ts';
import { noteSerializer, type Note, type NoteCategory } from '../src/data/note/noteTypes.ts';
import { RoutePath } from '../src/navigation/routes.ts';
import { PERSISTENCE_CONFIG } from '../src/config/persistence.ts';

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

// ---------------------------------------------------------------------------
// Route structure tests
// ---------------------------------------------------------------------------

describe('Phase 8 routes', () => {
  it('defines note routes', () => {
    assert.equal(RoutePath.appNotes, '/app/notes');
    assert.equal(RoutePath.appNotesAdd, '/app/notes/add');
    assert.ok(RoutePath.appNotesDetail.startsWith('/app/notes/'));
    assert.ok(RoutePath.appNotesEdit.startsWith('/app/notes/'));
  });

  it('note routes follow app nesting convention', () => {
    assert.ok(RoutePath.appNotes.startsWith('/app/'));
    assert.ok(RoutePath.appNotesAdd.startsWith('/app/'));
  });
});

// ---------------------------------------------------------------------------
// Migration tests
// ---------------------------------------------------------------------------

describe('Notes migration', () => {
  before(async () => {
    await initializeDatabase();
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('schema version is 6 after migration', () => {
    assert.equal(PERSISTENCE_CONFIG.schemaVersion, 10);
  });

  it('creates notes table', async () => {
    const db = await getDatabase();
    const tables = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'notes'",
    );
    assert.equal(tables.length, 1);
  });

  it('notes table has expected columns', async () => {
    const db = await getDatabase();
    const columns = await db.query<{ name: string }>(
      "PRAGMA table_info(notes)",
    );
    const colNames = columns.map((c) => c.name);
    assert.ok(colNames.includes('id'));
    assert.ok(colNames.includes('title'));
    assert.ok(colNames.includes('content'));
    assert.ok(colNames.includes('category'));
    assert.ok(colNames.includes('created_at'));
    assert.ok(colNames.includes('updated_at'));
    assert.ok(colNames.includes('deleted_at'));
  });

  it('notes table has indexes', async () => {
    const db = await getDatabase();
    const indexes = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'notes'",
    );
    const idxNames = indexes.map((i) => i.name);
    assert.ok(idxNames.includes('idx_notes_category'));
    assert.ok(idxNames.includes('idx_notes_deleted'));
    assert.ok(idxNames.includes('idx_notes_updated'));
  });
});

// ---------------------------------------------------------------------------
// Serialization tests
// ---------------------------------------------------------------------------

describe('Note serialization', () => {
  it('noteSerializer round-trips domain -> params -> row', () => {
    const note: Note = {
      id: 'test-note-id',
      title: 'Test Note',
      content: 'Some content here',
      category: 'love-letter',
      createdAt: '2026-01-15T12:00:00.000Z',
      updatedAt: '2026-01-15T12:00:00.000Z',
      deletedAt: null,
    };

    const params = noteSerializer.toParams(note);
    const row: Record<string, unknown> = {};
    noteSerializer.columns.forEach((col, i) => {
      row[col] = params[i];
    });

    const deserialized = noteSerializer.fromRow(row);
    assert.equal(deserialized.id, note.id);
    assert.equal(deserialized.title, note.title);
    assert.equal(deserialized.content, note.content);
    assert.equal(deserialized.category, note.category);
    assert.equal(deserialized.createdAt, note.createdAt);
    assert.equal(deserialized.updatedAt, note.updatedAt);
    assert.equal(deserialized.deletedAt, note.deletedAt);
  });

  it('serializer handles empty content', () => {
    const row: Record<string, unknown> = {
      id: 'test-id',
      title: 'Note',
      content: '',
      category: 'general',
      created_at: '2026-01-15T12:00:00.000Z',
      updated_at: '2026-01-15T12:00:00.000Z',
      deleted_at: null,
    };
    const result = noteSerializer.fromRow(row);
    assert.equal(result.content, '');
    assert.equal(result.category, 'general');
  });
});

// ---------------------------------------------------------------------------
// Repository CRUD tests
// ---------------------------------------------------------------------------

describe('NoteRepository CRUD', () => {
  let repo: NoteRepository;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    repo = new NoteRepository(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates a note', async () => {
    const note = await repo.create({
      title: 'My Note',
      content: 'Hello world',
      category: 'general',
      deletedAt: null,
    });
    assert.ok(note.id);
    assert.equal(note.title, 'My Note');
    assert.equal(note.content, 'Hello world');
    assert.equal(note.category, 'general');
  });

  it('retrieves a note by id', async () => {
    const created = await repo.create({
      title: 'Get Test',
      content: 'Body',
      category: 'private',
      deletedAt: null,
    });
    const found = await repo.getById(created.id);
    assert.ok(found);
    assert.equal(found.title, 'Get Test');
    assert.equal(found.category, 'private');
  });

  it('lists notes ordered by updated_at desc', async () => {
    const notes = await repo.listNotes();
    assert.ok(notes.length >= 1);
    for (let i = 1; i < notes.length; i++) {
      assert.ok(notes[i - 1].updatedAt >= notes[i].updatedAt);
    }
  });

  it('filters by category', async () => {
    await repo.create({
      title: 'Shared Note',
      content: '',
      category: 'shared',
      deletedAt: null,
    });
    const shared = await repo.listByCategory('shared');
    assert.ok(shared.length >= 1);
    assert.ok(shared.every((n) => n.category === 'shared'));
  });

  it('searches notes by title', async () => {
    await repo.create({
      title: 'Unique Search Term XYZ',
      content: '',
      category: 'general',
      deletedAt: null,
    });
    const results = await repo.search('Unique Search Term XYZ');
    assert.ok(results.length >= 1);
    assert.ok(results.some((n) => n.title.includes('Unique Search Term XYZ')));
  });

  it('searches notes by content', async () => {
    await repo.create({
      title: 'Another Note',
      content: 'Contains the word purple elephant',
      category: 'general',
      deletedAt: null,
    });
    const results = await repo.search('purple elephant');
    assert.ok(results.length >= 1);
  });

  it('updates a note', async () => {
    const created = await repo.create({
      title: 'Original',
      content: '',
      category: 'general',
      deletedAt: null,
    });
    const updated = await repo.update(created.id, { title: 'Updated Title' });
    assert.equal(updated.title, 'Updated Title');
    assert.ok(updated.updatedAt);
  });

  it('soft-deletes a note', async () => {
    const created = await repo.create({
      title: 'To Delete',
      content: '',
      category: 'general',
      deletedAt: null,
    });
    const result = await repo.delete(created.id);
    assert.equal(result, true);
    const found = await repo.getById(created.id);
    assert.equal(found, null);
  });

  it('returns false when deleting non-existent note', async () => {
    const result = await repo.delete('non-existent-id');
    assert.equal(result, false);
  });

  it('counts active notes', async () => {
    const before = await repo.count();
    await repo.create({
      title: 'Count Test',
      content: '',
      category: 'general',
      deletedAt: null,
    });
    const after = await repo.count();
    assert.equal(after, before + 1);
  });

  it('counts by category', async () => {
    await repo.create({
      title: 'Gratitude Note',
      content: '',
      category: 'gratitude',
      deletedAt: null,
    });
    const count = await repo.countByCategory('gratitude');
    assert.ok(count >= 1);
  });

  it('excerpt truncates long content', () => {
    const short = NoteRepository.excerpt('Short', 120);
    assert.equal(short, 'Short');

    const long = NoteRepository.excerpt('A'.repeat(200), 120);
    assert.equal(long.length, 121); // 120 chars + ellipsis
    assert.ok(long.endsWith('…'));
  });
});

// ---------------------------------------------------------------------------
// NoteService tests
// ---------------------------------------------------------------------------

describe('NoteService', () => {
  let service: NoteService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    service = new NoteService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates a note via service', async () => {
    const result = await service.createNote({
      title: 'Service Note',
      content: 'Some content',
      category: 'love-letter',
    });
    assert.ok(result.id);
    assert.equal(result.title, 'Service Note');
    assert.equal(result.content, 'Some content');
    assert.equal(result.category, 'love-letter');
    assert.equal(result.excerpt, 'Some content');
  });

  it('validates empty title', () => {
    const result = service.validateInput({ title: '' });
    assert.equal(result.ok, false);
    assert.ok(result.errors.length > 0);
  });

  it('validates title too long', () => {
    const result = service.validateInput({ title: 'A'.repeat(201) });
    assert.equal(result.ok, false);
  });

  it('validates invalid category', () => {
    const result = service.validateInput({ title: 'Test', category: 'bogus' as NoteCategory });
    assert.equal(result.ok, false);
  });

  it('accepts valid input', () => {
    const result = service.validateInput({
      title: 'Valid Note',
      content: 'Content',
      category: 'general',
    });
    assert.equal(result.ok, true);
  });

  it('lists notes via service', async () => {
    await service.createNote({ title: 'List Note 1' });
    await service.createNote({ title: 'List Note 2' });
    const notes = await service.listNotes();
    assert.ok(notes.length >= 2);
  });

  it('gets a single note via service', async () => {
    const created = await service.createNote({ title: 'Get Service Note' });
    const found = await service.getNote(created.id);
    assert.equal(found.title, 'Get Service Note');
    assert.ok(typeof found.excerpt === 'string');
  });

  it('throws for missing note', async () => {
    try {
      await service.getNote('non-existent');
      assert.fail('Should have thrown');
    } catch (err) {
      assert.ok(err instanceof Error);
      assert.ok(err.message.includes('not-found'));
    }
  });

  it('updates a note via service', async () => {
    const created = await service.createNote({ title: 'Update Me' });
    const updated = await service.updateNote(created.id, { title: 'Updated!' });
    assert.equal(updated.title, 'Updated!');
  });

  it('deletes a note via service', async () => {
    const created = await service.createNote({ title: 'Delete Me' });
    const deleted = await service.deleteNote(created.id);
    assert.equal(deleted, true);
    try {
      await service.getNote(created.id);
      assert.fail('Should have thrown');
    } catch {
      // Expected
    }
  });

  it('handles delete of non-existent note gracefully', async () => {
    const result = await service.deleteNote('non-existent');
    assert.equal(result, false);
  });

  it('lists notes by category via service', async () => {
    await service.createNote({ title: 'Gratitude 1', category: 'gratitude' });
    const gratitude = await service.listByCategory('gratitude');
    assert.ok(gratitude.length >= 1);
    assert.ok(gratitude.every((n) => n.category === 'gratitude'));
  });

  it('gets counts by category', async () => {
    const counts = await service.getCounts();
    assert.ok(typeof counts['all'] === 'number');
    assert.ok(typeof counts['general'] === 'number');
    assert.ok(typeof counts['love-letter'] === 'number');
  });
});

// ---------------------------------------------------------------------------
// Search integration tests
// ---------------------------------------------------------------------------

describe('Note search integration', () => {
  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    // Create test data in this suite's own DB
    const repo = new NoteRepository(db, FIXED_CLOCK);
    await repo.create({ title: 'Unique Search Term XYZ', content: '', category: 'general', deletedAt: null });
    await repo.create({ title: 'Another Note', content: 'Contains the word purple elephant', category: 'general', deletedAt: null });
    const provider = new NoteSearchProvider(db);
    searchEngine.registerProvider(provider);
  });

  after(async () => {
    searchEngine.unregisterProvider('note');
    await finalizeDatabaseForTests();
  });

  it('search engine finds notes by title', async () => {
    const results = await searchEngine.search('Unique Search Term XYZ');
    const noteMatches = results.matches.filter((m) => m.kind === 'note');
    assert.ok(noteMatches.length >= 1);
  });

  it('search engine returns empty for non-matching query', async () => {
    const results = await searchEngine.search('zzz_no_match_xyz_123');
    assert.equal(results.matches.length, 0);
  });

  it('search provider is registered', () => {
    const kinds = searchEngine.registeredKinds();
    assert.ok(kinds.includes('note'));
  });
});

// ---------------------------------------------------------------------------
// Note content and edge cases
// ---------------------------------------------------------------------------

describe('Note edge cases', () => {
  let service: NoteService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    service = new NoteService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('handles notes with empty content', async () => {
    const note = await service.createNote({ title: 'Empty Content' });
    assert.equal(note.content, '');
    assert.equal(note.excerpt, '');
  });

  it('handles notes with very long content', async () => {
    const longContent = 'A'.repeat(10000);
    const note = await service.createNote({ title: 'Long Note', content: longContent });
    assert.equal(note.content.length, 10000);
    assert.ok(note.excerpt.length <= 121);
  });

  it('handles multiple category types', async () => {
    const categories: NoteCategory[] = ['general', 'shared', 'private', 'love-letter', 'gratitude', 'idea', 'reminder'];
    for (const cat of categories) {
      const note = await service.createNote({ title: `Note ${cat}`, category: cat });
      assert.equal(note.category, cat);
    }
  });

  it('default category is general', async () => {
    const note = await service.createNote({ title: 'No Category' });
    assert.equal(note.category, 'general');
  });

  it('default content is empty string', async () => {
    const note = await service.createNote({ title: 'No Content' });
    assert.equal(note.content, '');
  });
});
