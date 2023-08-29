# python -m pip install lxml spyne
from spyne import Application, rpc, ServiceBase, String, AnyDict
from spyne.model.complex import Array
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication
from database.repositories.user_repository import *
from database.repositories.role_repository import *


class UserManagerService(ServiceBase):
    @rpc(String, String, String, String, _returns=String)
    def create_user(ctx, username, password, roles, jwt):
        try:
            new_user = create_user(username, password, roles, jwt)
            if new_user:
                return "User added sucessfully"
        except Exception as exc:
            return exc

    @rpc(String, String, String, _returns=String)
    def update_user_password(ctx, username, password, jwt):
        try:
            updated_user = update_user_password(username, password, jwt)
            if updated_user:
                return "User updated sucessfully"
        except Exception as exc:
            return exc

    @rpc(String, String, _returns=String)
    def delete_user(ctx, username, jwt):
        try:
            user_deleted = delete_user(username, jwt)
            if user_deleted:
                return "User deleted sucessfully"
        except Exception as exc:
            return exc

    @rpc(String, String, _returns=AnyDict)
    def get_user_info(ctx, username, jwt):
        info = get_user_info_secure(username, jwt)
        if info:
            return info
        return {"Error": "No info available"}

    @rpc(String,_returns=Array(String))
    def get_roles(ctx,jwt):
        info = get_roles(jwt)
        if info:
            return info
        return {"Error": "No info available"}

    @rpc(String, _returns=Array(AnyDict))
    def get_all(ctx, jwt):
        info = get_users(jwt)
        if info:
            return info
        return {"Error": "No info available"}

    @rpc(String, String, _returns=String)
    def login(ctx, username, password):
        print("am intrat")
        try:
            login_status = login(username, password)
            return login_status
        except Exception as exc:
            return exc

    @rpc(String, _returns=String)
    def authorize(ctx, jwt):
        try:
            return authorize(jwt)
        except Exception as exc:
            return exc

    @rpc(String, _returns=String)
    def logout(ctx, jwt):
        try:
            return logout(jwt)
        except Exception:
            return "Failed"


application = Application([UserManagerService], 'services.identitymanager.soap',
                          in_protocol=Soap11(validator='lxml'),
                          out_protocol=Soap11())

wsgi_application = WsgiApplication(application)

if __name__ == '__main__':
    import logging
    from wsgiref.simple_server import make_server

    logging.basicConfig(level=logging.INFO)
    logging.getLogger('spyne.protocol.xml').setLevel(logging.INFO)

    logging.info("listening to http://127.0.0.1:8000")
    logging.info("wsdl is at: http://127.0.0.1:8000/?wsdl")

    server = make_server('127.0.0.1', 8000, wsgi_application)
    server.serve_forever()  
