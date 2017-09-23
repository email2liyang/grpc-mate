#!/usr/bin/env bash

#set build dir
#rm -fr build
#mkdir -p build
pushd build
#set go path to current dir
GOPATH=$(pwd)
echo "GOPATH=$GOPATH"
# get go dependencies
#go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
#go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger
#go get -u github.com/golang/protobuf/protoc-gen-go

cp -fr ../src/grpc-gateway src
mkdir -p src/grpc-gateway/proto
cp -fr ../../protobuffers/google  src/grpc-gateway/proto/
cp -fr ../../protobuffers/*.proto  src/grpc-gateway/proto/
mkdir -p src/grpc-gateway/datanerd
#generate grpc stub
protoc -Igrpc-gateway/proto \
    -I$GOPATH/src \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
    --go_out=src/grpc-gateway/datanerd \
    grpc-gateway/proto/*.proto
#generate reversed proxy
protoc -Igrpc-gateway/proto \
    -I$GOPATH/src \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
    --grpc-gateway_out=logtostderr=true:src/grpc-gateway/datanerd \
    grpc-gateway/proto/*.proto

#build ghe grpc-gateway
pushd src/grpc-gateway
go get -d -v
#go install -v
popd

popd
