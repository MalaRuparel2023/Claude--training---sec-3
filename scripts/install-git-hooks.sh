#!/usr/bin/env bash
#
# Installs the repo's versioned git hooks into .git/hooks.
# Run once after cloning:  ./scripts/install-git-hooks.sh
#
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
SRC="$REPO_ROOT/scripts/git-hooks"
DEST="$REPO_ROOT/.git/hooks"

shopt -s nullglob
for hook in "$SRC"/*; do
  name="$(basename "$hook")"
  install -m 0755 "$hook" "$DEST/$name"
  echo "installed $name -> .git/hooks/$name"
done

echo "done. Bypass any hook for one commit with SKIP_HOOKS=1."
