#!/usr/bin/env bash
# Reproducible cloud preparation. Sources/tests/manifests are not rewritten.
set -euo pipefail
cd /workspace/NEBULA
python3 - <<'PY'
import hashlib, pathlib, tarfile, urllib.request
root=pathlib.Path('/workspace/toolchains')
jdk=root/'jdk-21.0.6+7'
if not (jdk/'bin/javac').is_file():
    root.mkdir(exist_ok=True)
    name='OpenJDK21U-jdk_x64_linux_hotspot_21.0.6_7.tar.gz'
    base='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/'
    archive=pathlib.Path('/tmp')/name
    urllib.request.urlretrieve(base+name,archive)
    expected=urllib.request.urlopen(base+name+'.sha256.txt').read().decode().split()[0]
    with archive.open('rb') as source:
        actual=hashlib.file_digest(source,'sha256').hexdigest()
    if expected!=actual:raise SystemExit('JDK SHA-256 mismatch; refusing installation')
    with tarfile.open(archive) as source:source.extractall(root,filter='data')
    archive.unlink()
PY
./tools/cloud-gradle core:test html:dist lwjgl3:classes --console=plain
