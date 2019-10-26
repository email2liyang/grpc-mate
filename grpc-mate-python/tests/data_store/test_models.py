from decimal import Decimal

import pytest
from faker import Faker

from data_store import engine
from data_store.db import session_scope
from data_store.models import Base, DBProduct
from grpc_mate.product_common_pb2 import InStock, Product


@pytest.fixture(autouse=True, scope='function')
def create_schema():
    if engine.url.__str__() == 'sqlite:///:memory:':
        Base.metadata.create_all(engine)


def test_db_products():
    faker = Faker()
    product = DBProduct(product_name=faker.name(), product_price=Decimal(faker.random_int() / 100),
                        product_status=InStock, category=faker.name())
    my_product = None
    with session_scope() as session:
        session.add(product)
        my_product = session.query(DBProduct).one()
        session.expunge(my_product)
        assert my_product.product_id is not None
        assert my_product.product_name == product.product_name
        assert my_product.product_price == product.product_price
        assert my_product.product_status == product.product_status
        assert my_product.category == product.category

    # copy the db product to grpc product
    grpc_product = Product()
    for k in grpc_product.DESCRIPTOR.fields_by_name:
        setattr(grpc_product, k, my_product.__dict__[k])
