from sqlalchemy import Column, String, Integer
from database.base.sql_base import Base
from sqlalchemy.orm import relationship
# from database.models.users_roles_orm import user_roles_relationship
from database.models.role_orm import Role


class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    username = Column(String)
    password = Column(String)
    # roles = relationship("Role", secondary=user_roles_relationship)

    def __init__(self, username, password):
        self.username = username
        self.password = password
