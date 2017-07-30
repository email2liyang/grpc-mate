gRPC-Mate - An enterprise ready micro service project base on [gRPC-Java](https://github.com/grpc/grpc-java)
========================================
gRPC-Mate demostrate best practice for gRPC based micro service.

[![Build Status](https://travis-ci.org/email2liyang/grpc-mate.svg?branch=master)](https://travis-ci.org/email2liyang/grpc-mate)
[![Code Coverage Status](https://s3.amazonaws.com/assets.coveralls.io/badges/coveralls_94.svg)](https://coveralls.io/github/email2liyang/grpc-mate?branch=master)

* [Grpc best practice](#grpc-best-practice)
  * [Simple RPC](#simple-rpc)
  * [Server streaming](#server-streaming)
  * [Client streaming](#client-streaming)
  * [Bi-directional streaming](#bi-directional-streaming)
* [Promethues integration](#promethues-integration)
* [Kubernetes Deployment](#kubernetes-deployment)
* [Gradle multiple builds best practice](#gradle-best-practice)
* [Mockito best practice](#mockito-best-practice)
* [Junit best practice](#junit-best-practice)
* [Proto buffer best practice](#proto-buffer-best-practice) 
* [Docker best practice](#docker-best-practice)
* [Quality control best practice](#quality-control-best-practice)
  * [CheckStyle](#checkstyle)
  * [FindBug](#findbug)
  * [Jacoco](#jacoco)

### Demo  Script
the project will demostrate an online store search service including

* Create elasticsearch index with alias
* Uploading products into Elasticsearch (client streaming)
* Downloading products from Elasticsearch (server streaming)
* Search products from elasticsearch (simple RPC)
* Calculate products score (bi-directional streaming)
### Grpc best practice
* elastic search communicate
  * use JsonFormat.Printer to convert proto buffer message into json
  * use JsonFormat.Parser to parse json into proto buffer 
#### Simple RPC
* [sample](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/ProductReadService.java#L23)
* we could use JsonFormat.Parser to convert es document into protobuf message
```java
      Product.Builder builder = Product.newBuilder();
      jsonParser.merge(hit.getSourceAsString(), builder);
      responseBuilder.addProducts(builder.build());
```
#### Server streaming
* [sample](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/ProductReadService.java#L39)
* with server streaming , user could pass PublishSubject to dao layer to connect the real data with ResponseObserver
```java
PublishSubject<Product> productPublishSubject = PublishSubject.create();
    Disposable disposable = productPublishSubject
        .doOnNext(product -> responseObserver.onNext(product))
        .doOnComplete(() -> responseObserver.onCompleted())
        .doOnError(t -> responseObserver.onError(t))
        .subscribe();
    productDao.downloadProducts(request, productPublishSubject);
    disposable.dispose();
``` 
#### Client streaming
* [sample](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/ProductUpdateService.java#L29)
* use [RxStreamObserver](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/RxStreamObserver.java) to connect grpc StreamObserver and [rxJava](https://github.com/ReactiveX/RxJava) so that in grpc service, we could use rx style programming
```java
PublishSubject<Product> publishSubject = PublishSubject.create();
    publishSubject
        .doOnNext(product -> {
          log.info("saving product - {} ", product);
          productDao.upsertProduct(product);
        })
        .doOnError(t -> responseObserver.onError(t))
        .doOnComplete(() -> {
          responseObserver.onNext(UploadProductResponse.newBuilder().build());
          responseObserver.onCompleted();
        })
        .subscribe();
```
#### Bi-directional streaming
* [sample](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/ProductReadService.java#L49)
* use grpc's InProcessServer to test grpc service
### Promethues integration
* use [Auto Value](https://github.com/google/auto/tree/master/value) to define the value class with builder, see [Metric.java](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/metrics/Metric.java)
* use [CounterFactory.java](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/metrics/CounterFactory.java) to normalize Prometheus Counter's path and instance
* use CounterFactory to create counter and use the counter to record service metrics see [ProductReadService.java](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/service/ProductReadService.java)
* use [NanoHttpD](https://github.com/NanoHttpd/nanohttpd) based [HttpServer.java](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/main/java/io/datanerd/es/server/HttpServer.java) to serve metrics and grpc health info
### Kubernetes Deployment
* [sample](https://github.com/email2liyang/grpc-mate/tree/master/elasticsearch-service/deployment)
* use property file to manage system property and add the system property to configmap, so it's easy to debug program locally by specify the property file from system env.
```
kubectl create configmap cluster-config --from-file=data_nerd.properties --namespace=prod
```
* mount property from configmap in deploymnet yaml file
```
volumes:
      - name: config-volume
        configMap:
          name: cluster-config
          items:
          - key: data_nerd.properties
            path: data_nerd.properties
```
* service will seldom get redeployed after first deployment

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
* show error log in console make it easier to debug build failure in travis-ci
```groovy
test {
    testLogging {
        // set options for log level LIFECYCLE
        events "failed"
        exceptionFormat "full"

        // remove standard output/error logging from --info builds
        // by assigning only 'failed' and 'skipped' events
        info.events = ["failed", "skipped"]
    }
}
```
### Mockito best practice
* use Mockito to mock dao method in service test, so that we do not launch docker container to provide ES env
* use Guice to inject any mocked instance into the dependency graph in unit test
```java
productDao = mock(ProductDao.class);
    injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule())
            .with(binder -> {
              binder.bind(ProductDao.class).toInstance(productDao);
            })
    );
```
### Junit best practice
* use [testcontainers-java](https://github.com/testcontainers/testcontainers-java), we could launch any docker image to support any env related class
* it's convenient to use JUnit Rule and ClassRule with docker container for test see [TransportClientProviderTest.java](https://github.com/email2liyang/grpc-mate/blob/master/elasticsearch-service/src/test/java/io/datanerd/es/guice/TransportClientProviderTest.java) for more details
```java
  @ClassRule
  public static final GenericContainer esContainer = 
      new GenericContainer("email2liyang/elasticsearch-unit-image:5.4.3")
        .withExposedPorts(9200,9300);
```
* user can use Guice Modules.override() method to override any default configuration in test
```java
MapConfiguration memoryParams = new MapConfiguration(new HashMap<>());
    memoryParams.setProperty(CONFIG_ES_CLUSTER_HOST,ip);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_PORT,transportPort);
    memoryParams.setProperty(CONFIG_ES_CLUSTER_NAME,"elasticsearch");
    Injector injector = Guice.createInjector(
        Modules.override(new ElasticSearchModule()).with(
            binder -> {
              binder.bind(Configuration.class).toProvider(() -> memoryParams);
            }
        )
    );
```
* use toProvider(()->xxx); to avoid dedicated provider logic to execute


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
#### CheckStyle 
* apply [Google Java Style] (http://checkstyle.sourceforge.net/google_style.html)
* user can exclude any file from checkstyle(e.g: grpc generated java file) by adding it to gradle/google_checks_suppressions.xml
#### FindBugs
* user can exclude any file from findbugs(e.g: grpc generated java file) by adding it to findbugs_exclude_filter.xml
#### Jacoco
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