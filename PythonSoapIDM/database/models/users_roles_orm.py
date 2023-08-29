from sqlalchemy import Column, Integer, Table, ForeignKey

from database.base.sql_base import Base


# user_roles_relationship = Table(
#     'users_roles', Base.metadata,
#     Column('user_id', Integer, ForeignKey('users.id')),
#     Column('role_id', Integer, ForeignKey('roles.id')),
#     extend_existing=True
# )


class UserRoles(Base):
    __tablename__ = 'users_roles'

    user_id = Column(Integer, primary_key=True)
    role_id = Column(Integer, primary_key=True)

    def __init__(self, user_id, role_id):
        self.user_id = user_id
        self.role_id = role_id
