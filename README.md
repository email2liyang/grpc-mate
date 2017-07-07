gRPC-Mate - An enterprise ready micro service project base on gRPC-Java
========================================
gRPC-Mate demostrate best practice for gRPC based micro service.

* Grpc features
  * Simple RPC
  * Server streaming
  * Client streaming
  * Bi-directional streaming
  * Authentication
* Promethues integration
* Kubernetes Deployment
* Gradle multiple builds best practice
* Mockito best practice
* TestNG best practice
* Guice best practice
* Docker-Java best practice
* Protobuffer best practice 
  * use Protobuffer message as value object
  * use Protobuffer message to persist into Elasticsearch
* Quality Control best practice
  * CheckStyle
  * FindBug
  * PMD

### Demo  Script
the project will demostrate an online store search service including

* Create elasticsearch index with alias
* Uploading products into Elasticsearch (client streaming)
* Downloading products from Elasticsearch (server streaming)
* Search products from elasticsearch (simple RPC)
* Calculate products score (bi-directional streaming)

 