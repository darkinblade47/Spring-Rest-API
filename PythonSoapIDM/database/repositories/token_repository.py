from database.models.token_orm import Token
from database.base.sql_base import Session


def add_token(jwt, valid):
    session = Session()
    token = Token(jwt, valid)
    try:
        session.add(token)
        session.commit()
    except Exception as exc:
        print(f"Failed to add token - {exc}")
    return token


def invalidate_token(jwt):
    session = Session()
    try:
        session.query(Token).filter(Token.jwt == jwt).update({Token.is_valid: False},
                                                             synchronize_session=False)
        session.commit()
        return True
    except Exception as exc:
        print(f"Failed to update user - {exc}")
        return False


def get_is_valid(jwt):
    session = Session()
    try:
        return session.query(Token.is_valid).filter(Token.jwt == jwt).first()
    except Exception as exc:
        print(f"Failed to get token availability - {exc}")
        return "Failed"
