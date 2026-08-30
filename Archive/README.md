# TwoHearts Archive

This folder is the designated destination for code and assets that are no longer
actively used after migration. Nothing is archived yet — this folder exists as
a placeholder for future migration work.

## When to Archive

During migration, move old implementations here (NOT delete them) when:

1. A feature has been completely rewritten in the new stack
2. The old code is no longer imported by any active code
3. The old code might be useful as reference for the new implementation

## What Should Be Archived (Future)

After migration replaces old systems, the following will be moved here:

### Code
- Old component implementations (after new component library is complete)
- Old feature screens (after new implementations are verified)
- Old service implementations (after new services are tested)
- Old repository implementations (after new data layer is verified)
- Old CSS files (after new styling system is in place)
- Old test files (after new tests are written)

### Assets
- Old SVG assets that are replaced by new asset pipeline
- Old onboarding illustrations (after new ones are created)

### Documentation
- Historical stage reports (STAGE-*.md) — preserved as development records
- Old directive files — preserved as context for what was planned

## What Should NOT Be Archived

- Brand SVG assets (branding/, decorations/) — these are canonical
- Configuration files still in use
- Test files still relevant to the new stack
- Any file still imported by active code

## Archive Rules

1. **Verify before archiving**: Check that no active code imports the file
2. **Document why**: Each archived item should have a note explaining why
3. **Keep readable**: Archived code should be understandable for reference
4. **No hasty moves**: When uncertain, mark for review instead of archiving
5. **Preserve git history**: Archiving (moving files) preserves git history; do not rewrite history
