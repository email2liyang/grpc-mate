import logging.config
import yaml

from concurrent import futures

import grpc

import grpc_mate.helloworld_pb2_grpc
from service.greeter_servicer import GreeterServicer
import grpc_mate.product_search_engine_pb2_grpc
from service.product_update_servicer import ProductUpdateServiceServicer

# Create a custom logger
with open('logging.yaml', 'r') as f:
    config = yaml.safe_load(f.read())
    logging.config.dictConfig(config)

logger = logging.getLogger(__name__)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    grpc_mate.helloworld_pb2_grpc.add_GreeterServicer_to_server(GreeterServicer(), server)
    grpc_mate.product_search_engine_pb2_grpc.add_ProductUpdateServiceServicer_to_server(ProductUpdateServiceServicer(),
                                                                                        server)
    server.add_insecure_port('[::]:8080')
    server.start()
    logger.debug('grpc server started at port 8080')
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
