#!/bin/bash
set -e

# Setup directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CURRENT_BRANCH=$(git branch --show-current)

echo "Starting lightweight landing page deployment..."

# Step 1: Ensure working copy is clean
if ! git diff-index --quiet HEAD --; then
    echo "Error: You have uncommitted changes. Please commit or stash them before deploying."
    exit 1
fi

# Step 2: Copy landing page to a temporary directory
if [ -d "$REPO_ROOT/TwoLinksCMP/landing_page" ]; then
    TEMP_LANDING_DIR=$(mktemp -d)
    cp -r "$REPO_ROOT/TwoLinksCMP/landing_page/"* "$TEMP_LANDING_DIR/"
    echo "Landing page files copied to temporary directory."
else
    echo "Error: Landing page directory not found at $REPO_ROOT/TwoLinksCMP/landing_page."
    exit 1
fi

# Step 3: Checkout the web branch
echo "Checking out web branch..."
if git show-ref --verify --quiet refs/heads/web; then
    git checkout -f web
else
    echo "Error: Web branch not found locally. Please run deploy_web.sh at least once first to initialize the web branch."
    rm -rf "$TEMP_LANDING_DIR"
    exit 1
fi

# Step 4: Remove existing landing page files (preserving 'app' folder)
echo "Clearing existing landing page files (preserving app/)..."
git rm -f _config.yml index.html main.scss Gemfile CNAME 2>/dev/null || true
git rm -rf _includes _layouts _pages _sass assets 2>/dev/null || true

# Step 5: Copy new landing page files
echo "Copying new landing page files..."
cp -r "$TEMP_LANDING_DIR/"* .

# Cleanup temporary directory
rm -rf "$TEMP_LANDING_DIR"

# Step 6: Add and commit changes
echo "Committing and pushing to web branch..."
if [ -f "_config.yml" ]; then
    git add _config.yml index.html _includes _layouts _pages _sass main.scss assets Gemfile CNAME
fi
git commit -m "Deploy landing page ONLY $(date)" || echo "No changes to commit"
git push --force origin web

# Step 7: Return to original branch
echo "Returning to $CURRENT_BRANCH branch..."
git checkout "$CURRENT_BRANCH"

echo "Landing page deployment completed successfully!"
