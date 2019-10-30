import pytest

from grpc_mate.product_common_pb2 import Product, InStock
from grpc_mate.product_search_engine_pb2 import UploadProductResponse

from data_store import engine
from data_store.models import Base


@pytest.fixture(scope='module')
def grpc_add_to_server():
    from grpc_mate.product_search_engine_pb2_grpc import add_ProductUpdateServiceServicer_to_server
    return add_ProductUpdateServiceServicer_to_server


@pytest.fixture(scope='module')
def grpc_servicer():
    from service.product_update_servicer import ProductUpdateServiceServicer

    return ProductUpdateServiceServicer()


@pytest.fixture(scope='module')
def grpc_stub_cls(grpc_channel):
    from grpc_mate.product_search_engine_pb2_grpc import ProductUpdateServiceStub

    return ProductUpdateServiceStub

@pytest.fixture(autouse=True, scope='module')
def create_schema():
    if engine.url.__str__() == 'sqlite:///:memory:':
        Base.metadata.create_all(engine)

def test_UploadProduct(grpc_stub):
    products = [
        Product(product_name='product_name_1', product_price=1.0, product_status=InStock, category='category_1')]
    grpc_stub.UploadProduct(iter(products))


def test_UploadProductResponse_enum():
    """
    see https://developers.google.com/protocol-buffers/docs/reference/python-generated#enum on how to use enum
    see https://github.com/protocolbuffers/protobuf/blob/master/python/google/protobuf/internal/enum_type_wrapper.py
        for method in all enums
    :return:
    """
    upload_product_response = UploadProductResponse(result_status=UploadProductResponse.SUCCESS)
    assert upload_product_response.result_status == UploadProductResponse.ResultStatus.Value('SUCCESS')
