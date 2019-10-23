import logging

import grpc_mate.product_search_engine_pb2_grpc

logger = logging.getLogger(__name__)


class ProductUpdateServiceServicer(grpc_mate.product_search_engine_pb2_grpc.ProductUpdateServiceServicer):
    def UploadProduct(self, request_iterator, context):
        return super().UploadProduct(request_iterator, context)
