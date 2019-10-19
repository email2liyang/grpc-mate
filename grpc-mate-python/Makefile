freeze:
	pipenv lock -r > requirements.txt

protoc:
	rm -fR grpc_mate/* && \
	rm -fR google/* && \
	python  -m grpc_tools.protoc -Iprotobuffers --python_out=. --grpc_python_out=.  protobuffers/grpc_mate/*.proto protobuffers/google/api/*.proto && \
	touch grpc_mate/__init__.py
	touch google/__init__.py
	touch google/api/__init__.py