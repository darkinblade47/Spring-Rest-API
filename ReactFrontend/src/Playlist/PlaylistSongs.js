import SongsService from "../Songs/SongsService";
import axios from "axios";
import AlbumSongs from "../Songs/AlbumSongs";
import SuppressedComponent from "../SuppressedComponent";
import ArtistService from "../Artists/ArtistService";


export default class PlaylistSongs extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.getSongByLink = this.getSongByLink.bind(this);
        this.setActiveSong = this.setActiveSong.bind(this);

        this.state = { error:null,
            songs: this.props.songs,
            currentSong: null,
            cachedSongs: {},
            currentIndex: -1,

            statusMessage: ""
        }
    }


    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new ArtistService(nextProps.token)
        }

        if (this.state.currentSong === nextState.currentSong) {
            return false
        }
        return true
    }

    getSongByLink(link, songId, index) {
        let http = axios.create({
            headers: {
                "Content-type": "application/json"
            }
        });

        http.get(link).then((response) => {
            let auxDict = this.state.cachedSongs
            auxDict[songId] = response.data
            this.setState({
                currentSong: response.data,
                currentIndex: index,
                cachedSongs: auxDict
            });
        }).catch((e) => {
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
    }

    setActiveSong(song, index) {
        //if cache is empty, we request the song again
        if (Object.keys(this.state.cachedSongs).length === 0)
            this.getSongByLink(song.link, song._id, index)
        else {
            //if the song is in cache, we show it so we don't do redundant requests
            if (this.state.cachedSongs[song._id] !== undefined) {
                this.setState({
                    currentSong: this.state.cachedSongs[song._id],
                    currentIndex: index
                });
            } else {
                //if the melody is not in cache, we do a GET request and add it in cache
                this.getSongByLink(song.link, song._id, index)
            }
        }


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
                                <AlbumSongs id={currentSong.id} service={new SongsService(null)}/>}
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