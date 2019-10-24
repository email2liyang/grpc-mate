from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String
from data_store.db import engine

Base = declarative_base()


class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    name = Column(String)
    fullname = Column(String(50))
    nickname = Column(String(50))

    def __repr__(self) -> str:
        return f'<User(id={self.id},name={self.name},fullname={self.fullname},nickname={self.nickname}>'


Base.metadata.create_all(engine)

