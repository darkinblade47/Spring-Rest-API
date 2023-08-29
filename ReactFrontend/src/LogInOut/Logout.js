import {ConfirmDialog} from 'primereact/confirmdialog';
import {Button} from 'primereact/button';
import AuthService from "../AuthSoap";
import {Toast} from 'primereact/toast';
import SuppressedComponent from "../SuppressedComponent";


export class Logout extends SuppressedComponent {
    constructor(props) {
        super(props);

        this.state = { error:null,
            visible: false
        };
        this.accept = this.accept.bind(this);
        this.reject = this.reject.bind(this);
    }

    accept() {
        AuthService.logout(this.props.token).then(() => {
            this.props.handler("login", "", "")
        })
    }

    reject() {
        this.toast.current.show({
            severity: 'warn',
            summary: 'Wdym?',
            detail: 'You have rejected me...rude',
            life: 3000
        });
    }


    render() {
        return (
            <div>
                <Toast ref={(el) => this.toast = el}/>

                <div className="card">
                    <ConfirmDialog visible={this.state.visible} onHide={() => this.setState({visible: false})}
                                   message="Are you sure you want to logout?"
                                   header="Confirmation" icon="pi pi-exclamation-triangle" accept={this.accept}
                                   reject={this.reject}/>
                    <Button onClick={() => this.setState({visible: true})} icon="pi pi-check" label="Logout"/>
                </div>
            </div>
        )
    }
}