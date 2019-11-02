import logging

import grpc_mate.product_search_engine_pb2_grpc
from data_store.db import session_scope
from data_store.models import DBProduct
from grpc_mate.product_common_pb2 import Product
from grpc_mate.product_search_engine_pb2 import SearchProductsResponse

logger = logging.getLogger(__name__)


def db_product_to_protobuf_product(db_product):
    # copy the db product to grpc product
    protobuf_product = Product()
    for k in protobuf_product.DESCRIPTOR.fields_by_name:
        setattr(protobuf_product, k, db_product.__dict__[k])
    return protobuf_product


class ProductReadServiceServicer(grpc_mate.product_search_engine_pb2_grpc.ProductReadServiceServicer):
    def DownloadProducts(self, request, context):
        return super().DownloadProducts(request, context)

    def SearchProducts(self, request, context):
        with session_scope() as session:
            result = session.query(DBProduct) \
                .filter(DBProduct.product_name.like(f'%{request.key_word}%')) \
                .order_by(DBProduct.product_id.asc()) \
                .limit(limit=request.limit) \
                .all()
            products = list(map(db_product_to_protobuf_product, result))
            return SearchProductsResponse(products=products)

    def CalculateProductScore(self, request_iterator, context):
        return super().CalculateProductScore(request_iterator, context)

    def DownloadProductImage(self, request, context):
        return super().DownloadProductImage(request, context)
