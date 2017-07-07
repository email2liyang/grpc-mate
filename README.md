gRPC-Mate - An enterprise ready micro service project base on [gRPC-Java](https://github.com/grpc/grpc-java)
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
* [Gradle multiple builds best practice](#gradle-best-practice)
* Mockito best practice
* TestNG best practice
* Guice best practice
* Docker-Java best practice
* [Proto buffer best practice](#proto-buffer-best-practice) 
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

### Gradle Best Practice
* add gradle wrapper, so that it can be run anywhere

```
task wrapper(type: Wrapper) {
    gradleVersion = '4.0'
}

> gradle wrapper
```
* remove auto generated classes in clean task

```groovy
clean {
    doLast {
        // remove auto-generated files on clean
        delete "${projectDir}/src/generated"
    }
}
```
* we force gradle to detect version conflict on build

```groovy
subprojects {
    apply plugin: 'java'

    configurations.all {
        resolutionStrategy {
            // fail eagerly on version conflict (includes transitive dependencies)
            // e.g. multiple different versions of the same dependency (group and name are equal)
            failOnVersionConflict()
        }
    }
}
```
### Proto buffer best practice
* define all proto file in top level of project for larger organization, it's a good idea to store all protobuffer file into a dedicated git repository, then checkout the proto buffer repository as a git submodule, then we could have single place to define all the grpc service and message to share across projects
* define Makefile to generate java code , then it's easy to detect any issue for proto buffer definition.
```
clean:
	mkdir -p java_generated && rm -rf java_generated/*
gen: clean
	protoc --java_out=java_generated *.proto
> make gen	
```
* it's good idea to use proto buffer message as value object to pass value among different layer of the application, then the developers do not need to care about marshalling/unmarshalling in different layer. let protobuffer to handle it in a reliable and fast way. 
* we could use JsonFormat.Printer and JsonFormat.Parser to serialize/deserialize proto buffer message into/from json to communicate with elasticsearch, as elastic search only support json format of data as it's document
* it's good idea to define common message in a separate proto file, so that it can be used in multiple proto files by import
* it's good idea to define package name and set multiple_files to true so that the generated java file has better package name
```
option java_package = "io.datanerd.generated.common";
option java_multiple_files = true;
```
 