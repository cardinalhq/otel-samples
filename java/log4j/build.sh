#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws
docker buildx build --platform linux/amd64,linux/arm64 -t public.ecr.aws/cardinalhq.io/demo/otel-log4j:latest --push .