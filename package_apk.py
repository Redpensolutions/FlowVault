#!/usr/bin/env python3
import os
import sys
import subprocess
import zipfile
import shutil

def main():
    workspace = '/app/applet'
    keystore = os.path.join(workspace, 'debug.keystore')
    zipalign = '/opt/android/sdk/build-tools/36.0.0/zipalign'
    apksigner = '/opt/android/sdk/build-tools/36.0.0/apksigner'
    apktool = os.path.join(workspace, 'tmp/apktool.jar')
    apk_work = '/tmp/apk_work'
    rebuilt_apk = '/tmp/test_rebuild.apk'

    out_debug_dir = os.path.join(workspace, 'app/build/outputs/apk/debug')
    root_out_dir = os.path.join(workspace, '.build-outputs')
    os.makedirs(out_debug_dir, exist_ok=True)
    os.makedirs(root_out_dir, exist_ok=True)

    target_apk = os.path.join(out_debug_dir, 'app-debug.apk')
    root_target_apk = os.path.join(root_out_dir, 'app-debug.apk')

    # Step 0: If /tmp/apk_work exists, rebuild it with apktool
    if os.path.exists(apk_work) and os.path.exists(apktool):
        print(f"Rebuilding APK from {apk_work} using apktool...")
        res = subprocess.run(['java', '-jar', apktool, 'b', apk_work, '-o', rebuilt_apk])
        if res.returncode != 0:
            print("Apktool build failed!")
            sys.exit(1)
        print("Apktool rebuild succeeded!")

    # Check source apk to process
    src_apk = None
    out_backup = os.path.join(workspace, 'final-app-debug.apk')
    candidates = [
        out_backup,
        rebuilt_apk,
        target_apk,
        root_target_apk
    ]
    for c in candidates:
        if os.path.exists(c) and os.path.getsize(c) > 0:
            src_apk = c
            break

    if not src_apk:
        print("No candidate APK found to package!")
        sys.exit(1)

    print(f"Packaging from: {src_apk}")

    clean_apk = '/tmp/cleaned_unsigned_temp.apk'
    aligned_apk = '/tmp/aligned_signed_temp.apk'

    if os.path.exists(clean_apk):
        os.remove(clean_apk)
    if os.path.exists(aligned_apk):
        os.remove(aligned_apk)

    print("1. Removing legacy signature files from META-INF...")
    with zipfile.ZipFile(src_apk, 'r') as zin, zipfile.ZipFile(clean_apk, 'w') as zout:
        for item in zin.infolist():
            if item.filename.startswith('META-INF/') and any(item.filename.endswith(ext) for ext in ['.SF', '.RSA', '.DSA', '.MF']):
                continue
            zout.writestr(item, zin.read(item.filename))

    print("2. Running zipalign -p 4 for 4KB/16KB page boundary alignment...")
    subprocess.run([zipalign, '-p', '4', '-f', clean_apk, aligned_apk], check=True)

    print("3. Signing with apksigner (v2 & v3 schemes)...")
    subprocess.run([
        apksigner, 'sign',
        '--ks', keystore,
        '--ks-pass', 'pass:android',
        '--ks-key-alias', 'androiddebugkey',
        '--key-pass', 'pass:android',
        '--v1-signing-enabled', 'true',
        '--v2-signing-enabled', 'true',
        '--v3-signing-enabled', 'true',
        aligned_apk
    ], check=True)

    print("4. Verifying alignment and signatures...")
    subprocess.run([zipalign, '-c', '-v', '4', aligned_apk], check=True, stdout=subprocess.DEVNULL)
    subprocess.run([apksigner, 'verify', '--verbose', aligned_apk], check=True)

    print("5. Deploying to final output paths...")
    shutil.copyfile(aligned_apk, target_apk)
    shutil.copyfile(aligned_apk, root_target_apk)
    print("Packaging complete! Both outputs updated successfully.")

    # Ensure FlowVault preview companion server on port 3000 is running
    server_path = os.path.join(workspace, 'server.py')
    if os.path.exists(server_path):
        import urllib.request
        try:
            urllib.request.urlopen('http://localhost:3000', timeout=1)
        except Exception:
            print("Starting FlowVault preview server on port 3000...")
            subprocess.Popen(['python3', server_path], cwd=workspace, stdout=open('/var/log/flowvault_server.log', 'a'), stderr=subprocess.STDOUT)

if __name__ == '__main__':
    main()
