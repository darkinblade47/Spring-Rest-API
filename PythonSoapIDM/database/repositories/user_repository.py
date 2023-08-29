from database.models.user_orm import User
from database.models.role_orm import Role
from database.models.users_roles_orm import UserRoles
from database.jwt import token
from database.repositories.token_repository import *


def get_users(jwt):
    try:
        if get_is_valid(jwt):  # valid in db
            is_token_valid = token.verify_token(jwt)  # validarea semnaturii
            user_info = get_user_info(is_token_valid['username'])
            if user_info['role'] != is_token_valid['role']:  # verific sa coincida rolurile din token cu db
                invalidate_token(jwt)
                raise Exception("Your roles don't match!")

            if user_info['credentials'][0] != is_token_valid['sub']:  # verific sa coincida id-urile
                invalidate_token(jwt)
                raise Exception("Your user id is invalid!")

            if 'admin' not in user_info['role']:  # daca nu e admin, nu poate face listarea
                invalidate_token(jwt)
                raise Exception("You are not authorized to do that!")
    except Exception as exc:
        raise Exception(exc.args[0])

    session = Session()
    users = session.query(User).all()
    users_info = list(map(lambda user: get_user_info(user.username), users))

    return users_info


def create_user(username, password, roles, jwt):
    session = Session()
    user = User(username, password)

    try:
        if get_is_valid(jwt):  # valid in db
            is_token_valid = token.verify_token(jwt)  # validarea semnaturii
            user_info = get_user_info(is_token_valid['username'])
            if user_info['role'] != is_token_valid['role']:  # verific sa coincida rolurile din token cu db
                invalidate_token(jwt)
                raise Exception("Your roles don't match!")

            if user_info['credentials'][0] != is_token_valid['sub']:  # verific sa coincida id-urile
                invalidate_token(jwt)
                raise Exception("Your user id is invalid!")

            if 'admin' not in user_info['role']:  # daca nu e admin, nu poate crea user-ul
                invalidate_token(jwt)
                raise Exception("You are not authorized to do that!")

            if get_user_info(username) is not None:
                raise Exception("Username already exists!")
    except Exception as exc:
        raise Exception(exc.args[0])

    try:
        session.add(user)
    except Exception as exc:
        raise Exception(f"Failed to add user - {exc}")

    for role in roles.split(':'):
        db_role = session.query(Role).filter(Role.value == role).first()
        if db_role:
            user_role = UserRoles(user.id, db_role.id)
            try:
                session.add(user_role)
                session.commit()
            except Exception as exc:
                raise Exception(f"Failed to add roles to user - {exc}")
        else:
            raise Exception(f"Role {role} doesn't exist")
    return user.id


def update_user_password(username, password, jwt):
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

            if 'admin' not in user_info['role']:  # daca nu e admin, verificam sa fie contul utilizatorului cu cererea
                if username != is_token_valid['username']:
                    invalidate_token(jwt)
                    raise Exception("You are not the account's owner!")
    except Exception as exc:
        raise Exception(exc.args[0])

    updated_user = None
    try:
        session.query(User).filter(User.username == username).update({User.password: password},
                                                                     synchronize_session=False)
        session.commit()
        updated_user = session.query(User).filter(User.username == username, User.password == password).first()
    except Exception as exc:
        print(f"Failed to update user - {exc}")
    return updated_user


def delete_user(username, jwt):
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

    deleted_user = None
    try:
        user = session.query(User).filter(User.username == username).first()
        session.delete(user)
        session.commit()
        deleted_user = session.query(User).filter(User.username == username).first()
        if deleted_user is None:
            return "Deleted"
    except Exception as exc:
        print(f"Failed to delete user - {exc}")
    return deleted_user


def get_user_info(username):
    session = Session()
    infos = {}
    try:
        roles = session.query(
            Role
        ).filter(
            User.id == UserRoles.user_id,
        ).filter(
            UserRoles.role_id == Role.id
        ).filter(
            User.username == username
        ).all()

        infos["role"] = list(map(lambda role: role.value, roles))
        creds = session.query(User).filter(User.username == username).first()
        if creds:
            infos["credentials"] = list()
            infos["credentials"].append(creds.id)
            infos["credentials"].append(creds.username)
            infos["credentials"].append(creds.password)

    except Exception as exc:
        print(f"Failed to get info about the user - {exc}")
    return infos


def get_user_info_secure(username, jwt):
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

    infos = {}
    try:
        roles = session.query(
            Role
        ).filter(
            User.id == UserRoles.user_id,
        ).filter(
            UserRoles.role_id == Role.id
        ).filter(
            User.username == username
        ).all()

        infos["role"] = list(map(lambda role: role.value, roles))
        creds = session.query(User).filter(User.username == username).first()
        if creds:
            infos["credentials"] = list()
            infos["credentials"].append(creds.id)
            infos["credentials"].append(creds.username)
            infos["credentials"].append(creds.password)

    except Exception as exc:
        print(f"Failed to get info about the user - {exc}")
    return infos


def login(username, password):
    session = Session()
    infos = {}
    try:
        user = session.query(User).filter(User.username == username).first()
        if user.username == username and user.password == password:
            infos = get_user_info(username)
            jwt = token.get_token(username, infos['role'], infos['credentials'][0])
            add_token(jwt['token'], True)
            return jwt['token']
        else:
            return False
    except Exception as exc:
        print(f"Failed to get info about the user - {exc}")
    return infos


def authorize(jwt):
    print(jwt)
    try:
        payload = token.verify_token(jwt)  # are grija de validari ca jmecheru'
        user_info = get_user_info(payload['username'])
        if user_info['role'] != payload['role']:
            invalidate_token(jwt)
            return "Error:Roles don't match"
        return f"{payload['sub']}|||{payload['role']}"
    except Exception as ex:
        invalidate_token(jwt)
        return "Error:"+ex.args[0]


def logout(jwt):
    try:
        invalidate_token(jwt)
        return "Success"
    except Exception:
        return "Failed"
