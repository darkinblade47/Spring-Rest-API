# import libraries
import uuid

# from jwcrypto import jwt, jwk
import jwt
from jose.exceptions import JWTClaimsError, JWSSignatureError
from jwt import ExpiredSignatureError
from pydantic import BaseModel
from datetime import datetime, timedelta

SECRET_KEY = "80ee0698e94a48445507fbcd0a287166ecbf7bddc633f1fe8c3a225d1ac23789"
ISS = "http://localhost:8000"
ALGORITHM = "HS256"


def create_access_token(data: dict):
    to_encode = data.copy()

    expire = datetime.utcnow() + timedelta(hours=1)
    to_encode.update({"exp": expire})
    to_encode.update({"jti": str(uuid.uuid4())})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

    return encoded_jwt


def get_token(username, role, sub):
    playload = {
        'username': username,
        'role': role,
        'iss': ISS,
        'sub': sub
    }

    token = create_access_token(data=playload)
    return {'token': token}


def verify_token(token: str):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTClaimsError as error:
        raise Exception(str(error))
    except ExpiredSignatureError:
        raise Exception("Signature has expired")
    except JWSSignatureError:
        raise Exception("Invalid signature")
