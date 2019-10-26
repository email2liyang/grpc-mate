import pytest

from data_store import engine
from data_store.db import session_scope
from data_store.models import Base, DBProduct
from grpc_mate.product_common_pb2 import InStock


@pytest.fixture(autouse=True, scope='function')
def create_schema():
    if engine.url.__str__() == 'sqlite:///:memory:':
        Base.metadata.create_all(engine)


def test_db_products():
    product = DBProduct(product_name='iPhone', product_price=2015.60, product_status=InStock, category='IT')
    with session_scope() as session:
        session.add(product)
        my_product = session.query(DBProduct).one()
        assert my_product.product_id is not None
        assert my_product.product_name == product.product_name
        assert my_product.product_price == product.product_price
        assert my_product.product_status == product.product_status
        assert my_product.category == product.category