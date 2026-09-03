#!/usr/bin/env bash
# Stop: advisory note on where the current unit of work stands.
# Never blocks - a blocking Stop hook can loop forever.
set -uo pipefail

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 0
branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null) || exit 0
msg=""

if [ "$branch" = "main" ]; then
  [ -n "$(git status --porcelain | head -1)" ] &&
    msg="Uncommitted work on main. Branch it before committing."
elif ! git rev-parse --verify "origin/$branch" >/dev/null 2>&1; then
  msg="Branch '$branch' is not pushed. git push -u origin $branch"
elif [ -n "$(git log --oneline "origin/$branch..$branch" 2>/dev/null)" ]; then
  msg="Branch '$branch' has unpushed commits."
elif ! gh pr view --json number >/dev/null 2>&1; then
  msg="Branch '$branch' is pushed but has no PR yet. gh pr create --fill"
fi

[ -n "$msg" ] && jq -n --arg m "$msg" '{systemMessage: $m}'
exit 0
