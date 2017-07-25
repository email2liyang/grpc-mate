pull_image:
	docker pull email2liyang/elasticsearch-unit-image:5.4.3-1
build:
	./gradlew build distTar
coveralls:
	./gradlew jacocoTestReport coveralls