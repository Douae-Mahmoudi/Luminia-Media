from jose import jwt, JWTError
from fastapi import HTTPException, Security, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from app.core.config import settings
import base64

bearer_scheme = HTTPBearer()


def verify_token(credentials: HTTPAuthorizationCredentials = Security(bearer_scheme)):
    try:
        secret = base64.b64decode(settings.JWT_SECRET)
        payload = jwt.decode(
            credentials.credentials,
            secret,
            algorithms=["HS384"]
        )
        return payload
    except JWTError as e:
        print(f"JWT Error: {e}")
        raise HTTPException(status_code=401, detail="Token invalide")


def get_current_user(
        credentials: HTTPAuthorizationCredentials = Security(bearer_scheme),
        payload: dict = Depends(verify_token),
) -> dict:

    user_id: str = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=401, detail="Token invalide : champ 'sub' manquant")

    payload["token"] = credentials.credentials
    return payload

