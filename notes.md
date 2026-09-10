# Notes

Gotchas and hard-won constraints for this project - things that look like bugs but are deliberate,
non-obvious root causes, "why not the obvious approach" decisions, and big architectural choices.

**This is not a running progress log or a session diary.** Ordinary feature work that a future
session could fully understand by reading the current code gets no entry: new features, routine
fixes, renames, small additions. The test is whether a cold session would otherwise have to
re-derive or re-ask about it. When in doubt, leave it out - an entry that restates the code is
worse than no entry, because it dilutes the ones that matter and goes stale.

The "Last shipped changelog" section at the bottom is the one exception to that rule: it is a
fixed-size record, replaced wholesale each backup rather than appended to, and it is what tells the
next backup where its commit range starts. Never delete it as a progress-log violation.

## Last shipped changelog

The `changelog.md` from this project's most recent backup, verbatim - replaced wholesale each
backup, never appended to. It is a record, not a source: the next one is written fresh from the
commit range. Its purpose is that the last backup's range is knowable from this project alone,
without opening OneDrive or unpacking a zip. See copi's `BACKUP_PROTOCOL.md` for the routine.

_No backup recorded yet. Its starting point is the range line at the top of the
`changelog.md` inside this project's most recent backup folder in OneDrive._

```
10_09_26_12_35 - changes in b86b3f2..67e060d

Internal and documentation changes only; no gameplay changes.

This project now keeps its own last shipped changelog in its own notes.md, so backing
up another mod can no longer overwrite its record. notes.md was created for it, and
states that this changelog is the one exception to its gotchas-only rule.
```
