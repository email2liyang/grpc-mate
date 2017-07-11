pull_image:
	docker pull email2liyang/elasticsearch-unit-image:5.4.3
build:
	./gradlew build
travis_build: pull_image build