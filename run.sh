#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

# Use JavaFX plugin so the JavaFX runtime is on the module path.
mvn -q clean javafx:run
