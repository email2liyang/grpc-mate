import logging

import grpc_mate.product_search_engine_pb2_grpc
from data_store.db import session_scope
from data_store.models import DBProduct
from grpc_mate.product_search_engine_pb2 import UploadProductResponse

logger = logging.getLogger(__name__)


class ProductUpdateServiceServicer(grpc_mate.product_search_engine_pb2_grpc.ProductUpdateServiceServicer):
    def UploadProduct(self, request_iterator, context):
        with session_scope() as session:
            for product in request_iterator:
                db_product = DBProduct()
                for k in product.DESCRIPTOR.fields_by_name:
                    setattr(db_product, k, getattr(product, k))
                db_product.product_id = None
                session.add(db_product)

        return UploadProductResponse(result_status=UploadProductResponse.SUCCESS)
