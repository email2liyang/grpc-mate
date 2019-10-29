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
                db_product = DBProduct(product_name=product.product_name,
                                       product_price=product.product_price,
                                       product_status=product.product_status,
                                       category=product.category)
                # TODO(IL) test it out
                # for k in product.DESCRIPTOR.fields_by_name:
                #     setattr(db_product, k, product.__dict__[k])
                # db_product.product_id = None
                session.add(db_product)

        return UploadProductResponse(result_status=UploadProductResponse.SUCCESS)
