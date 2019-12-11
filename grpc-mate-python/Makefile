freeze:
	pipenv lock -r > requirements.txt

protoc:
	rm -fR grpc_mate/* && \
	rm -fR google/* && \
	python  -m grpc_tools.protoc -Iprotobuffers --python_out=. --grpc_python_out=.  protobuffers/grpc_mate/*.proto protobuffers/google/api/*.proto && \
	touch grpc_mate/__init__.py
	touch google/__init__.py
	touch google/api/__init__.py

pytest:
	pytest --grpc-fake-server

style:
	pycodestyle service data_store server

clean:
	find data_store/ google/ grpc_mate/ server/ service/ -name "__pycache__" -type d  -exec rm -r "{}" \;
tar:
	mkdir -p target/
	rm -fR target/*
	tar cvf grpc-mate-python.tar data_store/ google/ grpc_mate/ server/ service/ requirements.txt
	mv grpc-mate-python.tar target/