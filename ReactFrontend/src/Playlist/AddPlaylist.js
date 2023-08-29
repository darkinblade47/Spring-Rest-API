import Input from "react-validation/build/input";
import CheckButton from "react-validation/build/button";
import Form from "react-validation/build/form";
import SuppressedComponent from "../SuppressedComponent";
import PlaylistService from "./PlaylistService";

const name = value => {
    if (!value) {
        return (
            <div className="alert alert-danger" role="alert">
                This field is required!
            </div>
        );
    }
}

function suppress() {
    const consoleWarn = console.warn;
    const SUPPRESSED_WARNINGS = ['A component is changing',
        'A component contains an input',
        'You have provided'];

    console.warn = function filterWarnings(msg, ...args) {
        if (!SUPPRESSED_WARNINGS.some((entry) => msg.includes(entry))) {
            consoleWarn(msg, ...args);
        }
    };
}


export default class AddPlaylist extends SuppressedComponent {
    constructor(props) {
        super(props);
        suppress();
        this.onEditName = this.onEditName.bind(this);

        this.savePlaylist = this.savePlaylist.bind(this);
        this.errorHandler = this.errorHandler.bind(this);
        this.service = new PlaylistService(this.props.token);

        this.state = { error:null,
            playlistName: "",

            statusMessage: "Give this playlist a nice profile",
            finished: false
        }
    }


    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new PlaylistService(nextProps.token)
        }

        if (nextState.statusMessage !== this.state.statusMessage)
            return true

        if (nextState.finished === true) {
            this.props.hide()
            return false
        }

    }

    errorHandler(e) {
        this.setState({
            statusMessage: e
        })
    }


    onEditName(e) {
        const playlistName = e.target.value;

        this.setState(
            {
                playlistName: playlistName
            }
        );
    }


    savePlaylist(e) {
        e.preventDefault();
        this.form.validateAll();
        if (this.checkBtn.context._errors.length === 0) {

            const data = {
                playlistName: this.state.playlistName,
                idUser: this.props.id
            };
            console.log(this.props.id)
            this.service.create(data, this.props.id)
                .then(response => {
                    console.log(response.data);
                    this.props.hide()
                })
                .catch(e => {
                    if (e.response.data.error_code == "401") {
                        let error = null
                        if (e.response.data.error_code == "403")
                            error = e
                        console.log(e);
                        //relogin
                        this.setState({
                            statusMessage: e.response.data.detail.toString(),
                            error: error
                        });
                    }
                });
        }
    }

    render() {
        const {error} = this.state
        if (error != null) throw error
        return (
            <div>
                <Form
                    onSubmit={this.saveSong}
                    ref={c => {
                        this.form = c;
                    }}
                >
                    <div className="form-group">
                        <label htmlFor="playlistName">Playlist name: </label>
                        <Input
                            type="text"
                            className="form-control"
                            id="playlistName"
                            required
                            name="playlistName"
                            onChange={this.onEditName}
                            validations={[name]}
                        />
                    </div>
                    <button onClick={this.savePlaylist} className="btn btn-success mt-2">
                        Submit
                    </button>

                    <CheckButton
                        style={{display: "none"}}
                        ref={c => {
                            this.checkBtn = c;
                        }}
                    />
                </Form>
                <div>
                    <br/>
                    <p>{this.state.statusMessage}</p>
                </div>
            </div>
        )
    }
}