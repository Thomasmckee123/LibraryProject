#!/usr/bin/env bash
# PreToolUse(Bash): refuse commits made directly on main.
# Enforces branch-per-unit-of-work mechanically.
set -uo pipefail

cmd=$(jq -r '.tool_input.command // empty')
case "$cmd" in
  *"git commit"*) ;;
  *) exit 0 ;;
esac

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 0
branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null) || exit 0

if [ "$branch" = "main" ]; then
  echo "Refusing to commit on main. Start a branch first:" >&2
  echo "  git checkout -b feature/<short-name>" >&2
  exit 2
fi
exit 0
