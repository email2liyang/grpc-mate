import logging.config
from concurrent import futures
from pathlib import Path

import grpc
import yaml

import grpc_mate.helloworld_pb2_grpc
import grpc_mate.product_search_engine_pb2_grpc
from service.greeter_servicer import GreeterServicer
from service.product_read_servicer import ProductReadServiceServicer
from service.product_update_servicer import ProductUpdateServiceServicer

# Create a custom logger
with Path(__file__).resolve().parent.joinpath('logging.yaml').open('r') as f:
    config = yaml.safe_load(f.read())
    logging.config.dictConfig(config)

logger = logging.getLogger(__name__)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    grpc_mate.helloworld_pb2_grpc.add_GreeterServicer_to_server(GreeterServicer(), server)
    grpc_mate.product_search_engine_pb2_grpc.add_ProductUpdateServiceServicer_to_server(ProductUpdateServiceServicer(),
                                                                                        server)
    grpc_mate.product_search_engine_pb2_grpc.add_ProductReadServiceServicer_to_server(ProductReadServiceServicer(),
                                                                                      server)
    server.add_insecure_port('[::]:8080')
    server.start()
    logger.debug('grpc server started at port 8080')
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
