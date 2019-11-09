import logging
from pathlib import Path

import grpc_mate.product_search_engine_pb2_grpc
from data_store.db import session_scope
from data_store.models import DBProduct
from grpc_mate.product_common_pb2 import Product, DataChunk
from grpc_mate.product_search_engine_pb2 import SearchProductsResponse, CalculateProductScoreResponse

logger = logging.getLogger(__name__)


def db_product_to_protobuf_product(db_product):
    # copy the db product to grpc product
    protobuf_product = Product()
    for k in protobuf_product.DESCRIPTOR.fields_by_name:
        setattr(protobuf_product, k, db_product.__dict__[k])
    return protobuf_product


class ProductReadServiceServicer(grpc_mate.product_search_engine_pb2_grpc.ProductReadServiceServicer):
    def DownloadProducts(self, request, context):
        with session_scope() as session:
            result = session.query(DBProduct) \
                .filter(DBProduct.category == request.category) \
                .all()
            for product in result:
                yield db_product_to_protobuf_product(product)

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
        for product in request_iterator:
            yield CalculateProductScoreResponse(product=product, score=int(product.product_price * 2))

    def DownloadProductImage(self, request, context):
        chunk_size = 1024
        image_path = Path(__file__).resolve().parent.parent.joinpath('images/python-grpc.png')

        with image_path.open('rb') as f:
            while True:
                chunk = f.read(chunk_size)
                if not chunk:
                    break
                yield DataChunk(data=chunk)
