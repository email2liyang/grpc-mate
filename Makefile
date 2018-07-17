pull_image:
	docker pull email2liyang/elasticsearch-unit-image:5.4.3-1
build:
	./gradlew build distTar
imageESService:
	cp elasticsearch-service/build/distributions/elasticsearch-service.tar elasticsearch-service/deployment/
	pushd elasticsearch-service/deployment && \
	docker build -t email2liyang/elasticsearch-service:1.0.0 . && \
	rm elasticsearch-service.tar && \
	popd
imageHelloService:
	cp helloworld-service/build/distributions/helloworld-service.tar helloworld-service/deployment/
	pushd helloworld-service/deployment && \
	docker build -t email2liyang/helloworld-service:1.0.0 . && \
	rm helloworld-service.tar && \
	popd
coveralls:
	./gradlew jacocoTestReport coveralls