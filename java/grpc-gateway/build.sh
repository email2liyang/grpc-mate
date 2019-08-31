#!/usr/bin/env bash

#set build dir
#rm -fr build
mkdir -p build
pushd build
#set go path to current dir
GOPATH=$(pwd)
echo "GOPATH=$GOPATH"
# get go dependencies
if [ ! -d src ]; then
    go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
    go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger
    go get -u github.com/golang/protobuf/protoc-gen-go
fi

cp -fr ../src/grpc-mate-gateway src
mkdir -p src/grpc-mate-gateway/proto
cp -fr ../../protobuffers/google  src/grpc-mate-gateway/proto/
cp -fr ../../protobuffers/*.proto  src/grpc-mate-gateway/proto/
mkdir -p src/grpc-mate-gateway/datanerd
#generate grpc stub
protoc -Isrc/grpc-mate-gateway/proto \
    -I$GOPATH/src \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
    --go_out=plugins=grpc:src/grpc-mate-gateway/datanerd \
    src/grpc-mate-gateway/proto/*.proto
#generate reversed proxy
protoc -Isrc/grpc-mate-gateway/proto \
    -I$GOPATH/src \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
    --grpc-gateway_out=logtostderr=true:src/grpc-mate-gateway/datanerd \
    src/grpc-mate-gateway/proto/*.proto

#build ghe grpc-gateway
pushd src/grpc-mate-gateway
go get -d -v
go install -v
popd

popd
