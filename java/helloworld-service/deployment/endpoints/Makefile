build: clean
	protoc --include_imports \
		--proto_path=../../src/main/proto \
		--descriptor_set_out helloworld_api.pb \
		../../src/main/proto/helloworld.proto

config_list:
	gcloud endpoints configs list --service helloworld-service.endpoints.pi-devops.cloud.goog

clean:
	rm -f *.pb

update_spec: build
	gcloud endpoints services deploy helloworld_api.pb api_service.yaml