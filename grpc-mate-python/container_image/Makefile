build:
	rm -f grpc-mate-python.tar
	cp ../target/grpc-mate-python.tar .
	docker build -t us.gcr.io/face-staging/grpc-mate-python:1.0.0 .

push:
	docker push us.gcr.io/face-staging/grpc-mate-python:1.0.0

run:
	docker run --name grpc-mate-python -p 8080:8080 -d us.gcr.io/face-staging/grpc-mate-python:1.0.0

rm:
	docker rm -f grpc-mate-python

shell: run
	docker exec -it grpc-mate-python bash