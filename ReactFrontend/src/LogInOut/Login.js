import React from "react";
import Form from "react-validation/build/form";
import Input from "react-validation/build/input";
import CheckButton from "react-validation/build/button";
import AuthService from "../AuthSoap";
import SuppressedComponent from "../SuppressedComponent";

const required = value => {
    if (!value) {
        return (
            <div className="alert alert-danger" role="alert">
                This field is required!
            </div>
        );
    }
};

class Login extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.handleLogin = this.handleLogin.bind(this);
        this.onChangeUsername = this.onChangeUsername.bind(this);
        this.onChangePassword = this.onChangePassword.bind(this);
        this.roles = ["admin", "content_manager", "client", "artist"]

        this.state = { error:null,
            role: "",
            token: "",
            id: ""
        };
    }

    onChangeUsername(event) {
        this.username = event.target.value
    }

    onChangePassword(e) {
        this.password = e.target.value
    }

    handleLogin(e) {
        e.preventDefault();
        this.form.validateAll();
        if (this.checkBtn.context._errors.length === 0) {
            let token, role, id;
            AuthService.login(this.username, this.password).then(
                function (e) {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(e.target.response, "application/xml");
                    token = doc.getElementsByTagName("tns:loginResult")[0].childNodes[0].nodeValue.toString()

                }).then(() => {
                if (token !== "Error") {

                    AuthService.authorize(token).then(
                        function (e) {
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(e.target.response, "application/xml");
                            let res = doc.getElementsByTagName("tns:authorizeResult")[0].childNodes[0].nodeValue.toString().split("|||")[1];
                            console.log(doc.getElementsByTagName("tns:authorizeResult")[0].childNodes[0].nodeValue.toString())
                            id = doc.getElementsByTagName("tns:authorizeResult")[0].childNodes[0].nodeValue.toString().split("|||")[0];
                            role = res.substring(2, res.length - 2);

                        }
                    ).then(() => {
                        // if (this.roles.includes(role)) {
                        //     console.log(role,token,id);
                        let userRoles = role.split("', '")
                        console.log(userRoles)
                        this.props.handler(userRoles, token, id, this.username, this.password)
                        // }
                    });
                }
            });
        }
    }

    render() {
        return (

            <div className="card card-container">
                <Form
                    onSubmit={this.handleLogin}
                    ref={c => {
                        this.form = c;
                    }}
                >
                    <div className="form-group">
                        <label htmlFor="username">Username</label>
                        <Input
                            type="text"
                            name="username"
                            value={this.username}
                            onChange={this.onChangeUsername}
                            validations={[required]}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <Input
                            type="password"
                            name="password"
                            value={this.password}
                            onChange={this.onChangePassword}
                            validations={[required]}
                        />
                    </div>

                    <div className="form-group">
                        <button
                            className="btn btn-primary btn-block"
                        >
                            <span>Login</span>
                        </button>
                    </div>

                    <CheckButton
                        style={{display: "none"}}
                        ref={c => {
                            this.checkBtn = c;
                        }}
                    />
                </Form>
            </div>
        )
    }

}

export default Login;

