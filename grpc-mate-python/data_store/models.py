from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.orm import relationship
from data_store.db import engine, get_session

Base = declarative_base()


class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    name = Column(String)
    fullname = Column(String(50))
    nickname = Column(String(50))

    addresses = relationship("Address", back_populates="user")

    def __repr__(self) -> str:
        return f'<User(id={self.id},name={self.name},fullname={self.fullname},nickname={self.nickname},addresses={self.addresses}>'


class Address(Base):
    __tablename__ = 'addresses'
    id = Column(Integer, primary_key=True)
    email_address = Column(String, nullable=False)
    user_id = Column(Integer, ForeignKey('users.id'))

    user = relationship("User", back_populates="addresses")

    def __repr__(self):
        return f'<Address id={self.id},email_address={self.email_address}>'


def create_user():
    jack = User(name='jack', fullname='Jack Bean', nickname='gjffdd')
    jack.addresses = [
        Address(email_address='jack@google.com'),
        Address(email_address='j25@yahoo.com')]
    session = get_session()
    session.add(jack)
    session.commit()

    print(session.query(User).one())

def create_schema():
    Base.metadata.create_all(engine)


if __name__ == '__main__':
    create_schema()
    create_user()
