import ArtistService from "./ArtistService";
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import {TextField} from "@mui/material";
import Badge from 'react-bootstrap/Badge';
import AddArtist from './AddArtist';
import InputLabel from '@mui/material/InputLabel';
import Form from "react-validation/build/form";
import SuppressedComponent from "../SuppressedComponent";


class ArtistList extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.getArtists = this.getArtists.bind(this);
        this.noRenderGetArtists = this.noRenderGetArtists.bind(this)

        this.onChangeSearchName = this.onChangeSearchName.bind(this);
        this.onEditName = this.onEditName.bind(this);
        this.onEditActivity = this.onEditActivity.bind(this);

        this.setActiveArtist = this.setActiveArtist.bind(this);
        this.handleAddArtist = this.handleAddArtist.bind(this);
        this.search = this.search.bind(this);
        this.errorHandler = this.errorHandler.bind(this);

        this.edit = this.edit.bind(this);
        this.deleteArtist = this.deleteArtist.bind(this);
        this.updateArtist = this.updateArtist.bind(this);

        this.service = new ArtistService(this.props.token);
        this.searchName = ""

        this.state = { error:null,
            artists: [],
            currentArtist: null,
            currentIndex: -1,
            statusMessage: "Please select an artist",
            editMode: false,
            addArtistMode: false
        };
    }

    componentDidMount() {
        this.getArtists();
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new ArtistService(nextProps.token)
        }

        if (this.state.editMode !== nextState.editMode)
            return true;

        if (this.state.addArtistMode !== nextState.addArtistMode)
            return true

        if (this.state.artists !== nextState.artists)
            return true

        return nextState.editMode !== true;

    }

    errorHandler(e) {
        this.setState({
            statusMessage: e.toString()
        })
    }

    onChangeSearchName(e) {
        this.searchName = e.target.value
    }

    onEditName(e) {
        const name = e.target.value.toString();

        this.setState(prevState => (
            {
                currentArtist:
                    {
                        ...prevState.currentArtist,
                        name: name
                    }

            }))
    }

    onEditActivity(e) {
        const isActive = e.target.value.toString();

        this.setState(function (prevState) {
            return {
                currentArtist:
                    {
                        ...prevState.currentArtist,
                        isActive: isActive
                    }
            }
        })
    }

    handleAddArtist() {
        if (this.state.addArtistMode === true) {
            this.noRenderGetArtists().then((response) => {
                this.setState({
                    statusMessage: "The artist was added successfully!",
                    artists: response,
                    addArtistMode: false,
                    currentArtist: null,
                    currentIndex: -1
                })
            })
                .catch(e => {
                    console.log(e);
                    this.errorHandler(e.response.data.detail)

                })
        } else {
            this.setState({
                addArtistMode: !this.state.addArtistMode
            })

        }
    }

    getArtists() {
        this.service.getAll()
            .then(response => {
                this.setState({
                    artists: response.data["_embedded"]["artists"]
                });
            })
            .catch(e => {
                console.log(e);
                this.errorHandler(e.response.data.detail)
            });
    }

    noRenderGetArtists() {
        return this.service.getAll()
            .then(response => {
                return response.data["_embedded"]["artists"]
            })
            .catch(e => {
                console.log(e.response.data.detail);
                return []
            });
    }

    updateArtist() {
        this.service.update(
            this.state.currentArtist._links.self.href.split("/").pop(),
            this.state.currentArtist
        ).then(() => {
                this.noRenderGetArtists().then((response) => {
                    this.setState({
                        statusMessage: "The artist was updated successfully!",
                        artists: response,
                        editMode: false,
                        currentArtist: null,
                        currentIndex: -1
                    })
                })
                    .catch(e => {
                        console.log(e);
                        this.errorHandler(e.response.data.detail)
                    })
            }
        )
            .catch(e => {
                if (e.response.data.error_code == "401") {
                    console.log(e);
                    //relogin
                    this.errorHandler(e.response.data.detail)
                    this.props.refresh();
                }
            });
    }

    deleteArtist() {
        this.service.delete(this.state.currentArtist._links.self.href.split("/").pop())
            .then((rip_artist) => {
                this.noRenderGetArtists().then((response) => {
                    this.setState({
                        editMode: false,
                        statusMessage: "Artist deleted successfully",
                        artists: response,
                        currentArtist: null,
                        currentIndex: -1
                    })
                })
                    .catch(e => {
                        console.log(e);
                        this.errorHandler(e.response.data.detail)
                    })
            }).catch(e => {
            if (e.response.data.error_code == "401") {
                console.log(e);
                //relogin
                this.errorHandler(e.response.data.detail)
                this.props.refresh();
            }
        })
    }

    setActiveArtist(artist, index) {
        if (this.state.editMode === true)
            return

        artist["uuid"] = artist._links.self.href.split("/").pop()
        this.setState({
            currentArtist: artist,
            currentIndex: index
        });
    }

    search() {
        this.service.findBy(this.searchName, "", null)
            .then(response => {
                this.setState({
                    currentTutorial: null,
                    currentIndex: -1,
                    artists: response.data["_embedded"]["artists"]
                });
            })
            .catch(e => {
                console.log(e);
                this.errorHandler(e.response.data.detail)
            });
    }

    edit() {
        this.setState({
            editMode: !this.state.editMode,
        })
    }


    render() {
        let {artists, currentIndex, currentArtist, error} = this.state;
        if (error != null) throw error;

        return (
            <div className="list row">
                <div>
                </div>
                <div className="col-md-8">
                    <div className="form-group">
                        <label>
                            Name:
                        </label>{" "}
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search by name"
                            onChange={this.onChangeSearchName}
                        />
                    </div>
                    <div className="form-group">
                        <button
                            className="btn btn-outline-secondary"
                            type="button"
                            onClick={this.search}
                        >
                            Search
                        </button>
                    </div>
                </div>
                <div className="col-md-6">
                    <h4>Artist List</h4>

                    <ul className="list-group">
                        {artists &&
                            artists.map((artist, index) => (
                                <li
                                    className={
                                        "list-group-item " +
                                        (index === currentIndex ? "active" : "")
                                    }
                                    onClick={() => this.setActiveArtist(artist, index)}
                                    key={index}
                                >
                                    {artist.name}
                                </li>
                            ))}
                    </ul>
                    {this.props.role === "content_manager" &&
                        <Badge bg="info"
                               onClick={this.handleAddArtist}>{this.state.addArtistMode ? "Forget about that" : "Add artist"}</Badge>
                    }
                    {this.props.role === "content_manager" &&
                        this.state.addArtistMode &&
                        <AddArtist hide={this.handleAddArtist} token={this.props.token}
                                   errorHandler={this.errorHandler} refresh={this.props.refresh}/>
                    }
                </div>

                <div className="col-md-6">
                    {currentArtist ? (
                        !this.state.editMode ? (
                            <div>
                                <h4>Artist</h4>
                                <div>
                                    <label>
                                        <strong>Name:</strong>
                                    </label>{" "}
                                    {currentArtist.name}
                                </div>

                                <div>
                                    <label>
                                        <strong>Activity:</strong>
                                    </label>{" "}
                                    {currentArtist.isActive ? "Active" : "Inactive"}
                                </div>

                                <div>
                                    {this.props.role === "content_manager" &&
                                        <Badge bg="primary"
                                               type="button"
                                               onClick={this.edit}
                                        >
                                            Edit
                                        </Badge>
                                    }
                                </div>
                            </div>
                        ) : (
                            <div>
                                <h4>Artist</h4>
                                <Form>
                                    <div className="form-group">
                                        <TextField defaultValue={this.state.currentArtist.name}
                                                   type="text"
                                                   className="form-control"
                                                   id="name"
                                                   label="Name"
                                                   onChange={this.onEditName}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <InputLabel id="demo-simple-select-label">Activity</InputLabel>
                                        <Select defaultValue={this.state.currentArtist.isActive}
                                                labelId="select-label"
                                                id="simple-select"
                                                label="Activity"
                                                onChange={this.onEditActivity}
                                        >
                                            <MenuItem value={true}>True</MenuItem>
                                            <MenuItem value={false}>False</MenuItem>
                                            <MenuItem value={null}>Doesn't matter</MenuItem>
                                        </Select>
                                    </div>
                                </Form>
                                <Badge bg="danger mr-2"
                                       onClick={this.deleteArtist}
                                >
                                    Delete
                                </Badge>

                                <Badge bg="success mr-2"
                                       type="submit"
                                       onClick={this.updateArtist}
                                >
                                    Update
                                </Badge>

                                <Badge bg="warning"
                                       type="submit"
                                       onClick={this.edit}
                                >
                                    Forget about this
                                </Badge>
                            </div>
                        )) : (
                        <div>
                            <br/>
                            <p>{this.state.statusMessage}</p>
                        </div>
                    )}
                </div>
            </div>
        );
    }
}

export default ArtistList;