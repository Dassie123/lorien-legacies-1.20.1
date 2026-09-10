If this project has a `notes.md`, read it at the start of a chat and after a context clear, before
doing anything else. It holds gotchas and hard-won constraints - things that look like bugs but are
deliberate. It also carries this project's last shipped changelog at the bottom - see there. Write to
it only for gotchas, non-obvious root causes, "why not the obvious approach" decisions and big
architectural choices - never as a running progress log.

The Backup/Release protocol lives in copi's `BACKUP_PROTOCOL.md`
(`../crazy-overpowered-ideas-1.20.1/BACKUP_PROTOCOL.md`) and covers every mod in `IdeaProjects/`,
this one included - folder naming, timestamps, zip exclusions, the `_notes` folder and the two
OneDrive destinations. Read it before backing up or cutting a release, rather than improvising a
convention for this project.
