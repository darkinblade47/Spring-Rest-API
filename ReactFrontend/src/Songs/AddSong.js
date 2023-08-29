import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import Input from "react-validation/build/input";
import CheckButton from "react-validation/build/button";
import Form from "react-validation/build/form";
import SuppressedComponent from "../SuppressedComponent";
import SongsService from "./SongsService";

const numberList = value => {
    let splitted = value.split(",");
    for (let i = 0; i < splitted.length; i++) {
        if (isNaN(splitted[i]))
            return (
                <div className="alert alert-danger" role="alert">
                    ID is not a number!
                </div>
            );
    }
}

const validateYear = (value) => {
    if (value < 1860) {
        return (
            <div className="alert alert-danger" role="alert">
                First song ever recorded was in late 1850s, so don't try that again.
            </div>
        );
    } else if (value > 2023) {
        return (
            <div className="alert alert-danger" role="alert">
                Yeah, ok, so you live in the future?
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


export default class AddSong extends SuppressedComponent {

    constructor(props) {
        super(props);
        suppress();
        this.onEditIDs = this.onEditIDs.bind(this);
        this.onEditName = this.onEditName.bind(this);
        this.onEditGenre = this.onEditGenre.bind(this);
        this.onEditType = this.onEditType.bind(this);
        this.onEditYear = this.onEditYear.bind(this);
        this.onEditAlbum = this.onEditAlbum.bind(this);
        this.saveSong = this.saveSong.bind(this);
        this.errorHandler = this.errorHandler.bind(this);
        this.service = new SongsService(this.props.token)

        this.state = { error:null,
            artistIds: [],

            name: "",
            year: 0,
            type: "SONG",
            genre: "ROCK",
            album: null,

            statusMessage: "Give this song a nice profile",
            finished: false
        }
    }


    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new SongsService(nextProps.token)
        }

        if (nextState.statusMessage !== this.state.statusMessage)
            return true

        if (nextState.finished === true) {
            this.props.hide()
            return false
        }
        if (nextState.type !== this.state.type && (nextState.type === "SONG" || this.state.type === "SONG"))
            return true
        else
            return false
    }

    errorHandler(e) {
        this.setState({
            statusMessage: e
        })
    }

    onEditIDs(e) {
        const ids = e.target.value.toString();
        let splitted = ids.split(",");

        this.setState(
            {
                artistIds: splitted
            }
        );
    }

    onEditName(e) {
        const name = e.target.value;

        this.setState(
            {
                name: name
            }
        );
    }

    onEditGenre(e) {
        const genre = e.target.value.toString();

        this.setState(
            {
                genre: genre
            }
        );
    }


    onEditType(e) {
        const type = e.target.value.toString();
        console.log(type)
        this.setState(
            {
                type: type
            }
        );
    }

    onEditYear(e) {
        const year = e.target.value || 1970;

        this.setState(
            {
                year: year
            }
        );
    }

    onEditAlbum(e) {
        const album = e.target.value || null;

        this.setState(
            {
                album: album
            }
        );
    }


    saveSong(e) {
        e.preventDefault();
        this.form.validateAll();
        if (this.checkBtn.context._errors.length === 0) {

            const data = {
                artistIds: this.state.artistIds,
                name: this.state.name,
                year: this.state.year,
                type: this.state.type,
                genre: this.state.genre,
                album: this.state.album,
            };
            console.log(data)
            this.service.create(data)
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
                        <label htmlFor="ids">Artists IDs: </label>
                        <Input
                            type="text"
                            className="form-control"
                            id="ids"
                            required
                            name="ids"
                            onChange={this.onEditIDs}
                            validations={[numberList]}
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="name">Name: </label>
                        <Input
                            defaultValue={"Melodia Mea Fantoma"}
                            type="text"
                            className="form-control"
                            id="name"
                            required
                            name="name"
                            onChange={this.onEditName}
                        />
                    </div>


                    <div className="form-group mt-3">
                        <label htmlFor="simple-select">Genre: </label>
                        <Select defaultValue={"ROCK"}
                                labelId="select-label"
                                id="simple-select"
                                label="Genre"
                                onChange={this.onEditGenre}
                        >
                            <MenuItem value={"ROCK"}>Rock</MenuItem>
                            <MenuItem value={"TRAP"}>Trap</MenuItem>
                            <MenuItem value={"RAP"}>Rap</MenuItem>
                            <MenuItem value={"TECHNO"}>Techno</MenuItem>
                            <MenuItem value={"POP"}>Pop</MenuItem>
                        </Select>
                    </div>

                    <div className="form-group mt-3">
                        <label htmlFor="simple-select">Type: </label>
                        <Select defaultValue={"SONG"}
                                labelId="select-label"
                                id="simple-select"
                                label="Genre"
                                onChange={this.onEditType}
                        >
                            <MenuItem value={"SINGLE"}>Single</MenuItem>
                            <MenuItem value={"SONG"}>Song</MenuItem>
                            <MenuItem value={"ALBUM"}>Album</MenuItem>
                        </Select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="year">Year: </label>
                        <Input
                            defaultValue={1970}
                            type="number"
                            className="form-control"
                            id="year"
                            required
                            validations={[validateYear]}
                            name="year"
                            onChange={this.onEditYear}
                        />
                    </div>
                    {this.state.type === "SONG" &&
                        <div className="form-group">
                            <label htmlFor="album">Album: </label>
                            <Input
                                type="number"
                                className="form-control"
                                id="album"
                                name="album"
                                placeholder="Leave empty for a standalone song"
                                onChange={this.onEditAlbum}
                            />
                        </div>
                    }

                    <button onClick={this.saveSong} className="btn btn-success mt-2">
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