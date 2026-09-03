#!/usr/bin/env bash
# PostToolUse(Write|Edit): run the suite for whichever Maven module was edited.
# Exit 2 hands the failure output back to Claude to fix.
set -uo pipefail

f=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty')
case "$f" in
  *.java) ;;
  *) exit 0 ;;
esac

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

# Pick the module from the edited path rather than assuming one.
case "$f" in
  */backend/*)         module="backend" ;;
  */library-tracker/*) module="library-tracker" ;;
  *)                   exit 0 ;;
esac

cd "$ROOT/$module" || exit 0

# Offline first - it is ~1s rather than several. On a cold cache (fresh clone,
# or a dependency that has never been fetched) Maven fails on resolution
# rather than on the tests, so retry online before reporting anything.
if out=$(mvn -o -q test 2>&1); then
  exit 0
fi

if grep -q "offline mode" <<<"$out"; then
  if out=$(mvn -q test 2>&1); then
    exit 0
  fi
fi

printf 'Tests failed in %s after editing %s\n\n%s\n' "$module" "$f" "$out" >&2
exit 2
