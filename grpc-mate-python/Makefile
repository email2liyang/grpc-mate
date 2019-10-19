freeze:
	pipenv lock -r > requirements.txt

protoc:
	rm -fR grpc_mate/* && \
	python  -m grpc_tools.protoc -Iprotobuffers --python_out=. --grpc_python_out=.  protobuffers/grpc_mate/*.proto && \
	touch grpc_mate/__init__.py