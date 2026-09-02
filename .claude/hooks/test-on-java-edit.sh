#!/usr/bin/env bash
# PostToolUse(Write|Edit): run the suite whenever a .java file changes.
# Exit 2 hands the failure output back to Claude to fix.
set -uo pipefail

f=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty')
case "$f" in
  *.java) ;;
  *) exit 0 ;;
esac

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT/library-tracker" || exit 0

if ! out=$(mvn -o -q test 2>&1); then
  printf 'Tests failed after editing %s\n\n%s\n' "$f" "$out" >&2
  exit 2
fi
exit 0
