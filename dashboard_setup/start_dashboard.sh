#!/usr/bin/env bash
# Script to launch FlowVault Admin Dashboard locally
echo "Starting FlowVault Admin Dashboard server on http://localhost:8080 ..."
if command -v python3 &>/dev/null; then
    python3 -m http.server 8080 --directory "$(dirname "$0")"
elif command -v npx &>/dev/null; then
    npx serve "$(dirname "$0")" -l 8080
else
    echo "Please open admin_dashboard.html directly in your web browser."
fi
