#!/usr/bin/env bash
set -euo pipefail
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
if [ ! -d "$PROJECT_ROOT/out/classes" ]; then
  echo "No build output found. Building first..."
  "$PROJECT_ROOT/build.sh"
fi
java -cp "$PROJECT_ROOT/out/classes" com.example.hotel.HotelApplication

