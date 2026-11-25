#!/usr/bin/env bash
# Genera dist/PanteraFitness-Windows.zip: version autocontenida para Windows
# (Java 21 portable de Eclipse Temurin + JAR unico con frontend y API + lanzador .bat).
# Uso (desde la raiz del repo, en Linux/macOS/Git Bash):  bash scripts/build-ejecutable-windows.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JRE_VERSION="21.0.9+10"
JRE_FILE="OpenJDK21U-jre_x64_windows_hotspot_${JRE_VERSION/+/_}.zip"
JRE_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-${JRE_VERSION/+/%2B}/${JRE_FILE}"
JRE_SHA256="39c5e23f3ce4d420663afba8ffde28034b72e2b3e240943dc2321bc1f912eef9"
WORK="$ROOT/dist/work"
PKG="$WORK/PanteraFitness"

echo "1/4 Compilando frontend (sitio estatico)..."
(cd "$ROOT/panterafitnes" && npm ci --no-audit --no-fund && NEXT_PUBLIC_API_URL="" npm run build)

echo "2/4 Compilando backend con el frontend incluido..."
(cd "$ROOT/panterafitnes-backend" && ./gradlew --no-daemon bootJar -PwithFrontend -x test)

echo "3/4 Descargando Java portable para Windows y verificando su firma SHA-256..."
mkdir -p "$WORK"
[ -f "$WORK/$JRE_FILE" ] || curl -fL -o "$WORK/$JRE_FILE" "$JRE_URL"
echo "$JRE_SHA256  $WORK/$JRE_FILE" | sha256sum -c -

echo "4/4 Armando el paquete..."
rm -rf "$PKG" && mkdir -p "$PKG/app" "$PKG/jre"
unzip -q "$WORK/$JRE_FILE" -d "$WORK/jre-tmp"
mv "$WORK"/jre-tmp/*/* "$PKG/jre/" && rm -rf "$WORK/jre-tmp"
cp "$ROOT/panterafitnes-backend/build/libs/pantera-fitness.jar" "$PKG/app/"
cp "$ROOT/distribucion/windows/"* "$PKG/"
(cd "$WORK" && rm -f ../PanteraFitness-Windows.zip && zip -qr ../PanteraFitness-Windows.zip PanteraFitness)
echo "Listo: dist/PanteraFitness-Windows.zip"
