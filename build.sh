#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC_MAIN="$PROJECT_ROOT/src/main/java"
SRC_TEST="$PROJECT_ROOT/src/test/java"
OUT_MAIN="$PROJECT_ROOT/out/classes"
OUT_TEST="$PROJECT_ROOT/out/test-classes"
JAR_DIR="$PROJECT_ROOT/out/jar"
MAIN_CLASS="com.example.hotel.HotelApplication"
TEST_MAIN_CLASS="com.example.hotel.service.ReservationServiceTest"

mkdir -p "$OUT_MAIN" "$OUT_TEST" "$JAR_DIR"

echo "Compiling main sources..."
find "$OUT_MAIN" -type f -name '*.class' -delete || true
javac -encoding UTF-8 -Xlint:unchecked -Xlint:deprecation \
  -d "$OUT_MAIN" \
  $(find "$SRC_MAIN" -name '*.java')

echo "Compiling test sources..."
find "$OUT_TEST" -type f -name '*.class' -delete || true
javac -encoding UTF-8 -Xlint:unchecked -Xlint:deprecation \
  -cp "$OUT_MAIN" \
  -d "$OUT_TEST" \
  $(find "$SRC_TEST" -name '*.java')

# Create a simple runnable jar (main classes only)
JAR_FILE="$JAR_DIR/hotel-app.jar"
pushd "$OUT_MAIN" >/dev/null
jar --create --file "$JAR_FILE" --main-class "$MAIN_CLASS" $(find . -type f -name '*.class') >/dev/null
popd >/dev/null

echo "Running test harness..."
set +e
java -cp "$OUT_MAIN:$OUT_TEST" "$TEST_MAIN_CLASS"
TEST_STATUS=$?
set -e
if [ $TEST_STATUS -ne 0 ]; then
  echo "Tests failed (exit $TEST_STATUS)" >&2
  exit $TEST_STATUS
fi

echo "Build successful. Jar: $JAR_FILE"

