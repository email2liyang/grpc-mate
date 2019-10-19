#!/usr/bin/env bash
# config map
kubectl delete configmap cluster-config --namespace=prod
kubectl create configmap cluster-config --from-file=data_nerd.properties --namespace=prod
# deployment
kubectl apply -f deployment.yaml
# service
kubectl apply -f service.yaml