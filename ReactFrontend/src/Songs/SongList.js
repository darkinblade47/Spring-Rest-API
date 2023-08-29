import React from "react";
import SongsService from "./SongsService";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Badge from "react-bootstrap/Badge";
import AddSong from "./AddSong";
import AlbumSongs from "./AlbumSongs";
import SuppressedComponent from "../SuppressedComponent";

export default class SongList extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.service = new SongsService(this.props.token);
        this.getSongs = this.getSongs.bind(this);
        this.setActiveSong = this.setActiveSong.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.onPageSizeChange = this.onPageSizeChange.bind(this);
        this.noRenderGetSongs = this.noRenderGetSongs.bind(this);
        this.handleAddSong = this.handleAddSong.bind(this);
        this.addSongModeHandler = this.addSongModeHandler.bind(this);

        this.stopCalling = false
        this.maxPages = -1
        this.state = { error:null,
            songs: [],
            currentSong: null,
            currentIndex: -1,

            statusMessage: "",
            addSongMode: false,

            currentPage: 0,
            pageSize: 3
        };
    }

    componentDidMount() {
        this.getSongs();
    }

    shouldComponentUpdate(nextProps, nextState, context) {
        if (nextState.songs.length === 0) {
            return false
        }

        if (this.state.addSongMode !== nextState.addSongMode)
            return true
        return true
    }

    //COMPONENT HANDLERS
    onPageSizeChange(e) {
        const items_per_page = e.target.value
        this.noRenderGetSongs(0, items_per_page).then((response) => {
            this.maxPages = -1
            this.stopCalling = false
            this.setState({
                pageSize: items_per_page,
                currentPage: 0,
                currentSong: null,
                currentIndex: -1,
                songs: response
            })
        })
            .catch((e) => {
                if (e.response.data.error_code == 404) {
                    this.setState({
                        statusMessage: e.response.data.detail.toString()
                    })
                }
            })
    }

    handlePageChange(event, op) {
        //pressing NextPage increments currentPage
        //pressing PrevPage decrements currentPage
        const incomingPage = (op === "-") ? this.state.currentPage - 1 : this.state.currentPage + 1
        const items_per_page = this.state.pageSize
        //on the last page, pressing Next doesn't get any redundant requests done
        if (this.stopCalling)
            this.stopCalling = (op === "+")
        //as long as the page is valid, do the requests
        if (incomingPage >= 0 && !this.stopCalling && (this.maxPages >= incomingPage || this.maxPages === -1)) {
            this.noRenderGetSongs(incomingPage, items_per_page).then(
                (response) => {
                    this.setState(
                        {
                            currentPage: incomingPage,
                            currentSong: null,
                            currentIndex: -1,
                            statusMessage: "You are now on page " + incomingPage,
                            songs: response
                        }
                    )
                }
            )
                .catch(e => {
                    console.log(e)
                    this.setState({
                        songs: [],
                        statusMessage: e.response.data.detail.toString(),
                    })
                })
        }

    }


    setActiveSong(song, index) {
        song["id"] = song._links.self.href.split("/").pop()
        this.setState({
            currentSong: song,
            currentIndex: index,
            statusMessage: ""
        });
    }

    addSongModeHandler() {
        this.setState({
            addSongMode: !this.state.addSongMode
        })
    }

    handleAddSong() {
        if (this.state.addSongMode === true) {
            this.noRenderGetSongs(0, this.state.pageSize).then((response) => {
                this.setState({
                    statusMessage: "The song was added successfully!",
                    songs: response,
                    addSongMode: false,
                    currentSong: null,
                    currentIndex: -1
                })
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

                })
        } else {
            this.setState({
                addSongMode: !this.state.addSongMode
            })

        }
    }

    //API CALLS
    noRenderGetSongs(page, items_per_page) {
        return this.service.getAllPaged(page, items_per_page)
            .then(response => {
                return response.data["_embedded"]["songs"]
            })
            .catch(e => {
                this.stopCalling = true
                this.maxPages = page - 1
                throw e
            });
    }

    getSongs() {
        let data = {
            page: parseInt(this.state.currentPage),
            items_per_page: parseInt(this.state.pageSize)
        }
        this.service.getAllPaged(data.page, data.items_per_page)
            .then(response => {
                this.setState({
                    songs: response.data["_embedded"]["songs"],
                    statusMessage: "Select a song"
                });
            })
            .catch(e => {
                console.log(e);
                this.setState({
                    statusMessage: e.response.data.detail.toString()
                });
            });
    }

    render() {
        const {
            songs,
            currentSong,
            currentIndex,
            error
        } = this.state;
        if (error != null) throw error;

        return (
            <div className="list row">
                <div className="col-md-6">
                    <h3>Song List</h3>
                    <h6>Items per page</h6>
                    <Select defaultValue={this.state.pageSize}
                            labelId="select-label"
                            id="simple-select"
                            label="Activity"
                            onChange={this.onPageSizeChange}
                    >
                        <MenuItem value={3}>3</MenuItem>
                        <MenuItem value={5}>5</MenuItem>
                        <MenuItem value={10}>10</MenuItem>
                    </Select>
                    <ul className="list-group">
                        {songs &&
                            songs.map((song, index) => (
                                <li
                                    className={
                                        "list-group-item " +
                                        (index === currentIndex ? "active" : "")
                                    }
                                    onClick={() => this.setActiveSong(song, index)}
                                    key={index}
                                >
                                    {song.name}
                                </li>
                            ))}
                    </ul>

                    <button
                        className="btn btn-outline-secondary mr-3"
                        type="button"
                        onClick={(e) => this.handlePageChange(e, "-")}
                    >
                        Previous Page
                    </button>
                    <button
                        className="btn btn-outline-secondary"
                        type="button"
                        onClick={(e) => this.handlePageChange(e, "+")}
                    >
                        Next Page
                    </button>
                    {<br/>}

                    {this.props.role === "content_manager" &&
                        <Badge bg="info"
                               onClick={this.addSongModeHandler}>
                            {this.state.addSongMode ? "Forget about that" : "Add song"}</Badge>
                    }
                    {
                        this.props.role === "content_manager" &&
                        this.state.addSongMode &&
                        <AddSong hide={this.handleAddSong} token={this.props.token}
                                 refresh={this.props.refresh}/>
                    }
                </div>
                <div className="col-md-6">
                    {currentSong && (
                        <div>
                            <h4>Song</h4>
                            <div>
                                <label>
                                    <strong>Name:</strong>
                                </label>{" "}
                                {currentSong.name}
                            </div>

                            <div>
                                <label>
                                    <strong>Genre:</strong>
                                </label>{" "}
                                {currentSong.genre}
                            </div>
                            <div>
                                <label>
                                    <strong>Type:</strong>
                                </label>{" "}
                                {currentSong.type}
                            </div>
                            <div>
                                <label>
                                    <strong>Release year:</strong>
                                </label>{" "}
                                {currentSong.year}
                            </div>
                            {currentSong.parent != null &&
                                <div>
                                    <label>
                                        <strong>In album:</strong>
                                    </label>{" "}
                                    {currentSong.parent}
                                </div>}
                            {currentSong.type === "ALBUM" &&
                                <AlbumSongs id={currentSong.id} token={this.props.token}/>}
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