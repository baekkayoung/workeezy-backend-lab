#!/bin/bash
set -e

APP_DIR=/home/ubuntu/workeezy-backend
REGION=ap-northeast-2
ECR_REGISTRY=020513637952.dkr.ecr.ap-northeast-2.amazonaws.com/workeezy-server

echo "Move to app directory"
cd $APP_DIR

echo "ECR login"
aws ecr get-login-password --region $REGION \
 | sudo docker login --username AWS --password-stdin $ECR_REGISTRY

echo "Clean unused Docker images, containers, and cache"
sudo docker system prune -af || true
sudo docker builder prune -af || true

echo "Pull latest images"
sudo docker compose pull

echo "Stop & remove old containers"
sudo docker compose down --remove-orphans

echo "Start containers (app + redis)"
sudo docker compose up -d

echo "Deploy finished"