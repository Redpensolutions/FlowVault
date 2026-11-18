import http.server
import io
import json
import os
import socketserver
import urllib.parse
import zipfile
from datetime import datetime, timedelta

PORT = 3000
FLUTTER_DIR = "/app/applet/flutter_app"

DATA = {
    "accounts": [
        {
            "id": 1,
            "accountNumber": "VA-HDFC-101",
            "name": "HDFC Current Account",
            "poolType": "Bank account",
            "balance": 450000.0,
            "currency": "₹",
            "desc": "Primary operational banking account",
        },
        {
            "id": 2,
            "accountNumber": "VA-COL-202",
            "name": "Daily Collections Pool",
            "poolType": "Collection Account",
            "balance": 182400.0,
            "currency": "₹",
            "desc": "Central receiving account for UPI & cash",
        },
        {
            "id": 3,
            "accountNumber": "VA-REC-303",
            "name": "Recovery & Arrears Reserve",
            "poolType": "Recovery Account",
            "balance": 64200.0,
            "currency": "₹",
            "desc": "Allocations from delayed accounts",
        },
        {
            "id": 4,
            "accountNumber": "VA-OPS-505",
            "name": "Lending Operations Reserve",
            "poolType": "Operations account",
            "balance": 820000.0,
            "currency": "₹",
            "desc": "High liquidity pool reserved for new loans",
        },
    ],
    "borrowers": [
        {
            "id": 1,
            "name": "Rajesh Kumar",
            "phone": "+91 98765 12340",
            "email": "rajesh.kumar@email.com",
            "rating": "A+",
            "notes": "Hardware shop owner, prompt monthly payer",
        },
        {
            "id": 2,
            "name": "Priya Sharma",
            "phone": "+91 98765 23450",
            "email": "priya.sharma@designs.in",
            "rating": "A",
            "notes": "Boutique entrepreneur, expansion funding",
        },
        {
            "id": 3,
            "name": "Suresh Patel",
            "phone": "+91 98765 34560",
            "email": "suresh.patel@logistics.in",
            "rating": "B",
            "notes": "Logistics fleet operator",
        },
        {
            "id": 4,
            "name": "Ananya Roy",
            "phone": "+91 98765 45670",
            "email": "ananya.roy@pharma.com",
            "rating": "A+",
            "notes": "Wholesale pharmacy distributor",
        },
        {
            "id": 5,
            "name": "Deepak Verma",
            "phone": "+91 98765 56780",
            "email": "deepak.verma@metals.org",
            "rating": "C",
            "notes": "Metal fabrication unit",
        },
    ],
    "loans": [
        {
            "id": 1,
            "loanNumber": "LN-2026-001",
            "borrowerName": "Rajesh Kumar",
            "principal": 150000.0,
            "rate": 14.5,
            "type": "SIMPLE",
            "period": "YEARLY",
            "term": 12,
            "due": "2026-12-15",
            "totalDue": 171750.0,
            "paid": 128800.0,
            "status": "ACTIVE",
            "direction": "GIVEN",
            "vaId": 1,
        },
        {
            "id": 2,
            "loanNumber": "LN-2026-002",
            "borrowerName": "Priya Sharma",
            "principal": 85000.0,
            "rate": 12.0,
            "type": "SIMPLE",
            "period": "YEARLY",
            "term": 6,
            "due": "2026-06-30",
            "totalDue": 90100.0,
            "paid": 90100.0,
            "status": "PAID",
            "direction": "GIVEN",
            "vaId": 1,
        },
        {
            "id": 3,
            "loanNumber": "LN-2026-003",
            "borrowerName": "Suresh Patel",
            "principal": 220000.0,
            "rate": 18.0,
            "type": "SIMPLE",
            "period": "YEARLY",
            "term": 24,
            "due": "2026-09-10",
            "totalDue": 299200.0,
            "paid": 45000.0,
            "status": "OVERDUE",
            "direction": "GIVEN",
            "vaId": 2,
        },
        {
            "id": 4,
            "loanNumber": "LN-2026-004",
            "borrowerName": "Ananya Roy",
            "principal": 310000.0,
            "rate": 13.5,
            "type": "SIMPLE",
            "period": "YEARLY",
            "term": 18,
            "due": "2027-04-01",
            "totalDue": 372775.0,
            "paid": 165000.0,
            "status": "ACTIVE",
            "direction": "GIVEN",
            "vaId": 4,
        },
        {
            "id": 5,
            "loanNumber": "LN-2026-005",
            "borrowerName": "Deepak Verma",
            "principal": 120000.0,
            "rate": 20.0,
            "type": "COMPOUND",
            "period": "YEARLY",
            "term": 12,
            "due": "2026-10-05",
            "totalDue": 144000.0,
            "paid": 24000.0,
            "status": "OVERDUE",
            "direction": "GIVEN",
            "vaId": 3,
        },
    ],
    "ledger": [
        {
            "id": 1,
            "loanId": 1,
            "vaId": 2,
            "category": "LOAN_REPAYMENT",
            "amount": 14312.5,
            "date": "2026-10-01",
            "description": "Repayment from Rajesh Kumar (Principal: 12000, Interest: 2312.5)",
        },
        {
            "id": 2,
            "loanId": 4,
            "vaId": 2,
            "category": "LOAN_REPAYMENT",
            "amount": 20710.0,
            "date": "2026-09-28",
            "description": "Repayment from Ananya Roy (Principal: 17000, Interest: 3710)",
        },
    ],
}


def collect_flutter_files():
    files = []
    if not os.path.isdir(FLUTTER_DIR):
        return files
    for root, _, filenames in os.walk(FLUTTER_DIR):
        for name in sorted(filenames):
            full_path = os.path.join(root, name)
            rel_path = os.path.relpath(full_path, FLUTTER_DIR).replace("\\", "/")
            try:
                with open(full_path, "r", encoding="utf-8") as f:
                    content = f.read()
                files.append({
                    "path": rel_path,
                    "lines": content.count("\n") + 1,
                    "bytes": len(content.encode("utf-8")),
                    "content": content,
                })
            except Exception:
                pass
    files.sort(key=lambda x: (0 if "/" not in x["path"] else 1, x["path"]))
    return files


def build_flutter_zip():
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for root, _, filenames in os.walk(FLUTTER_DIR):
            for name in filenames:
                full_path = os.path.join(root, name)
                arcname = os.path.join("flutter_app", os.path.relpath(full_path, FLUTTER_DIR))
                zf.write(full_path, arcname)
    buf.seek(0)
    return buf.read()


def calculate_loan_total(principal, rate, interest_type, period, term, basis):
    safe_p = max(0.0, float(principal))
    safe_m = max(1, int(term))
    if interest_type == "ZERO" or float(rate) <= 0.0:
        return round(safe_p, 2)
    if interest_type == "SIMPLE":
        years = (safe_m / 365.0) if basis == "DAYS_BASED" else (safe_m / 12.0)
        return round(safe_p + safe_p * (float(rate) / 100.0) * years, 2)
    periods_map = {
        "HALF_YEARLY": 2.0,
        "QUARTERLY": 4.0,
        "MONTHLY": 12.0,
        "WEEKLY": 52.0,
        "YEARLY": 1.0,
    }
    n = periods_map.get(period, 1.0)
    r_per_period = (float(rate) / 100.0) / n
    num_periods = (safe_m / (365.0 / n)) if basis == "DAYS_BASED" else (safe_m / (12.0 / n))
    return round(safe_p * ((1.0 + r_per_period) ** num_periods), 2)


HTML_TEMPLATE = r"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FlowVault — Flutter Prototype & Codebase Explorer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #0F62FE;
      --primary-dark: #0043CE;
      --primary-light: #EDF5FF;
      --bg-shell: #0B0F17;
      --panel-bg: #111827;
      --panel-border: #1F2937;
      --surface: #FFFFFF;
      --surface-variant: #F4F6F9;
      --text-main: #121619;
      --text-muted: #565D6D;
      --success: #198038;
      --success-bg: #DEFBE6;
      --danger: #DA1E28;
      --danger-bg: #FFE8E8;
      --border: #E0E4EC;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
      background: var(--bg-shell);
      color: #F9FAFB;
      height: 100vh;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    /* Top Workspace Bar */
    .topbar {
      height: 54px;
      background: #0F172A;
      border-bottom: 1px solid var(--panel-border);
      padding: 0 18px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      flex-shrink: 0;
    }
    .topbar-left { display: flex; align-items: center; gap: 12px; }
    .brand-badge {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 800;
      font-size: 1rem;
      color: white;
    }
    .flutter-pill {
      background: rgba(56, 189, 248, 0.14);
      color: #38BDF8;
      border: 1px solid rgba(56, 189, 248, 0.35);
      font-size: 0.72rem;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: 999px;
    }
    .note-pill {
      font-size: 0.75rem;
      color: #9CA3AF;
    }
    .topbar-actions { display: flex; align-items: center; gap: 8px; }
    .top-btn {
      background: #1E293B;
      color: #E2E8F0;
      border: 1px solid #334155;
      padding: 7px 12px;
      border-radius: 8px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
    .top-btn:hover { background: #334155; }
    .top-btn.primary {
      background: var(--primary);
      border-color: var(--primary);
      color: white;
    }
    .top-btn.primary:hover { background: var(--primary-dark); }

    /* Split Workspace */
    .workspace {
      flex: 1;
      display: grid;
      grid-template-columns: 440px 1fr;
      min-height: 0;
      overflow: hidden;
    }

    /* Left Pane: Interactive Phone Prototype */
    .prototype-pane {
      background: radial-gradient(circle at top, #172033 0%, #0B0F17 75%);
      border-right: 1px solid var(--panel-border);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 14px;
      min-height: 0;
      overflow: hidden;
    }
    .phone-frame {
      width: 100%;
      max-width: 392px;
      height: 100%;
      max-height: 810px;
      background: var(--surface-variant);
      color: var(--text-main);
      border-radius: 34px;
      border: 8px solid #1E293B;
      box-shadow: 0 24px 60px rgba(0, 0, 0, 0.65);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      position: relative;
    }
    .phone-appbar {
      background: var(--primary);
      color: white;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-shrink: 0;
    }
    .phone-appbar-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 800;
      font-size: 1rem;
    }
    .jump-code-btn {
      background: rgba(255,255,255,0.18);
      color: white;
      border: 1px solid rgba(255,255,255,0.35);
      border-radius: 999px;
      padding: 4px 9px;
      font-size: 0.7rem;
      font-weight: 600;
      cursor: pointer;
    }
    .jump-code-btn:hover { background: rgba(255,255,255,0.28); }

    .phone-body {
      flex: 1;
      overflow-y: auto;
      padding: 12px 14px 76px 14px;
    }

    /* Hero Portfolio Card */
    .hero-card {
      background: linear-gradient(135deg, #0F62FE, #0043CE);
      color: white;
      border-radius: 18px;
      padding: 16px;
      box-shadow: 0 8px 20px rgba(15, 98, 254, 0.25);
      margin-bottom: 12px;
    }
    .hero-top { display: flex; justify-content: space-between; align-items: center; }
    .hero-label { font-size: 0.68rem; font-weight: 700; opacity: 0.8; letter-spacing: 0.5px; }
    .ledger-badge {
      background: #FFD700;
      color: #000;
      font-size: 0.62rem;
      font-weight: 800;
      padding: 3px 7px;
      border-radius: 10px;
    }
    .hero-val { font-size: 1.55rem; font-weight: 800; margin: 5px 0 12px 0; }
    .hero-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
      border-top: 1px solid rgba(255,255,255,0.22);
      padding-top: 10px;
    }
    .hero-sub-val { font-size: 0.92rem; font-weight: 700; margin-top: 2px; }

    /* Quick Actions & KPIs */
    .quick-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 10px; }
    .quick-card {
      background: white;
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 10px 6px;
      text-align: center;
      cursor: pointer;
      color: var(--primary);
      font-weight: 700;
      font-size: 0.76rem;
    }
    .quick-card:hover { border-color: var(--primary); background: var(--primary-light); }
    .kpi-row { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px; }
    .kpi-card {
      background: white;
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 10px 12px;
    }
    .kpi-title { font-size: 0.68rem; color: var(--text-muted); }
    .kpi-val { font-size: 1.15rem; font-weight: 800; margin-top: 2px; }

    .section-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 12px 0 8px 0;
      font-size: 0.7rem;
      font-weight: 800;
      color: var(--text-muted);
      letter-spacing: 0.5px;
    }
    .link-btn { background: none; border: none; color: var(--primary); font-weight: 700; font-size: 0.75rem; cursor: pointer; }

    /* Cards inside phone */
    .m-card {
      background: white;
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 12px 14px;
      margin-bottom: 10px;
    }
    .m-card.clickable { cursor: pointer; }
    .m-card.clickable:hover { border-color: var(--primary); }
    .m-card-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; }
    .m-title { font-size: 0.9rem; font-weight: 700; color: var(--text-main); }
    .m-sub { font-size: 0.72rem; color: var(--text-muted); margin-top: 2px; }
    .status-pill {
      font-size: 0.62rem;
      font-weight: 800;
      padding: 3px 7px;
      border-radius: 7px;
      flex-shrink: 0;
    }
    .pill-ACTIVE { background: var(--success-bg); color: var(--success); }
    .pill-OVERDUE { background: var(--danger-bg); color: var(--danger); }
    .pill-PAID { background: #E0E2EC; color: #44474F; }

    .progress-track { height: 6px; background: #EDF0F5; border-radius: 999px; margin: 10px 0 8px 0; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--primary); border-radius: 999px; }
    .progress-fill.overdue { background: var(--danger); }
    .metric-split { display: flex; justify-content: space-between; font-size: 0.74rem; }

    /* Controls in Loans Tab */
    .segmented {
      display: grid;
      grid-template-columns: 1fr 1fr;
      background: #E5E7EB;
      border-radius: 10px;
      padding: 3px;
      margin-bottom: 8px;
    }
    .seg-btn {
      border: none;
      background: transparent;
      padding: 7px;
      border-radius: 8px;
      font-size: 0.76rem;
      font-weight: 700;
      color: var(--text-muted);
      cursor: pointer;
    }
    .seg-btn.active { background: white; color: var(--primary); box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .search-input {
      width: 100%;
      padding: 8px 12px;
      border: 1px solid var(--border);
      border-radius: 10px;
      font-size: 0.8rem;
      margin-bottom: 8px;
      background: white;
    }
    .chips-row { display: flex; gap: 6px; overflow-x: auto; padding-bottom: 6px; margin-bottom: 8px; }
    .chip {
      border: 1px solid var(--border);
      background: white;
      border-radius: 8px;
      padding: 5px 10px;
      font-size: 0.72rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }
    .chip.active { background: var(--primary-light); color: var(--primary); border-color: var(--primary); }

    .btn-full {
      width: 100%;
      background: var(--primary);
      color: white;
      border: none;
      padding: 9px 12px;
      border-radius: 10px;
      font-weight: 700;
      font-size: 0.78rem;
      cursor: pointer;
      margin-top: 8px;
    }
    .btn-full:hover { background: var(--primary-dark); }
    .btn-outline {
      background: white;
      color: var(--primary);
      border: 1px solid var(--primary);
      padding: 8px 12px;
      border-radius: 10px;
      font-weight: 700;
      font-size: 0.76rem;
      cursor: pointer;
    }

    /* FAB & Bottom NavBar */
    .phone-fab {
      position: absolute;
      right: 14px;
      bottom: 72px;
      background: var(--primary);
      color: white;
      border: none;
      border-radius: 14px;
      padding: 10px 14px;
      font-weight: 700;
      font-size: 0.78rem;
      box-shadow: 0 6px 18px rgba(15, 98, 254, 0.35);
      cursor: pointer;
      z-index: 5;
    }
    .phone-navbar {
      height: 60px;
      background: white;
      border-top: 1px solid var(--border);
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      flex-shrink: 0;
    }
    .nav-item {
      border: none;
      background: transparent;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 2px;
      font-size: 0.68rem;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
    }
    .nav-item.active { color: var(--primary); font-weight: 800; }

    /* Modal inside phone */
    .phone-modal-overlay {
      position: absolute;
      inset: 0;
      background: rgba(15, 23, 42, 0.55);
      display: none;
      align-items: flex-end;
      z-index: 30;
    }
    .phone-modal {
      background: white;
      width: 100%;
      max-height: 88%;
      overflow-y: auto;
      border-top-left-radius: 22px;
      border-top-right-radius: 22px;
      padding: 16px;
    }
    .modal-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .modal-title { font-size: 1rem; font-weight: 800; }
    .close-x { background: none; border: none; font-size: 1.35rem; cursor: pointer; color: var(--text-muted); }
    .fg { margin-bottom: 10px; }
    .fl { display: block; font-size: 0.72rem; font-weight: 700; color: var(--text-muted); margin-bottom: 4px; }
    .fi, .fs {
      width: 100%;
      padding: 8px 10px;
      border: 1px solid var(--border);
      border-radius: 10px;
      font-size: 0.82rem;
      font-family: inherit;
      background: white;
    }
    .preview-box {
      background: var(--primary-light);
      border: 1px solid rgba(15, 98, 254, 0.25);
      border-radius: 12px;
      padding: 10px 12px;
      margin: 10px 0;
      font-size: 0.76rem;
    }
    .preview-row { display: flex; justify-content: space-between; margin-top: 4px; }

    /* Right Pane: Codebase Explorer */
    .code-pane {
      display: grid;
      grid-template-columns: 270px 1fr;
      background: var(--panel-bg);
      min-height: 0;
      overflow: hidden;
    }
    .file-tree {
      background: #0D1320;
      border-right: 1px solid var(--panel-border);
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
    .tree-header {
      padding: 10px 14px;
      border-bottom: 1px solid var(--panel-border);
      font-size: 0.72rem;
      font-weight: 800;
      color: #94A3B8;
      letter-spacing: 0.5px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
    }
    .tree-controls { display: flex; align-items: center; gap: 6px; }
    .tree-mini-btn {
      background: #1E293B;
      color: #CBD5E1;
      border: 1px solid #334155;
      border-radius: 5px;
      padding: 2px 6px;
      font-size: 0.65rem;
      font-weight: 600;
      cursor: pointer;
    }
    .tree-mini-btn:hover { background: #334155; color: white; }
    .tree-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px 6px;
    }
    .tree-folder, .tree-item {
      width: 100%;
      text-align: left;
      background: transparent;
      border: none;
      color: #CBD5E1;
      padding: 5px 8px;
      border-radius: 6px;
      font-size: 0.76rem;
      font-family: 'JetBrains Mono', monospace;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1px;
    }
    .tree-folder { color: #E2E8F0; font-weight: 600; }
    .tree-folder:hover, .tree-item:hover { background: #1E293B; }
    .tree-item.active { background: rgba(15, 98, 254, 0.28); color: #93C5FD; font-weight: 600; }
    .tree-node-label {
      display: flex;
      align-items: center;
      gap: 6px;
      min-width: 0;
      overflow: hidden;
    }
    .tree-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .tree-chev { color: #64748B; font-size: 0.7rem; width: 10px; display: inline-block; text-align: center; flex-shrink: 0; }
    .tree-badge {
      font-size: 0.6rem;
      font-weight: 700;
      padding: 1px 4px;
      border-radius: 4px;
      flex-shrink: 0;
    }
    .badge-dir { background: rgba(251, 191, 36, 0.14); color: #FBBF24; }
    .badge-dart { background: rgba(56, 189, 248, 0.14); color: #38BDF8; }
    .badge-yaml { background: rgba(192, 132, 252, 0.16); color: #C084FC; }
    .badge-md { background: rgba(134, 239, 172, 0.14); color: #86EFAC; }
    .tree-lines { font-size: 0.66rem; color: #64748B; flex-shrink: 0; margin-left: 6px; }

    .code-viewer {
      display: flex;
      flex-direction: column;
      min-height: 0;
      overflow: hidden;
    }
    .code-toolbar {
      height: 44px;
      padding: 0 16px;
      border-bottom: 1px solid var(--panel-border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #111827;
      flex-shrink: 0;
    }
    .active-filepath {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.82rem;
      color: #E2E8F0;
      font-weight: 600;
    }
    .code-meta { font-size: 0.74rem; color: #94A3B8; margin-left: 10px; }
    .copy-btn {
      background: #1E293B;
      color: #E2E8F0;
      border: 1px solid #334155;
      padding: 5px 10px;
      border-radius: 7px;
      font-size: 0.74rem;
      font-weight: 600;
      cursor: pointer;
    }
    .copy-btn:hover { background: #334155; }

    .code-scroll {
      flex: 1;
      overflow: auto;
      background: #0B101B;
      padding: 14px 0;
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.8rem;
      line-height: 1.55;
    }
    .code-table { width: 100%; border-collapse: collapse; }
    .ln {
      width: 50px;
      text-align: right;
      padding: 0 14px 0 10px;
      color: #475569;
      user-select: none;
      vertical-align: top;
    }
    .lc {
      padding-right: 20px;
      color: #E2E8F0;
      white-space: pre;
    }
    .tok-kw { color: #C084FC; font-weight: 500; }
    .tok-type { color: #38BDF8; }
    .tok-str { color: #86EFAC; }
    .tok-cmt { color: #64748B; font-style: italic; }
    .tok-num { color: #FDBA74; }

    @media (max-width: 980px) {
      body { height: auto; overflow: auto; }
      .workspace { grid-template-columns: 1fr; overflow: visible; }
      .prototype-pane { border-right: none; border-bottom: 1px solid var(--panel-border); }
      .phone-frame { height: 740px; }
      .code-pane { grid-template-columns: 1fr; height: 700px; }
      .file-tree { max-height: 200px; border-right: none; border-bottom: 1px solid var(--panel-border); }
    }
  </style>
</head>
<body>
  <header class="topbar">
    <div class="topbar-left">
      <div class="brand-badge">
        <span>FlowVault</span>
      </div>
      <span class="flutter-pill">Flutter 3 / Material 3</span>
      <span class="note-pill">Interactive Prototype + Live <code>flutter_app/</code> Codebase</span>
    </div>
    <div class="topbar-actions">
      <span class="note-pill" id="fileCountPill">Flutter-Only Workspace</span>
      <a href="/download-flutter-zip" class="top-btn primary">Download Flutter ZIP</a>
    </div>
  </header>

  <main class="workspace">
    <!-- Left: Interactive Mobile Prototype -->
    <section class="prototype-pane">
      <div class="phone-frame">
        <div class="phone-appbar">
          <div class="phone-appbar-title">
            <span>FlowVault</span>
          </div>
          <button class="jump-code-btn" id="jumpCodeBtn" onclick="jumpToCurrentScreenCode()">View Dart Code</button>
        </div>

        <div class="phone-body" id="phoneBody"></div>

        <button class="phone-fab" onclick="openCreateLoanModal()">+ New Loan</button>

        <nav class="phone-navbar">
          <button class="nav-item active" data-tab="dashboard" onclick="switchTab('dashboard')">
            <span>Dashboard</span>
          </button>
          <button class="nav-item" data-tab="loans" onclick="switchTab('loans')">
            <span>Loans</span>
          </button>
          <button class="nav-item" data-tab="accounts" onclick="switchTab('accounts')">
            <span>Accounts</span>
          </button>
          <button class="nav-item" data-tab="borrowers" onclick="switchTab('borrowers')">
            <span>Borrowers</span>
          </button>
        </nav>

        <!-- Modals inside Phone Shell -->
        <div class="phone-modal-overlay" id="modalOverlay">
          <div class="phone-modal" id="modalContent"></div>
        </div>
      </div>
    </section>

    <!-- Right: Flutter Codebase Explorer -->
    <section class="code-pane">
      <aside class="file-tree">
        <div class="tree-header">
          <span>FLUTTER TREE</span>
          <div class="tree-controls">
            <button class="tree-mini-btn" onclick="expandAllFolders(true)">Expand</button>
            <button class="tree-mini-btn" onclick="expandAllFolders(false)">Collapse</button>
          </div>
        </div>
        <div class="tree-list" id="fileTreeList"></div>
      </aside>

      <div class="code-viewer">
        <div class="code-toolbar">
          <div>
            <span class="active-filepath" id="activeFilePath">Loading...</span>
            <span class="code-meta" id="activeFileMeta"></span>
          </div>
          <button class="copy-btn" id="copyBtn" onclick="copyCurrentFile()">Copy Code</button>
        </div>
        <div class="code-scroll" id="codeScroll"></div>
      </div>
    </section>
  </main>

  <script>
    let appData = { accounts: [], borrowers: [], loans: [], ledger: [] };
    let flutterFiles = [];
    let currentTab = 'dashboard';
    let currentFilePath = 'lib/main.dart';

    // Loans screen filters matching LoansScreen
    let loanDirection = 'GIVEN';
    let loanSearch = '';
    let loanStatusFilter = 'ALL';
    let loanSort = 'DEFAULT';

    const tabToDartFile = {
      dashboard: 'lib/screens/dashboard_screen.dart',
      loans: 'lib/screens/loans_screen.dart',
      accounts: 'lib/screens/accounts_screen.dart',
      borrowers: 'lib/screens/borrowers_screen.dart',
    };

    function formatINR(val) {
      return '₹ ' + Math.round(Number(val || 0)).toLocaleString('en-IN');
    }

    function escapeHtml(s) {
      return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
    }

    async function init() {
      const [dataRes, filesRes] = await Promise.all([
        fetch('/api/data'),
        fetch('/api/flutter-files'),
      ]);
      appData = await dataRes.json();
      flutterFiles = await filesRes.json();
      const pill = document.getElementById('fileCountPill');
      if (pill) pill.textContent = flutterFiles.length + ' Flutter files';
      initDefaultExpandedFolders();
      renderTree();
      if (flutterFiles.length > 0) {
        const defaultFile = flutterFiles.find(f => f.path === 'lib/screens/dashboard_screen.dart') || flutterFiles[0];
        selectFile(defaultFile.path);
      }
      renderPhone();
    }

    function switchTab(tab) {
      currentTab = tab;
      document.querySelectorAll('.nav-item').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tab);
      });
      renderPhone();
    }

    function jumpToCurrentScreenCode() {
      const target = tabToDartFile[currentTab] || 'lib/main.dart';
      selectFile(target);
    }

    function renderPhone() {
      const body = document.getElementById('phoneBody');
      if (currentTab === 'dashboard') renderDashboard(body);
      else if (currentTab === 'loans') renderLoans(body);
      else if (currentTab === 'accounts') renderAccounts(body);
      else renderBorrowers(body);
    }

    function renderDashboard(el) {
      const totalPortfolio = appData.loans.filter(l => l.status !== 'PAID').reduce((s, l) => s + l.principal, 0);
      const totalCollected = appData.loans.reduce((s, l) => s + l.paid, 0);
      const totalDue = appData.loans.reduce((s, l) => s + (l.status === 'PAID' ? 0 : Math.max(0, l.totalDue - l.paid)), 0);
      const activeCount = appData.loans.filter(l => l.status === 'ACTIVE').length;
      const overdueCount = appData.loans.filter(l => l.status === 'OVERDUE').length;

      let html = `
        <div class="hero-card">
          <div class="hero-top">
            <span class="hero-label">TOTAL ACTIVE PORTFOLIO</span>
            <span class="ledger-badge">FLOWVAULT LEDGER</span>
          </div>
          <div class="hero-val">${formatINR(totalPortfolio)}</div>
          <div class="hero-grid">
            <div>
              <div class="hero-label">COLLECTED</div>
              <div class="hero-sub-val">${formatINR(totalCollected)}</div>
            </div>
            <div>
              <div class="hero-label">OUTSTANDING DUE</div>
              <div class="hero-sub-val" style="color:#FFB3B8;">${formatINR(totalDue)}</div>
            </div>
          </div>
        </div>

        <div class="quick-row">
          <div class="quick-card" onclick="openCreateLoanModal()">+ New Loan</div>
          <div class="quick-card" onclick="openRecordPaymentModal()">₹ Payment</div>
          <div class="quick-card" onclick="switchTab('accounts')">Pools</div>
        </div>

        <div class="kpi-row">
          <div class="kpi-card">
            <div class="kpi-title">Active Loans</div>
            <div class="kpi-val" style="color:var(--success);">${activeCount}</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-title">Overdue Alerts</div>
            <div class="kpi-val" style="color:var(--danger);">${overdueCount}</div>
          </div>
        </div>

        <div class="section-head">
          <span>CONTRACTS OVERVIEW (${appData.loans.length})</span>
          <button class="link-btn" onclick="switchTab('loans')">View All</button>
        </div>
      `;

      appData.loans.slice(0, 4).forEach(loan => {
        const pct = loan.totalDue > 0 ? Math.min(100, Math.round((loan.paid / loan.totalDue) * 100)) : 100;
        html += `
          <div class="m-card clickable" onclick="openLoanDetailModal(${loan.id})">
            <div class="m-card-head">
              <div>
                <div class="m-title">${escapeHtml(loan.borrowerName)}</div>
                <div class="m-sub">${escapeHtml(loan.loanNumber)} • ${loan.rate}% (${loan.type}) • ${loan.term} mos</div>
              </div>
              <span class="status-pill pill-${loan.status}">${loan.status}</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill ${loan.status === 'OVERDUE' ? 'overdue' : ''}" style="width:${pct}%"></div>
            </div>
            <div class="metric-split">
              <span>Paid: <b>${formatINR(loan.paid)}</b></span>
              <span>Due: <b>${formatINR(loan.totalDue)}</b></span>
            </div>
          </div>
        `;
      });

      html += `<div class="section-head"><span>RECENT LEDGER ACTIVITY</span></div>`;
      appData.ledger.slice(0, 5).forEach(entry => {
        html += `
          <div class="m-card">
            <div class="m-card-head">
              <div>
                <div class="m-title" style="font-size:0.8rem;">${escapeHtml(entry.description)}</div>
                <div class="m-sub">${escapeHtml(entry.date)}</div>
              </div>
              <b style="font-size:0.82rem; white-space:nowrap;">${formatINR(entry.amount)}</b>
            </div>
          </div>
        `;
      });

      el.innerHTML = html;
    }

    function renderLoans(el) {
      const q = loanSearch.trim().toLowerCase();
      let list = appData.loans.filter(l => {
        if ((l.direction || 'GIVEN') !== loanDirection) return false;
        if (loanStatusFilter !== 'ALL' && l.status !== loanStatusFilter) return false;
        if (q && !l.borrowerName.toLowerCase().includes(q) && !l.loanNumber.toLowerCase().includes(q)) return false;
        return true;
      });

      if (loanSort === 'OVERDUE') {
        const w = { OVERDUE: 0, ACTIVE: 1, PAID: 2 };
        list.sort((a, b) => (w[a.status] ?? 9) - (w[b.status] ?? 9));
      } else if (loanSort === 'BALANCE') {
        list.sort((a, b) => Math.max(0, b.totalDue - b.paid) - Math.max(0, a.totalDue - a.paid));
      } else if (loanSort === 'DUE') {
        list.sort((a, b) => a.due.localeCompare(b.due));
      }

      let html = `
        <div class="segmented">
          <button class="seg-btn ${loanDirection === 'GIVEN' ? 'active' : ''}" onclick="loanDirection='GIVEN'; renderPhone()">Loans Given</button>
          <button class="seg-btn ${loanDirection === 'TAKEN' ? 'active' : ''}" onclick="loanDirection='TAKEN'; renderPhone()">Loans Taken</button>
        </div>
        <input class="search-input" type="text" placeholder="Search borrower or loan #..." value="${escapeHtml(loanSearch)}" oninput="loanSearch=this.value; renderPhone()">
        <div class="chips-row">
          ${['ALL', 'ACTIVE', 'OVERDUE', 'PAID'].map(st => `<button class="chip ${loanStatusFilter === st ? 'active' : ''}" onclick="loanStatusFilter='${st}'; renderPhone()">${st === 'ALL' ? 'All' : st[0] + st.slice(1).toLowerCase()}</button>`).join('')}
          <select class="chip" onchange="loanSort=this.value; renderPhone()">
            <option value="DEFAULT" ${loanSort==='DEFAULT'?'selected':''}>Sort: Default</option>
            <option value="OVERDUE" ${loanSort==='OVERDUE'?'selected':''}>Overdue First</option>
            <option value="BALANCE" ${loanSort==='BALANCE'?'selected':''}>Balance High-Low</option>
            <option value="DUE" ${loanSort==='DUE'?'selected':''}>Due Soonest</option>
          </select>
        </div>
      `;

      if (list.length === 0) {
        html += `<div class="m-card" style="text-align:center; color:var(--text-muted);">No loan contracts match the current filters.</div>`;
      } else {
        list.forEach(loan => {
          const rem = loan.status === 'PAID' ? 0 : Math.max(0, loan.totalDue - loan.paid);
          html += `
            <div class="m-card clickable" onclick="openLoanDetailModal(${loan.id})">
              <div class="m-card-head">
                <div>
                  <div class="m-title">${escapeHtml(loan.borrowerName)}</div>
                  <div class="m-sub">${escapeHtml(loan.loanNumber)} • Due ${escapeHtml(loan.due)}</div>
                </div>
                <span class="status-pill pill-${loan.status}">${loan.status}</span>
              </div>
              <div class="metric-split" style="margin-top:8px;">
                <span style="color:var(--text-muted);">Principal: ${formatINR(loan.principal)}</span>
                <b style="color:${loan.status==='PAID'?'var(--success)':'var(--danger)'};">Balance: ${formatINR(rem)}</b>
              </div>
              ${loan.status !== 'PAID' ? `<button class="btn-full" onclick="event.stopPropagation(); openRecordPaymentModal(${loan.id})">Record Payment</button>` : ''}
            </div>
          `;
        });
      }
      el.innerHTML = html;
    }

    function renderAccounts(el) {
      const totalLiq = appData.accounts.reduce((s, a) => s + a.balance, 0);
      let html = `
        <div class="m-card">
          <div class="hero-label" style="color:var(--text-muted);">TOTAL LIQUIDITY ACROSS POOLS</div>
          <div style="font-size:1.4rem; font-weight:800; color:var(--primary); margin:4px 0 10px 0;">${formatINR(totalLiq)}</div>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
            <button class="btn-outline" onclick="openTransferModal()">Transfer</button>
            <button class="btn-full" style="margin-top:0;" onclick="openCreateAccountModal()">+ New Pool</button>
          </div>
        </div>
        <div class="section-head"><span>VIRTUAL ACCOUNT POOLS (${appData.accounts.length})</span></div>
      `;
      appData.accounts.forEach(acc => {
        html += `
          <div class="m-card">
            <div class="m-card-head">
              <div>
                <div class="m-title">${escapeHtml(acc.name)}</div>
                <div class="m-sub">${escapeHtml(acc.accountNumber)} • ${escapeHtml(acc.poolType)}</div>
                <div class="m-sub">${escapeHtml(acc.desc || '')}</div>
              </div>
              <b style="font-size:0.95rem;">${formatINR(acc.balance)}</b>
            </div>
          </div>
        `;
      });
      el.innerHTML = html;
    }

    function renderBorrowers(el) {
      let html = `
        <div class="section-head">
          <span>BORROWER DIRECTORY (${appData.borrowers.length})</span>
          <button class="link-btn" onclick="openAddBorrowerModal()">+ Add Borrower</button>
        </div>
      `;
      appData.borrowers.forEach(b => {
        const bLoans = appData.loans.filter(l => l.borrowerName.toLowerCase() === b.name.toLowerCase());
        const outstanding = bLoans.filter(l => l.status !== 'PAID').reduce((s, l) => s + Math.max(0, l.totalDue - l.paid), 0);
        html += `
          <div class="m-card">
            <div class="m-card-head">
              <div>
                <div class="m-title">${escapeHtml(b.name)}</div>
                <div class="m-sub">${escapeHtml(b.phone)} • ${escapeHtml(b.email)}</div>
              </div>
              <span class="status-pill" style="background:var(--primary-light); color:var(--primary);">Grade ${escapeHtml(b.rating)}</span>
            </div>
            ${b.notes ? `<div class="m-sub" style="margin-top:6px;">${escapeHtml(b.notes)}</div>` : ''}
            <div class="metric-split" style="margin-top:8px; padding-top:8px; border-top:1px solid #F0F2F5;">
              <span style="color:var(--text-muted);">${bLoans.length} contract(s)</span>
              <b>Outstanding: ${formatINR(outstanding)}</b>
            </div>
          </div>
        `;
      });
      el.innerHTML = html;
    }

    function openModal(html) {
      document.getElementById('modalContent').innerHTML = html;
      document.getElementById('modalOverlay').style.display = 'flex';
    }
    function closeModal() {
      document.getElementById('modalOverlay').style.display = 'none';
    }

    function calcPreview(p, r, type, period, term, basis) {
      const safeP = Math.max(0, parseFloat(p) || 0);
      const safeR = parseFloat(r) || 0;
      const safeT = Math.max(1, parseInt(term) || 1);
      if (type === 'ZERO' || safeR <= 0) return { interest: 0, total: safeP, monthly: safeP / safeT };
      if (type === 'SIMPLE') {
        const yrs = basis === 'DAYS_BASED' ? safeT / 365 : safeT / 12;
        const interest = safeP * (safeR / 100) * yrs;
        const total = safeP + interest;
        return { interest, total, monthly: total / Math.max(1, basis === 'DAYS_BASED' ? Math.ceil(safeT/30) : safeT) };
      }
      const map = { HALF_YEARLY: 2, QUARTERLY: 4, MONTHLY: 12, WEEKLY: 52, YEARLY: 1 };
      const n = map[period] || 1;
      const np = basis === 'DAYS_BASED' ? safeT / (365 / n) : safeT / (12 / n);
      const total = safeP * Math.pow(1 + (safeR / 100) / n, np);
      return { interest: total - safeP, total, monthly: total / Math.max(1, basis === 'DAYS_BASED' ? Math.ceil(safeT/30) : safeT) };
    }

    function updateLoanPreviewBox() {
      const p = document.getElementById('cl_principal')?.value;
      const r = document.getElementById('cl_rate')?.value;
      const t = document.getElementById('cl_type')?.value;
      const per = document.getElementById('cl_period')?.value || 'YEARLY';
      const term = document.getElementById('cl_term')?.value;
      const basis = document.getElementById('cl_basis')?.value;
      const pg = document.getElementById('cl_period_group');
      if (pg) pg.style.display = t === 'COMPOUND' ? 'block' : 'none';
      const res = calcPreview(p, r, t, per, term, basis);
      const box = document.getElementById('cl_preview');
      if (box) {
        box.innerHTML = `
          <b style="color:var(--primary-dark); font-size:0.68rem;">LIVE CALCULATION PREVIEW</b>
          <div class="preview-row"><span>Total Interest</span><b>${formatINR(res.interest)}</b></div>
          <div class="preview-row"><span>Total Maturity Due</span><b style="color:var(--primary);">${formatINR(res.total)}</b></div>
          <div class="preview-row"><span>Est. Monthly Installment</span><b>${formatINR(res.monthly)}</b></div>
        `;
      }
    }

    function openCreateLoanModal() {
      openModal(`
        <div class="modal-head">
          <span class="modal-title">Issue New Loan</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <form onsubmit="submitCreateLoan(event)">
          <div class="fg">
            <label class="fl">Direction</label>
            <select class="fs" id="cl_dir">
              <option value="GIVEN">Money Given</option>
              <option value="TAKEN">Money Taken</option>
            </select>
          </div>
          <div class="fg"><label class="fl">Borrower Name</label><input class="fi" id="cl_name" required placeholder="e.g. Anand Mahindra"></div>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
            <div class="fg"><label class="fl">Principal (₹)</label><input class="fi" id="cl_principal" type="number" value="100000" min="1" required oninput="updateLoanPreviewBox()"></div>
            <div class="fg"><label class="fl">Rate (%)</label><input class="fi" id="cl_rate" type="number" step="0.1" value="12.0" min="0" required oninput="updateLoanPreviewBox()"></div>
          </div>
          <div class="fg">
            <label class="fl">Interest Mode</label>
            <select class="fs" id="cl_type" onchange="updateLoanPreviewBox()">
              <option value="SIMPLE">Simple Interest</option>
              <option value="COMPOUND">Compound Interest</option>
              <option value="ZERO">Zero Interest (0%)</option>
            </select>
          </div>
          <div class="fg" id="cl_period_group" style="display:none;">
            <label class="fl">Compounding Interval</label>
            <select class="fs" id="cl_period" onchange="updateLoanPreviewBox()">
              <option value="YEARLY">Yearly</option>
              <option value="HALF_YEARLY">Half yearly</option>
              <option value="QUARTERLY">Quarterly</option>
              <option value="MONTHLY">Monthly</option>
              <option value="WEEKLY">Weekly</option>
            </select>
          </div>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
            <div class="fg"><label class="fl">Term</label><input class="fi" id="cl_term" type="number" value="12" min="1" required oninput="updateLoanPreviewBox()"></div>
            <div class="fg">
              <label class="fl">Basis</label>
              <select class="fs" id="cl_basis" onchange="updateLoanPreviewBox()">
                <option value="MONTHS_BASED">Months</option>
                <option value="DAYS_BASED">Days</option>
              </select>
            </div>
          </div>
          <div class="fg">
            <label class="fl">Virtual Account Pool</label>
            <select class="fs" id="cl_va">${appData.accounts.map(a => `<option value="${a.id}">${escapeHtml(a.name)} (${formatINR(a.balance)})</option>`).join('')}</select>
          </div>
          <div class="preview-box" id="cl_preview"></div>
          <button class="btn-full" type="submit">Save & Issue Loan</button>
        </form>
      `);
      updateLoanPreviewBox();
    }

    async function submitCreateLoan(e) {
      e.preventDefault();
      await fetch('/api/create-loan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          borrowerName: document.getElementById('cl_name').value,
          principal: parseFloat(document.getElementById('cl_principal').value),
          rate: parseFloat(document.getElementById('cl_rate').value),
          interestType: document.getElementById('cl_type').value,
          interestPeriod: document.getElementById('cl_period').value,
          term: parseInt(document.getElementById('cl_term').value),
          interestBasis: document.getElementById('cl_basis').value,
          direction: document.getElementById('cl_dir').value,
          vaId: parseInt(document.getElementById('cl_va').value),
        }),
      });
      closeModal();
      await reloadData();
    }

    function openLoanDetailModal(loanId) {
      const loan = appData.loans.find(l => l.id === loanId);
      if (!loan) return;
      const rem = loan.status === 'PAID' ? 0 : Math.max(0, loan.totalDue - loan.paid);
      const acc = appData.accounts.find(a => a.id === loan.vaId);
      openModal(`
        <div class="modal-head">
          <span class="modal-title">${escapeHtml(loan.borrowerName)} • ${escapeHtml(loan.loanNumber)}</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <div class="preview-box" style="background:#F8FAFC; border-color:var(--border);">
          <div class="preview-row"><span>Principal Issued</span><b>${formatINR(loan.principal)}</b></div>
          <div class="preview-row"><span>Interest Rate</span><b>${loan.rate}% (${loan.type})</b></div>
          <div class="preview-row"><span>Total Paid</span><b>${formatINR(loan.paid)}</b></div>
          <div class="preview-row"><span>Remaining Balance</span><b style="color:var(--danger);">${formatINR(rem)}</b></div>
          <div class="preview-row"><span>Due Date</span><b>${escapeHtml(loan.due)}</b></div>
          <div class="preview-row"><span>Pool Account</span><b>${escapeHtml(acc ? acc.name : 'N/A')}</b></div>
        </div>
        ${loan.status !== 'PAID' ? `
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-top:10px;">
            <button class="btn-outline" onclick="settleLoan(${loan.id})">Mark Settled</button>
            <button class="btn-full" style="margin-top:0;" onclick="openRecordPaymentModal(${loan.id})">Record Payment</button>
          </div>
        ` : ''}
      `);
    }

    async function settleLoan(loanId) {
      await fetch('/api/settle-loan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ loanId }),
      });
      closeModal();
      await reloadData();
    }

    function openRecordPaymentModal(initialLoanId) {
      const openLoans = appData.loans.filter(l => l.status !== 'PAID');
      if (openLoans.length === 0) return;
      const selected = openLoans.find(l => l.id === initialLoanId) || openLoans[0];
      const rem = Math.max(0, Math.round(selected.totalDue - selected.paid));
      openModal(`
        <div class="modal-head">
          <span class="modal-title">Record Repayment</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <form onsubmit="submitRecordPayment(event)">
          <div class="fg">
            <label class="fl">Loan Contract</label>
            <select class="fs" id="rp_loan" onchange="const l=appData.loans.find(x=>x.id==this.value); if(l) document.getElementById('rp_amt').value=Math.max(0,Math.round(l.totalDue-l.paid));">
              ${openLoans.map(l => `<option value="${l.id}" ${l.id===selected.id?'selected':''}>${escapeHtml(l.borrowerName)} (${escapeHtml(l.loanNumber)}) — Due ${formatINR(l.totalDue - l.paid)}</option>`).join('')}
            </select>
          </div>
          <div class="fg"><label class="fl">Payment Amount (₹)</label><input class="fi" id="rp_amt" type="number" value="${rem}" min="1" required></div>
          <div class="fg">
            <label class="fl">Receiving Virtual Account</label>
            <select class="fs" id="rp_va">${appData.accounts.map(a => `<option value="${a.id}">${escapeHtml(a.name)}</option>`).join('')}</select>
          </div>
          <button class="btn-full" type="submit">Confirm & Update Ledger</button>
        </form>
      `);
    }

    async function submitRecordPayment(e) {
      e.preventDefault();
      await fetch('/api/record-payment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          loanId: parseInt(document.getElementById('rp_loan').value),
          amount: parseFloat(document.getElementById('rp_amt').value),
          vaId: parseInt(document.getElementById('rp_va').value),
        }),
      });
      closeModal();
      await reloadData();
    }

    function openCreateAccountModal() {
      openModal(`
        <div class="modal-head">
          <span class="modal-title">Create Virtual Pool</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <form onsubmit="submitCreateAccount(event)">
          <div class="fg"><label class="fl">Account Name</label><input class="fi" id="ca_name" required placeholder="e.g. ICICI Reserve"></div>
          <div class="fg"><label class="fl">Initial Balance (₹)</label><input class="fi" id="ca_bal" type="number" value="250000" required></div>
          <div class="fg">
            <label class="fl">Pool Category</label>
            <select class="fs" id="ca_type">
              <option>Bank account</option>
              <option>Collection Account</option>
              <option>Recovery Account</option>
              <option>Operations account</option>
            </select>
          </div>
          <button class="btn-full" type="submit">Create Pool</button>
        </form>
      `);
    }

    async function submitCreateAccount(e) {
      e.preventDefault();
      await fetch('/api/create-account', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: document.getElementById('ca_name').value,
          balance: parseFloat(document.getElementById('ca_bal').value),
          poolType: document.getElementById('ca_type').value,
        }),
      });
      closeModal();
      await reloadData();
    }

    function openTransferModal() {
      if (appData.accounts.length < 2) return;
      openModal(`
        <div class="modal-head">
          <span class="modal-title">Transfer Between Pools</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <form onsubmit="submitTransfer(event)">
          <div class="fg"><label class="fl">Source Pool</label><select class="fs" id="tr_from">${appData.accounts.map(a => `<option value="${a.id}">${escapeHtml(a.name)}</option>`).join('')}</select></div>
          <div class="fg"><label class="fl">Destination Pool</label><select class="fs" id="tr_to">${appData.accounts.map((a, idx) => `<option value="${a.id}" ${idx===1?'selected':''}>${escapeHtml(a.name)}</option>`).join('')}</select></div>
          <div class="fg"><label class="fl">Amount (₹)</label><input class="fi" id="tr_amt" type="number" value="25000" min="1" required></div>
          <button class="btn-full" type="submit">Transfer</button>
        </form>
      `);
    }

    async function submitTransfer(e) {
      e.preventDefault();
      await fetch('/api/transfer', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fromId: parseInt(document.getElementById('tr_from').value),
          toId: parseInt(document.getElementById('tr_to').value),
          amount: parseFloat(document.getElementById('tr_amt').value),
        }),
      });
      closeModal();
      await reloadData();
    }

    function openAddBorrowerModal() {
      openModal(`
        <div class="modal-head">
          <span class="modal-title">Add Borrower</span>
          <button class="close-x" onclick="closeModal()">&times;</button>
        </div>
        <form onsubmit="submitAddBorrower(event)">
          <div class="fg"><label class="fl">Full Name</label><input class="fi" id="ab_name" required></div>
          <div class="fg"><label class="fl">Phone</label><input class="fi" id="ab_phone" placeholder="+91 ..."></div>
          <div class="fg"><label class="fl">Email</label><input class="fi" id="ab_email" placeholder="name@domain.com"></div>
          <div class="fg">
            <label class="fl">Credit Grade</label>
            <select class="fs" id="ab_rating"><option>A+</option><option selected>A</option><option>B</option><option>C</option></select>
          </div>
          <div class="fg"><label class="fl">Notes</label><input class="fi" id="ab_notes"></div>
          <button class="btn-full" type="submit">Save Borrower</button>
        </form>
      `);
    }

    async function submitAddBorrower(e) {
      e.preventDefault();
      await fetch('/api/add-borrower', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: document.getElementById('ab_name').value,
          phone: document.getElementById('ab_phone').value || 'Not provided',
          email: document.getElementById('ab_email').value || 'Not provided',
          rating: document.getElementById('ab_rating').value,
          notes: document.getElementById('ab_notes').value || '',
        }),
      });
      closeModal();
      await reloadData();
    }

    async function reloadData() {
      const res = await fetch('/api/data');
      appData = await res.json();
      renderPhone();
    }

    // Hierarchical Flutter Tree Explorer
    const expandedFolders = new Set(['flutter_app']);

    function initDefaultExpandedFolders() {
      expandedFolders.clear();
      expandedFolders.add('flutter_app');
      flutterFiles.forEach(f => {
        const parts = f.path.split('/');
        let acc = 'flutter_app';
        for (let i = 0; i < parts.length - 1; i++) {
          acc += '/' + parts[i];
          expandedFolders.add(acc);
        }
      });
    }

    function expandAllFolders(open) {
      expandedFolders.clear();
      expandedFolders.add('flutter_app');
      if (open) {
        flutterFiles.forEach(f => {
          const parts = f.path.split('/');
          let acc = 'flutter_app';
          for (let i = 0; i < parts.length - 1; i++) {
            acc += '/' + parts[i];
            expandedFolders.add(acc);
          }
        });
      }
      renderTree();
    }

    function toggleFolder(folderKey) {
      if (expandedFolders.has(folderKey)) expandedFolders.delete(folderKey);
      else expandedFolders.add(folderKey);
      renderTree();
    }

    function buildTreeModel() {
      const root = { name: 'flutter_app', key: 'flutter_app', dirs: {}, files: [] };
      flutterFiles.forEach(f => {
        const parts = f.path.split('/');
        let node = root;
        let keyAcc = 'flutter_app';
        for (let i = 0; i < parts.length - 1; i++) {
          const dirName = parts[i];
          keyAcc += '/' + dirName;
          if (!node.dirs[dirName]) {
            node.dirs[dirName] = { name: dirName, key: keyAcc, dirs: {}, files: [] };
          }
          node = node.dirs[dirName];
        }
        node.files.push({ name: parts[parts.length - 1], file: f });
      });
      return root;
    }

    function fileBadge(name) {
      if (name.endsWith('.dart')) return '<span class="tree-badge badge-dart">DART</span>';
      if (name.endsWith('.yaml')) return '<span class="tree-badge badge-yaml">YAML</span>';
      return '<span class="tree-badge badge-md">MD</span>';
    }

    function renderTreeNode(node, depth) {
      const isOpen = expandedFolders.has(node.key);
      const pad = 8 + depth * 14;
      let html = `
        <button class="tree-folder" style="padding-left:${pad}px;" data-folder="${escapeHtml(node.key)}" onclick="toggleFolder(this.dataset.folder)">
          <span class="tree-node-label">
            <span class="tree-chev">${isOpen ? '▾' : '▸'}</span>
            <span class="tree-badge badge-dir">DIR</span>
            <span class="tree-name">${escapeHtml(node.name)}/</span>
          </span>
        </button>
      `;
      if (!isOpen) return html;

      const dirNames = Object.keys(node.dirs).sort((a, b) => a.localeCompare(b));
      dirNames.forEach(d => {
        html += renderTreeNode(node.dirs[d], depth + 1);
      });

      const sortedFiles = [...node.files].sort((a, b) => a.name.localeCompare(b.name));
      sortedFiles.forEach(entry => {
        const f = entry.file;
        const active = f.path === currentFilePath ? 'active' : '';
        const fPad = 8 + (depth + 1) * 14;
        html += `
          <button class="tree-item ${active}" style="padding-left:${fPad}px;" data-path="${escapeHtml(f.path)}" onclick="selectFile(this.dataset.path)">
            <span class="tree-node-label">
              <span class="tree-chev"></span>
              ${fileBadge(entry.name)}
              <span class="tree-name">${escapeHtml(entry.name)}</span>
            </span>
            <span class="tree-lines">${f.lines}L</span>
          </button>
        `;
      });
      return html;
    }

    function renderTree() {
      const list = document.getElementById('fileTreeList');
      const root = buildTreeModel();
      list.innerHTML = renderTreeNode(root, 0);
    }

    function highlightCode(line, path) {
      const escaped = escapeHtml(line);
      if (path.endsWith('.yaml')) {
        if (/^\s*#/.test(line)) return `<span class="tok-cmt">${escaped}</span>`;
        return escaped.replace(/^(\s*[a-zA-Z0-9_-]+:)/, '<span class="tok-type">$1</span>');
      }
      if (path.endsWith('.md')) {
        if (/^\s*#/.test(line)) return `<span class="tok-kw">${escaped}</span>`;
        return escaped;
      }
      if (/^\s*\/\//.test(line)) {
        return `<span class="tok-cmt">${escaped}</span>`;
      }
      return escaped
        .replace(/('(?:[^'\\]|\\.)*')/g, '<span class="tok-str">$1</span>')
        .replace(/\b(import|class|enum|final|const|var|void|return|if|else|switch|case|for|in|late|required|super|extends|with|async|await|true|false|null|static|get|override)\b/g, '<span class="tok-kw">$1</span>')
        .replace(/\b(Widget|BuildContext|StatelessWidget|StatefulWidget|State|ChangeNotifier|MaterialApp|Scaffold|ThemeData|ColorScheme|Color|Text|Row|Column|ListView|Card|ListTile|Container|Padding|SizedBox|Expanded|Icon|FilledButton|OutlinedButton|TextButton|FloatingActionButton|NavigationBar|NavigationRail|TextFormField|DropdownButtonFormField|SegmentedButton|FilterChip|AlertDialog|Future|String|double|int|bool|DateTime|List|VoidCallback)\b/g, '<span class="tok-type">$1</span>');
    }

    function selectFile(path) {
      const file = flutterFiles.find(f => f.path === path);
      if (!file) return;
      currentFilePath = path;
      // Ensure all ancestor folders are open
      expandedFolders.add('flutter_app');
      const parts = path.split('/');
      let acc = 'flutter_app';
      for (let i = 0; i < parts.length - 1; i++) {
        acc += '/' + parts[i];
        expandedFolders.add(acc);
      }
      renderTree();
      document.getElementById('activeFilePath').textContent = 'flutter_app/' + file.path;
      document.getElementById('activeFileMeta').textContent = `${file.lines} lines • ${(file.bytes / 1024).toFixed(1)} KB`;
      const lines = file.content.split('\n');
      const rows = lines.map((line, i) => `
        <tr>
          <td class="ln">${i + 1}</td>
          <td class="lc">${highlightCode(line, file.path)}</td>
        </tr>
      `).join('');
      document.getElementById('codeScroll').innerHTML = `<table class="code-table">${rows}</table>`;
    }

    async function copyCurrentFile() {
      const file = flutterFiles.find(f => f.path === currentFilePath);
      if (!file) return;
      await navigator.clipboard.writeText(file.content);
      const btn = document.getElementById('copyBtn');
      btn.textContent = 'Copied!';
      setTimeout(() => { btn.textContent = 'Copy Code'; }, 1500);
    }

    init();
  </script>
</body>
</html>
"""


class FlowVaultServer(http.server.BaseHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")

    def send_json(self, obj, code=200):
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(json.dumps(obj).encode("utf-8"))

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_cors_headers()
        self.end_headers()

    def do_HEAD(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_cors_headers()
        self.end_headers()

    def do_GET(self):
        path = urllib.parse.urlparse(self.path).path

        if path == "/api/data":
            self.send_json(DATA)
        elif path == "/api/flutter-files":
            self.send_json(collect_flutter_files())
        elif path == "/download-flutter-zip":
            zip_bytes = build_flutter_zip()
            self.send_response(200)
            self.send_header("Content-Type", "application/zip")
            self.send_header("Content-Disposition", 'attachment; filename="flowvault_flutter_app.zip"')
            self.send_header("Content-Length", str(len(zip_bytes)))
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(zip_bytes)
        else:
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(HTML_TEMPLATE.encode("utf-8"))

    def do_POST(self):
        path = urllib.parse.urlparse(self.path).path
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length)
        payload = json.loads(body.decode("utf-8")) if body else {}

        if path == "/api/create-loan":
            new_id = max((l["id"] for l in DATA["loans"]), default=0) + 1
            p = float(payload["principal"])
            r = float(payload["rate"])
            t = int(payload["term"])
            itype = payload.get("interestType", "SIMPLE")
            iperiod = payload.get("interestPeriod", "YEARLY")
            ibasis = payload.get("interestBasis", "MONTHS_BASED")
            direction = payload.get("direction", "GIVEN")
            va_id = int(payload["vaId"])
            total_due = calculate_loan_total(p, r, itype, iperiod, t, ibasis)
            due_dt = (datetime.now() + timedelta(days=t if ibasis == "DAYS_BASED" else t * 30)).strftime("%Y-%m-%d")
            loan_num = f"LN-{datetime.now().year}-{new_id:03d}"

            DATA["loans"].insert(0, {
                "id": new_id,
                "loanNumber": loan_num,
                "borrowerName": payload["borrowerName"].strip(),
                "principal": p,
                "rate": 0.0 if itype == "ZERO" else r,
                "type": itype,
                "period": iperiod,
                "term": t,
                "due": due_dt,
                "totalDue": total_due,
                "paid": 0.0,
                "status": "ACTIVE",
                "direction": direction,
                "vaId": va_id,
            })
            if not any(b["name"].lower() == payload["borrowerName"].strip().lower() for b in DATA["borrowers"]):
                DATA["borrowers"].append({
                    "id": len(DATA["borrowers"]) + 1,
                    "name": payload["borrowerName"].strip(),
                    "phone": "Not provided",
                    "email": "Not provided",
                    "rating": "B",
                    "notes": f"Added automatically from {loan_num}",
                })
            for acc in DATA["accounts"]:
                if acc["id"] == va_id:
                    acc["balance"] += (-p if direction == "GIVEN" else p)
                    break
            DATA["ledger"].insert(0, {
                "id": len(DATA["ledger"]) + 1,
                "loanId": new_id,
                "vaId": va_id,
                "category": "LOAN_DISBURSAL",
                "amount": p,
                "date": datetime.now().strftime("%Y-%m-%d"),
                "description": f"{'Disbursed to' if direction == 'GIVEN' else 'Borrowed from'} {payload['borrowerName'].strip()} ({loan_num})",
            })
            self.send_json({"success": True})

        elif path == "/api/record-payment":
            lid = int(payload["loanId"])
            amt = float(payload["amount"])
            va_id = int(payload["vaId"])
            for loan in DATA["loans"]:
                if loan["id"] == lid:
                    loan["paid"] += amt
                    if loan["paid"] >= loan["totalDue"]:
                        loan["status"] = "PAID"
                    delta = amt if loan.get("direction", "GIVEN") == "GIVEN" else -amt
                    for acc in DATA["accounts"]:
                        if acc["id"] == va_id:
                            acc["balance"] += delta
                            break
                    DATA["ledger"].insert(0, {
                        "id": len(DATA["ledger"]) + 1,
                        "loanId": lid,
                        "vaId": va_id,
                        "category": "LOAN_REPAYMENT",
                        "amount": amt,
                        "date": datetime.now().strftime("%Y-%m-%d"),
                        "description": f"Payment for {loan['loanNumber']} ({loan['borrowerName']})",
                    })
                    break
            self.send_json({"success": True})

        elif path == "/api/settle-loan":
            lid = int(payload["loanId"])
            for loan in DATA["loans"]:
                if loan["id"] == lid:
                    rem = max(0.0, loan["totalDue"] - loan["paid"])
                    loan["paid"] += rem
                    loan["status"] = "PAID"
                    if rem > 0:
                        for acc in DATA["accounts"]:
                            if acc["id"] == loan["vaId"]:
                                acc["balance"] += rem
                                break
                        DATA["ledger"].insert(0, {
                            "id": len(DATA["ledger"]) + 1,
                            "loanId": lid,
                            "vaId": loan["vaId"],
                            "category": "LOAN_REPAYMENT",
                            "amount": rem,
                            "date": datetime.now().strftime("%Y-%m-%d"),
                            "description": f"Final settlement for {loan['loanNumber']} ({loan['borrowerName']})",
                        })
                    break
            self.send_json({"success": True})

        elif path == "/api/create-account":
            new_id = max((a["id"] for a in DATA["accounts"]), default=0) + 1
            DATA["accounts"].append({
                "id": new_id,
                "accountNumber": f"VA-USR-{new_id:03d}",
                "name": payload["name"].strip(),
                "poolType": payload["poolType"],
                "balance": float(payload["balance"]),
                "currency": "₹",
                "desc": "Custom virtual account pool",
            })
            self.send_json({"success": True})

        elif path == "/api/transfer":
            from_id = int(payload["fromId"])
            to_id = int(payload["toId"])
            amt = float(payload["amount"])
            if from_id != to_id and amt > 0:
                from_acc = next((a for a in DATA["accounts"] if a["id"] == from_id), None)
                to_acc = next((a for a in DATA["accounts"] if a["id"] == to_id), None)
                if from_acc and to_acc:
                    from_acc["balance"] -= amt
                    to_acc["balance"] += amt
                    DATA["ledger"].insert(0, {
                        "id": len(DATA["ledger"]) + 1,
                        "loanId": None,
                        "vaId": to_id,
                        "category": "POOL_TRANSFER",
                        "amount": amt,
                        "date": datetime.now().strftime("%Y-%m-%d"),
                        "description": f"Transferred from {from_acc['name']} to {to_acc['name']}",
                    })
            self.send_json({"success": True})

        elif path == "/api/add-borrower":
            new_id = max((b["id"] for b in DATA["borrowers"]), default=0) + 1
            DATA["borrowers"].append({
                "id": new_id,
                "name": payload["name"].strip(),
                "phone": payload.get("phone", "Not provided"),
                "email": payload.get("email", "Not provided"),
                "rating": payload.get("rating", "A"),
                "notes": payload.get("notes", ""),
            })
            self.send_json({"success": True})
        else:
            self.send_json({"error": "Not found"}, code=404)


if __name__ == "__main__":
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), FlowVaultServer) as httpd:
        print(f"FlowVault Prototype + Codebase Server running on port {PORT}")
        httpd.serve_forever()
