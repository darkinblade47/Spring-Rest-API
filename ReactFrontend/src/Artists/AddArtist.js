import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import SuppressedComponent from "../SuppressedComponent";
import ArtistService from "./ArtistService";


export default class AddArtist extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.onEditName = this.onEditName.bind(this);
        this.onEditActivity = this.onEditActivity.bind(this);
        this.onEditUuid = this.onEditUuid.bind(this);
        this.saveArtist = this.saveArtist.bind(this);
        this.service = new ArtistService(this.props.token);

        this.state = { error:null,
            uuid: "",
            name: "",
            isActive: null,
            finished: false
        }
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new ArtistService(nextProps.token)
        }

        if (nextState.finished === true) {
            this.props.hide()
            return false
        }
        return false
    }

    onEditName(e) {
        const name = e.target.value;

        this.setState(
            {
                name: name
            }
        );
    }

    onEditUuid(e) {
        const uuid = e.target.value;

        this.setState(
            {
                uuid: uuid
            }
        );
    }


    onEditActivity(e) {
        const isActive = e.target.value.toString();

        this.setState(
            {
                isActive: isActive
            }
        );
    }


    saveArtist() {
        const data = {
            uuid: this.state.uuid,
            name: this.state.name,
            isActive: this.state.isActive
        };

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
                        error: error
                    });
                }
            });
    }

    render() {
        const {error} = this.state
        if (error != null)
            throw error

        return (
            <div>
                {console.log("una randare per AddArtist")}
                <div className="form-group">
                    <label htmlFor="uuid">UUID</label>
                    <input
                        type="text"
                        className="form-control"
                        id="uuid"
                        required
                        onChange={this.onEditUuid}
                        name="uuid"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input
                        type="text"
                        className="form-control"
                        id="name"
                        required
                        onChange={this.onEditName}
                        name="name"
                    />
                </div>


                <div className="form-group">
                    <label htmlFor="simple-select">Activity: </label>
                    <Select defaultValue={this.state.isActive}
                            labelId="select-label"
                            id="simple-select"
                            label="Activity"
                            onChange={this.onEditActivity}
                    >
                        <MenuItem value={true}>True</MenuItem>
                        <MenuItem value={false}>False</MenuItem>
                        <MenuItem value={"null"}>-</MenuItem>
                    </Select>
                </div>

                <button onClick={this.saveArtist} className="btn btn-success">
                    Submit
                </button>
            </div>
        )
    }
}