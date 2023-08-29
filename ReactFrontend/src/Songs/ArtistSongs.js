import AlbumSongs from "./AlbumSongs";
import SuppressedComponent from "../SuppressedComponent";

export default class ArtistSongs extends SuppressedComponent {

    constructor(props) {
        super(props);
        this.setActiveSong = this.setActiveSong.bind(this);
        this.state = { error:null,
            songs: this.props.songs,
            currentSong: null,
            currentIndex: -1

        }

    }

    componentDidMount() {

    }

    setActiveSong(song, index) {
        this.setState({
            currentSong: song,
            currentIndex: index
        });
    }

    render() {
        const {
            songs,
            currentSong,
            currentIndex
        } = this.state;

        return (
            <div className="list row">
                <div className="col-md-6">
                    <h3>Song List</h3>
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
                            <h4>Details</h4>
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

                                {currentSong.type === "ALBUM" &&
                                    <div>
                                        <label>
                                            <strong>Album songs:</strong>
                                        </label>{" "}
                                    </div>
                                }
                                {currentSong.type === "ALBUM" &&
                                    <AlbumSongs id={currentSong.id} token={this.props.token}/>}

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
