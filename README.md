gRPC-Mate - An enterprise ready micro service project base on [gRPC-Java](https://github.com/grpc/grpc-java)
========================================
gRPC-Mate demostrate best practice for gRPC based micro service.

[![Build Status](https://travis-ci.org/email2liyang/grpc-mate.svg?branch=master)](https://travis-ci.org/email2liyang/grpc-mate)

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
* Junit best practice
* Guice best practice
* [Proto buffer best practice](#proto-buffer-best-practice) 
* [Docker best practice](#docker-best-practice)
* [Quality control best practice](#quality-control-best-practice)
  * CheckStyle
  * FindBug
  * Jacoco

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
* [proto buffer name best practice](https://developers.google.com/protocol-buffers/docs/style)
  * use CamelCase (with an initial capital) for message names
  * use CamelCase (with an initial capital) for grpc service name
  * use underscore_separated_names for field names
  * use CamelCase (with an initial capital) for enum type names and CAPITALS_WITH_UNDERSCORES for value names
```proto
service ProductUpdateService {
    //upload product into elastic search , make it so that we could search on it
    //used to demo client side stream
    rpc UploadProduct (stream Product) returns (UploadProductResponse) {

    }
}


message UploadProductResponse {
    enum ResultStatus {
        SUCCESS = 0;
        FAILED = 1;
    }
    ResultStatus result_status = 1;
}
```  
### Docker best practice 
* we can use docker to simulate external service (e.g elasticsearch) in unit test
  * in this demo project , we will an [elasitcsearch image](https://github.com/email2liyang/elasticsearch-unit-image) for unit test purpose only
  * user can download it by command ```make pull_image``` to get latest test image

### Quality control best practice
* CheckStyle 
  * apply [Google Java Style] (http://checkstyle.sourceforge.net/google_style.html)
  * user can exclude any file from checkstyle(e.g: grpc generated java file) by adding it to gradle/google_checks_suppressions.xml
* FindBugs
  * user can exclude any file from findbugs(e.g: grpc generated java file) by adding it to findbugs_exclude_filter.xml
* Jacoco
  * Jacoco related tasks are not bind to check and test task, we can bind jacoco related tasks to test by 
```groovy
    test.finalizedBy(jacocoTestReport,jacocoTestCoverageVerification)
```    
  * use can add multiple rules in jacocoTestCoverageVerification
  * user can exclude any package from jacoco report in afterEvaluate config
```groovy
    afterEvaluate {
        classDirectories = files(classDirectories.files.collect {
            fileTree(dir: it,
                     exclude: ['**/generated/**',
                               'com/google/**'])
        })
    }
``` 
  * Line coverage ratio on package level is the most meaningful standard on code coverage perspective
  * Jacoco will work with Junit out of box, for TestNG, it need extra config to make jacoco to work.