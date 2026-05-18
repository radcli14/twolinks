#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LANDING_PAGE_DIR="$SCRIPT_DIR/landing_page"
WORKTREE_DIR=$(mktemp -d)

# Always clean up the worktree on exit, whether successful or not
cleanup() {
    git -C "$REPO_ROOT" worktree remove --force "$WORKTREE_DIR" 2>/dev/null || true
    rm -rf "$WORKTREE_DIR" 2>/dev/null || true
}
trap cleanup EXIT

echo "Starting lightweight landing page deployment..."

# Step 1: Ensure working copy is clean
if ! git -C "$REPO_ROOT" diff-index --quiet HEAD --; then
    echo "Error: You have uncommitted changes. Please commit or stash them before deploying."
    exit 1
fi

# Step 2: Verify prerequisites
if ! git -C "$REPO_ROOT" show-ref --verify --quiet refs/heads/web; then
    echo "Error: Web branch not found locally. Please run deploy_web.sh at least once first."
    exit 1
fi

if [ ! -d "$LANDING_PAGE_DIR" ]; then
    echo "Error: Landing page directory not found at $LANDING_PAGE_DIR"
    exit 1
fi

# Step 3: Add a worktree on the web branch (no branch switching needed)
echo "Adding git worktree for web branch..."
git -C "$REPO_ROOT" worktree prune
git -C "$REPO_ROOT" worktree add "$WORKTREE_DIR" web

# Step 4: Sync landing page files (--exclude=app preserves the existing web build)
echo "Syncing landing page files..."
rsync -av --delete --exclude=".git" --exclude="app" "$LANDING_PAGE_DIR/" "$WORKTREE_DIR/"

# Step 5: Commit and push — only changed files appear in the diff
echo "Committing and pushing to web branch..."
git -C "$WORKTREE_DIR" add -A
git -C "$WORKTREE_DIR" commit -m "Deploy landing page $(date)" || echo "No changes to commit"
git -C "$WORKTREE_DIR" push --force-with-lease origin web

echo "Landing page deployment completed successfully!"
