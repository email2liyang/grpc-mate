from sqlalchemy import Column, SMALLINT, Integer, String, DECIMAL, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship

from data_store import engine
from data_store.db import session_scope

Base = declarative_base()


class DBProduct(Base):
    __tablename__ = 'products'
    product_id = Column(Integer, primary_key=True)
    product_name = Column(String(200))
    product_price = Column(DECIMAL(10, 2))
    product_status = Column(SMALLINT)
    category = Column(String(50))



def msg():
    print("in models")


if __name__ == '__main__':
    create_schema()
    create_user()
