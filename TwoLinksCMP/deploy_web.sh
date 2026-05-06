#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "Starting web deployment process..."

# Step 1: Build the Compose Multiplatform Web application
echo "Building the wasm application..."
./gradlew clean
./gradlew :composeApp:wasmJsBrowserDistribution

# Store the path to the build output
BUILD_OUTPUT_DIR="composeApp/build/dist/wasmJs/productionExecutable"

# Make sure the build output exists
if [ ! -d "$BUILD_OUTPUT_DIR" ]; then
    echo "Error: Build output directory $BUILD_OUTPUT_DIR not found!"
    exit 1
fi

# Get the absolute path of the repository root and build output
REPO_ROOT=$(git rev-parse --show-toplevel)
BUILD_OUTPUT_ABS_PATH="$(pwd)/$BUILD_OUTPUT_DIR"

# Store current branch name
CURRENT_BRANCH=$(git branch --show-current)

# If we are somehow already on the web branch, warn and exit
if [ "$CURRENT_BRANCH" == "web" ]; then
    echo "Error: You are currently on the 'web' branch. Please run this script from your main working branch."
    exit 1
fi

# Step 2: Make sure the working directory is clean
if ! git diff-index --quiet HEAD --; then
    echo "Error: You have uncommitted changes. Please commit or stash them before deploying."
    exit 1
fi

# Step 3: Copy the build output to a temporary directory
TEMP_DIR=$(mktemp -d)
cp -r "$BUILD_OUTPUT_ABS_PATH/"* "$TEMP_DIR/"

echo "Build output copied to temporary directory."

# Step 4: Checkout the web branch
echo "Checking out web branch..."
if git show-ref --verify --quiet refs/heads/web; then
    git checkout web
else
    echo "Web branch not found locally. Trying to fetch and checkout..."
    if git ls-remote --exit-code --heads origin web; then
        git checkout web
    else
        echo "Web branch not found remotely either. Creating an orphan branch..."
        git checkout --orphan web
    fi
fi

# Step 5: Go to repository root and clear it
cd "$REPO_ROOT"
# Remove all files from Git's index and working directory to ensure it's clean
git rm -rf . || true

# Step 6: Create 'app' directory and copy files
echo "Setting up 'app' directory..."
mkdir -p app
cp -r "$TEMP_DIR/"* app/

# Add .nojekyll to prevent GitHub Pages from ignoring files with underscores
touch .nojekyll

# Cleanup temporary directory
rm -rf "$TEMP_DIR"

# Step 7: Add, commit, and push
echo "Committing and pushing to web branch..."
git add app .nojekyll
git commit -m "Deploy web build to app/ $(date)" || echo "No changes to commit"
git push origin web || git push -u origin web

# Step 8: Return to original branch
echo "Returning to $CURRENT_BRANCH branch..."
git checkout "$CURRENT_BRANCH"

echo "Web deployment completed successfully! Your app should be available at your GitHub Pages URL shortly."
