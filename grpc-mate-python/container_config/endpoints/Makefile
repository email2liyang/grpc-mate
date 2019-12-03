build: clean
	protoc --include_imports \
		--proto_path=../../protobuffers \
		--descriptor_set_out helloworld.pb \
		../../protobuffers/grpc_mate/helloworld.proto

config_list:
	rm ../../.python-version
	gcloud endpoints configs list --service greeter.endpoints.face-prod.cloud.goog
	echo "3.6.8" > ../../.python-version

clean:
	rm -f *.pb

update_prod_spec: build
	rm ../../.python-version
	gcloud endpoints services deploy helloworld.pb endpoint.yaml
	echo "3.6.8" > ../../.python-version