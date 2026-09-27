#!/bin/bash
set -e

mkdir -p out
javac -d out $(find src/main -name "*.java")
javac -cp out -d out $(find src/test -name "*.java")

echo "Running correctness tests..."
java -cp out com.aitu.ds.Tests

echo "Running benchmarks (this can take a few minutes for n=100000)..."
java -cp out com.aitu.ds.Benchmark

echo "Generating plots..."
python3 -m pip install --quiet matplotlib 2>/dev/null || true
python3 plot_results.py

echo "Done. See results/tables and results/plots."
