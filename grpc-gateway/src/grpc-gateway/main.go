package main

import (
	"flag"
	"net/http"

	"github.com/golang/glog"
	"golang.org/x/net/context"
	"github.com/grpc-ecosystem/grpc-gateway/runtime"
	"google.golang.org/grpc"

	productSearchGrpc "grpc-gateway/datanerd"
)

var (
	echoEndpoint = flag.String("product_search_endpoint", "localhost:8080", "endpoint of Product Search")
)

func run() error {
	ctx := context.Background()
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()

	mux := runtime.NewServeMux()
	opts := []grpc.DialOption{grpc.WithInsecure()}
	err := productSearchGrpc.RegisterProductReadServiceHandlerFromEndpoint(ctx, mux, *echoEndpoint, opts)
	if err != nil {
		return err
	}

	return http.ListenAndServe(":9090", mux)
}

func main() {
	flag.Parse()
	defer glog.Flush()

	if err := run(); err != nil {
		glog.Fatal(err)
	}
}
