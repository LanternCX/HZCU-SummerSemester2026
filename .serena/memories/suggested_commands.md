# Suggested Commands

- Inspect docs: `find docs/reference -type f | sort`.
- Search text: `rg -n "pattern" README.md docs/reference`.
- Check stale reference paths after doc cleanup: `rg -n "docs/Reference|docs/assets|docs/案例|docs/课程课件|system_architecture|personal-plan" README.md docs/reference || true`.
- Serena memory sanity check: `serena memories check` from project root.
- No build/test command exists yet; add commands here when Java project files are created.