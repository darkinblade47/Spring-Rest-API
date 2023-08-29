from database.models.role_orm import Role
from database.base.sql_base import Session
from database.repositories.token_repository import *
from database.jwt import token
from database.repositories.user_repository import get_user_info


def get_roles(jwt):
    session = Session()

    try:
        if get_is_valid(jwt):
            is_token_valid = token.verify_token(jwt)
            user_info = get_user_info(is_token_valid['username'])
            if user_info['role'] != is_token_valid['role']:  # verific sa coincida rolurile din token cu db
                invalidate_token(jwt)
                raise Exception("Your roles don't match!")

            if user_info['credentials'][0] != is_token_valid['sub']:  # verific sa coincida id-urile
                invalidate_token(jwt)
                raise Exception("Your user id is invalid!")

            if 'admin' not in user_info['role']:  # daca nu e admin, nu poate sterge user-ul
                invalidate_token(jwt)
                raise Exception("You are not authorized to do that!")

    except Exception as exc:
        raise Exception(exc.args[0])

    roles = session.query(Role).all()
    roles_string = [o.value for o in roles]
    return roles_string
