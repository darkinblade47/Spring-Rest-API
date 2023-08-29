from sqlalchemy import Column, String, Boolean
from database.base.sql_base import Base


class Token(Base):
    __tablename__ = 'tokens'

    jwt = Column(String, primary_key=True)
    is_valid = Column(Boolean)

    def __init__(self, jwt, is_valid):
        self.jwt = jwt
        self.is_valid = is_valid
