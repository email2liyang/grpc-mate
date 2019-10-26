from sqlalchemy import Column, SMALLINT, Integer, String, DECIMAL
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class DBProduct(Base):
    __tablename__ = 'products'
    product_id = Column(Integer, primary_key=True)
    product_name = Column(String(200))
    product_price = Column(DECIMAL(10, 2))
    product_status = Column(SMALLINT)
    category = Column(String(50))
