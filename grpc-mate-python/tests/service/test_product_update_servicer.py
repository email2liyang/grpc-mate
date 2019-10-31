import pytest

from data_store import engine
from data_store.models import Base, DBProduct
from data_store.db import session_scope
from grpc_mate.product_common_pb2 import Product, InStock
from grpc_mate.product_search_engine_pb2 import UploadProductResponse


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


@pytest.fixture(autouse=True, scope='function')
def create_schema():
    if engine.url.__str__() == 'sqlite:///:memory:':
        Base.metadata.create_all(engine)
        yield None
        Base.metadata.drop_all(engine)



def test_UploadProduct_insert_one(grpc_stub):
    products = [
        Product(product_name='product_name_1', product_price=1.0, product_status=InStock, category='category_1')]
    grpc_stub.UploadProduct(iter(products))
    with session_scope() as session:
        rows = session.query(DBProduct).count()
        assert rows == 1
        my_product = session.query(DBProduct).one()
        product = products[0]
        assert my_product.product_id is not None
        assert my_product.product_name == product.product_name
        assert my_product.product_price == product.product_price
        assert my_product.product_status == product.product_status
        assert my_product.category == product.category


def test_UploadProduct_insert_two(grpc_stub):
    products = [
        Product(product_name='product_name_1', product_price=1.0, product_status=InStock, category='category_1'),
        Product(product_name='product_name_2', product_price=2.0, product_status=InStock, category='category_2')]
    grpc_stub.UploadProduct(iter(products))
    with session_scope() as session:
        rows = session.query(DBProduct).count()
        assert rows == 2


def test_UploadProductResponse_enum():
    """
    see https://developers.google.com/protocol-buffers/docs/reference/python-generated#enum on how to use enum
    see https://github.com/protocolbuffers/protobuf/blob/master/python/google/protobuf/internal/enum_type_wrapper.py
        for method in all enums
    :return:
    """
    upload_product_response = UploadProductResponse(result_status=UploadProductResponse.SUCCESS)
    assert upload_product_response.result_status == UploadProductResponse.ResultStatus.Value('SUCCESS')
