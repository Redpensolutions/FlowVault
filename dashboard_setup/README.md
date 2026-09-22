# FlowVault - Admin & User Telemetry Dashboard Setup

This folder contains the complete, standalone setup files for the FlowVault Admin & User Analytics Dashboard.

## Files in this Folder

- `admin_dashboard.html`: The standalone web dashboard file containing all UI, authentication gate, real-time telemetry metrics, user tables, search/filters, and CSV/JSON export tools.
- `index.html`: Web-server ready index file for direct hosting.
- `start_dashboard.sh`: Quick local launcher script.

## Setup & Hosting Options

### Option 1: Host on Your Domain (e.g. `redpensolutions.in`)
1. Upload `index.html` (or `admin_dashboard.html`) to your web server (Apache, Nginx, cPanel, Firebase Hosting, GitHub Pages, or Netlify).
2. Configure SSL/HTTPS for secure access.
3. Access the dashboard via your browser: `https://yourdomain.com/dashboard/`

### Option 2: Run Locally
You can run a local HTTP server using Python or Node.js:

```bash
# Using Python 3
cd dashboard_setup
python3 -m http.server 8080

# Or run the included script:
./start_dashboard.sh
```
Then open your browser to `http://localhost:8080`.

### Security & Authentication
- The dashboard is protected by an admin passcode gate.
- Default Admin Passcode: `FlowVaultAdmin2025`
- You can change the passcode by editing the `ADMIN_PASSCODE` variable in `admin_dashboard.html`.
- For enterprise deployments, place this behind your domain's SSO or HTTP Basic Authentication.

### Features
- Real-time registered users list & session login tracking
- Active loans (Given & Taken) with live outstanding balances
- Total capital managed across Virtual Accounts
- Filter by authentication provider (Google OAuth, Guest, Mobile)
- Export complete user telemetry and debt summaries to CSV or JSON
