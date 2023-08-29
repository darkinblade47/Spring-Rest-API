import PlaylistService from "./PlaylistService";
import Badge from "react-bootstrap/Badge";
import PlaylistSongs from "./PlaylistSongs";
import AddPlaylist from "./AddPlaylist";
import SuppressedComponent from "../SuppressedComponent";

export default class Playlist extends SuppressedComponent {

    constructor(props) {
        super(props);
        this.setActivePlaylist = this.setActivePlaylist.bind(this);
        this.getPlaylists = this.getPlaylists.bind(this);
        this.noRenderGetPlaylist = this.noRenderGetPlaylist.bind(this);
        this.addPlaylistModeHandler = this.addPlaylistModeHandler.bind(this);
        this.handleAddPlaylist = this.handleAddPlaylist.bind(this);

        this.service = new PlaylistService(this.props.token)
        this.state = { error:null,
            playlists: [],
            currentPlaylist: null,
            currentIndex: -1,

            statusMessage: "",
            addPlaylistMode: false,

        };
    }

    componentDidMount() {
        try {
            this.getPlaylists()

        } catch (e) {
            console.log(e)
        }
    }


    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new PlaylistService(nextProps.token)
        }

        if (this.state.playlists.length !== nextState.playlists.length)
            return true

        if (this.state.addPlaylistMode !== nextState.addPlaylistMode)
            return true

        if (this.state.currentPlaylist === nextState.currentPlaylist) {
            return false
        }

        return true
    }

    setActivePlaylist(playlist, index) {
        this.setState({
            currentPlaylist: playlist,
            currentIndex: index,
            statusMessage: ""
        });
    }

    addPlaylistModeHandler() {
        this.setState({
            addPlaylistMode: !this.state.addPlaylistMode
        })
    }

    handleAddPlaylist() {
        if (this.state.addPlaylistMode === true) {
            this.noRenderGetPlaylist().then((response) => {
                this.setState({
                    statusMessage: "The song was added successfully!",
                    playlists: response,
                    addPlaylistMode: false,
                    currentPlaylist: null,
                    currentIndex: -1
                })
            })
                .catch(e => {
                    if (e.response.data.error_code == "401") {
                        console.log(e);
                        //relogin
                        this.setState({
                            statusMessage: e.response.data.detail
                        });
                        this.props.refresh();
                    }
                })
        } else {
            this.setState({
                addPlaylistMode: !this.state.addPlaylistMode
            })

        }
    }

    noRenderGetPlaylist() {
        return this.service.getUserPlaylists(this.props.id)
            .then(response => {
                return response.data["_embedded"]["playlistDTOList"]
            })
            .catch(e => {
                throw e
            });
    }

    getPlaylists() {
        //this code snippet is here to force a 403, because I took care of any aspect that would normally cause a 403

        // this.service.getUserPlaylists(9).then(response => {
        //     console.log("Abis, asta cica merge")
        // }).catch(e => {
        //     if (e.response.data.error_code == "403")
        //         this.setState({error: e})
        // })

        this.service.getUserPlaylists(this.props.id)
            .then(response => {
                this.setState({
                    playlists: response.data["_embedded"]["playlistDTOList"],
                    statusMessage: "Select a playlist"
                });
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
                    this.props.refresh();
                }

            });
    }


    render() {
        const {
            playlists,
            currentPlaylist,
            currentIndex,
            error
        } = this.state;
        console.log(error)
        if (error != null) throw error

        return (
            <div className="list row">
                <div className="col-md-6">
                    <h3>Playlist List</h3>
                    <ul className="list-group">
                        {playlists &&
                            playlists.map((playlist, index) => (
                                <li
                                    className={
                                        "list-group-item " +
                                        (index === currentIndex ? "active" : "")
                                    }
                                    onClick={() => this.setActivePlaylist(playlist, index)}
                                    key={index}
                                >
                                    {playlist.playlistName}
                                </li>
                            ))}
                    </ul>
                    <div>
                        {
                            <Badge bg="info"
                                   onClick={this.addPlaylistModeHandler}>
                                {this.state.addPlaylistMode ? "Forget about that" : "Add playlist"}</Badge>
                        }
                        {
                            this.state.addPlaylistMode &&
                            <AddPlaylist hide={this.handleAddPlaylist} token={this.props.token}
                                         id={this.props.id} errorHandler={this.errorHandler}/>
                        }
                    </div>
                </div>
                <div className="col-md-6">
                    {currentPlaylist && (
                        <div>
                            <h4>Playlist details</h4>
                            <div>
                                <label>
                                    <strong>Name:</strong>
                                </label>{" "}
                                {currentPlaylist.playlistName}
                            </div>
                            <div>
                                {currentPlaylist.songs.length !== 0 && (

                                    <label>
                                        <strong>Songs:</strong>
                                    </label>
                                )}
                                {currentPlaylist.songs.length !== 0 && (

                                    <PlaylistSongs songs={currentPlaylist.songs} id={currentPlaylist._id}
                                                   token={this.props.token} refresh={this.props.refresh}/>
                                )
                                }
                            </div>
                        </div>
                    )
                    }
                    <div>
                        <br/>
                        <p>{this.state.statusMessage}</p>
                    </div>
                </div>

            </div>
        )
    }


}