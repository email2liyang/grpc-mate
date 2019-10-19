import logging

import grpc_mate.helloworld_pb2
import grpc_mate.helloworld_pb2_grpc

logger = logging.getLogger(__name__)


class GreeterServicer(grpc_mate.helloworld_pb2_grpc.GreeterServicer):

    def SayHello(self, request, context):
        logger.debug(f"get request {request.name}")
        return grpc_mate.helloworld_pb2.HelloReply(message=f"hello {request.name}")
