# Git Commit Instructions

## Commit Message Format

Please follow the commit message format below for all commits:

```
[{YOU_TRACK_ISSUE_ID}] <type>: <short description>
```

### Fields:
- **[{YOU_TRACK_ISSUE_ID}]**: Replace this with the relevant YouTrack issue ID (e.g., `[PROJ-123]`).
- **<type>**: Specify the type of change. Use one of the following:
  - `chore`: For routine tasks like refactoring, dependency updates, etc.
  - `feature`: For new features.
  - `docs`: For documentation updates.
- **<short description>**: Provide a concise summary of the changes (max 50 characters).

### Examples:
- `[PROJ-123] feature: Add WebSocket support`
- `[PROJ-456] chore: Update dependencies`
- `[PROJ-789] docs: Improve README`

### Additional Notes:
- Use the imperative mood in the description (e.g., "Add" instead of "Added").
- Keep the description short and to the point.
- Ensure the commit message accurately reflects the changes made.
