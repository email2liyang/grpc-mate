import filecmp
import os
from decimal import Decimal
from pathlib import Path

import pytest
from faker import Faker

from data_store import engine
from data_store.db import session_scope
from data_store.models import Base, DBProduct
from grpc_mate.product_common_pb2 import InStock
from grpc_mate.product_search_engine_pb2 import SearchProductsRequest, DownloadProductsRequest, \
    DownloadProductImageRequest


@pytest.fixture(scope='module')
def grpc_add_to_server():
    from grpc_mate.product_search_engine_pb2_grpc import add_ProductReadServiceServicer_to_server
    return add_ProductReadServiceServicer_to_server


@pytest.fixture(scope='module')
def grpc_servicer():
    from service.product_read_servicer import ProductReadServiceServicer

    return ProductReadServiceServicer()


@pytest.fixture(scope='module')
def grpc_stub_cls(grpc_channel):
    from grpc_mate.product_search_engine_pb2_grpc import ProductReadServiceStub

    return ProductReadServiceStub


@pytest.fixture(autouse=True, scope='function')
def create_schema():
    if engine.url.__str__() == 'sqlite:///:memory:':
        Base.metadata.create_all(engine)
        yield None
        Base.metadata.drop_all(engine)


def test_SearchProducts_none_exist(grpc_stub):
    faker = Faker()
    keyword = faker.name()

    response = grpc_stub.SearchProducts(SearchProductsRequest(key_word=keyword, limit=2))
    assert len(response.products) == 0


def test_SearchProducts_exist(grpc_stub):
    faker = Faker()
    keyword = faker.name()
    product = DBProduct(product_name=keyword,
                        product_price=Decimal(faker.random_int() / 100),
                        product_status=InStock,
                        category=faker.name())
    # save to db
    with session_scope() as session:
        session.add(product)

    response = grpc_stub.SearchProducts(SearchProductsRequest(key_word=keyword, limit=2))
    assert len(response.products) == 1
    assert keyword in response.products[0].product_name


def test_SearchProducts_limit(grpc_stub):
    faker = Faker()
    keyword = faker.name()
    # save to db
    with session_scope() as session:
        for idx in range(5):
            product = DBProduct(product_name=f'{keyword}_{idx}',
                                product_price=Decimal(faker.random_int() / 100),
                                product_status=InStock,
                                category=faker.name())
            session.add(product)

    response = grpc_stub.SearchProducts(SearchProductsRequest(key_word=keyword, limit=2))
    assert len(response.products) == 2
    assert keyword in response.products[0].product_name
    assert keyword in response.products[1].product_name


def test_DownloadProducts_exist(grpc_stub):
    faker = Faker()
    category = faker.name()
    # save to db
    with session_scope() as session:
        for idx in range(5):
            product = DBProduct(product_name=f'{faker.name()}_{idx}',
                                product_price=Decimal(faker.random_int() / 100),
                                product_status=InStock,
                                category=category)
            session.add(product)
    result = grpc_stub.DownloadProducts(DownloadProductsRequest(category=category))

    # assert we have 5 items
    assert len(list(result)) == 5


def test_DownloadProducts_none_exist(grpc_stub):
    faker = Faker()
    category = faker.name()
    result = grpc_stub.DownloadProducts(DownloadProductsRequest(category=category))

    # assert we have 0 items
    assert len(list(result)) == 0


def test_DownloadProductImage(grpc_stub):
    faker = Faker()
    target_image_file = faker.file_name(category=None, extension='png')
    data_chunks = grpc_stub.DownloadProductImage(DownloadProductImageRequest(product_id=1))
    with open(target_image_file, 'wb') as f:
        for chunk in data_chunks:
            f.write(chunk.data)

    original_image_file = Path(__file__).resolve().parent.parent.parent.joinpath('images/python-grpc.png')
    assert filecmp.cmp(original_image_file, target_image_file)
    os.remove(target_image_file)
