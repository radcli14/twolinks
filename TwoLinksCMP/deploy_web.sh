#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_OUTPUT_DIR="$SCRIPT_DIR/composeApp/build/dist/wasmJs/productionExecutable"
LANDING_PAGE_DIR="$SCRIPT_DIR/landing_page"
WORKTREE_DIR=$(mktemp -d)

# Always clean up the worktree on exit, whether successful or not
cleanup() {
    git -C "$REPO_ROOT" worktree remove --force "$WORKTREE_DIR" 2>/dev/null || true
    rm -rf "$WORKTREE_DIR" 2>/dev/null || true
}
trap cleanup EXIT

echo "Starting web deployment process..."

# Step 1: Build the Compose Multiplatform Web application
# Set CLEAN_BUILD=true to force a clean before building (slower but safe)
if [ "${CLEAN_BUILD:-false}" == "true" ]; then
    echo "Cleaning build..."
    cd "$SCRIPT_DIR" && ./gradlew clean
fi
echo "Building the wasm application..."
cd "$SCRIPT_DIR" && ./gradlew :composeApp:wasmJsBrowserDistribution

if [ ! -d "$BUILD_OUTPUT_DIR" ]; then
    echo "Error: Build output directory not found: $BUILD_OUTPUT_DIR"
    exit 1
fi

# Step 2: Ensure working copy is clean
if ! git -C "$REPO_ROOT" diff-index --quiet HEAD --; then
    echo "Error: You have uncommitted changes. Please commit or stash them before deploying."
    exit 1
fi

# Step 3: Add a worktree on the web branch (create orphan branch if it doesn't exist yet)
echo "Setting up git worktree..."
git -C "$REPO_ROOT" worktree prune
if ! git -C "$REPO_ROOT" show-ref --verify --quiet refs/heads/web; then
    if git -C "$REPO_ROOT" ls-remote --exit-code --heads origin web > /dev/null 2>&1; then
        echo "Fetching web branch from remote..."
        git -C "$REPO_ROOT" fetch origin web:web
        git -C "$REPO_ROOT" worktree add "$WORKTREE_DIR" web
    else
        echo "Creating new orphan web branch..."
        git -C "$REPO_ROOT" worktree add --orphan -b web "$WORKTREE_DIR"
    fi
else
    git -C "$REPO_ROOT" worktree add "$WORKTREE_DIR" web
fi

# Step 4: Sync app build output into app/ (rsync deletes stale files, preserves unchanged ones)
echo "Syncing web app build to app/..."
mkdir -p "$WORKTREE_DIR/app"
rsync -av --delete "$BUILD_OUTPUT_DIR/" "$WORKTREE_DIR/app/"

# Step 5: Sync landing page into the root (--exclude=app preserves the web build above)
if [ -d "$LANDING_PAGE_DIR" ]; then
    echo "Syncing landing page..."
    rsync -av --delete --exclude=".git" --exclude="app" "$LANDING_PAGE_DIR/" "$WORKTREE_DIR/"
else
    echo "Warning: Landing page directory not found. Landing page will not be deployed."
fi

# Step 6: Commit and push — only changed files appear in the diff
echo "Committing and pushing to web branch..."
git -C "$WORKTREE_DIR" add -A
git -C "$WORKTREE_DIR" commit -m "Deploy $(date)" || echo "No changes to commit"
git -C "$WORKTREE_DIR" push --force-with-lease origin web

echo "Web deployment completed successfully!"
