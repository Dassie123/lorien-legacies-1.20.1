**notes.md is gotchas only, with exactly one exception.** Read it at the start of a chat and
after a context clear, before doing anything else - it holds gotchas and hard-won constraints,
things that look like bugs but are deliberate. Write to it only for those: non-obvious root causes,
"why not the obvious approach" decisions, big architectural choices. Never as a running progress
log.

**The exception is the "Last shipped changelog" section at the bottom** - the changelog from the
most recent backup, which tells the next backup where its commit range starts. It qualifies because
it is fixed-size and replaced wholesale each backup, never appended to. Never delete it during a
trim. It lives there rather than here because CLAUDE.md is loaded into context every session and
this is not needed every session.

The Backup/Release protocol lives in copi's `BACKUP_PROTOCOL.md`
(`../crazy-overpowered-ideas-1.20.1/BACKUP_PROTOCOL.md`) and covers every mod in `IdeaProjects/`,
this one included - folder naming, timestamps, zip exclusions, the `_notes` folder and the two
OneDrive destinations. Read it before backing up or cutting a release, rather than improvising a
convention for this project.
