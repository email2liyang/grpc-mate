import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

db_url = os.getenv('db_url', 'sqlite:///:memory:')
engine = create_engine(db_url, echo=True)
Session = sessionmaker(bind=engine)
