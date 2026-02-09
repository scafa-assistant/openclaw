#!/bin/bash
# OpenClaw Android Push Script
# Run this in Git Bash or WSL

cd ~/openclaw-backup

# Initialize git if not already done
if [ ! -d .git ]; then
    git init
    git remote add origin https://github.com/gigibeule/openclaw.git
fi

# Add all files
git add .

# Commit with descriptive message
git commit -m "Fix Android build: Add missing mipmap directories for all DPI levels

- Added mipmap-mdpi, xhdpi, xxhdpi, xxxhdpi, anydpi-v26
- Fixed AAPT resource linking error (Build #14-15)
- Ready for Build #16

Co-authored-by: ORACLE <oracle@openclaw.ai>"

# Push to main
git push -u origin main

echo "✅ Push complete! GitHub Actions will start Build #16 automatically."
echo "Check status at: https://github.com/gigibeule/openclaw/actions"
