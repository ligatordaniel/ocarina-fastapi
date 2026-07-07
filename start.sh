#!/bin/bash
set -e

echo "Starting Ocarina API..."
uvicorn main:app --host 0.0.0.0 --port 8081 &

echo "Starting Ocarina Worker..."
python worker.py
