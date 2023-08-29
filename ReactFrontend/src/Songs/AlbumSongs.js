import SongsService from "./SongsService";
import SuppressedComponent from "../SuppressedComponent";

export default class AlbumSongs extends SuppressedComponent {
    constructor(props) {
        super(props)
        this.service = new SongsService(null)
        this.getAlbumSongs = this.getAlbumSongs.bind(this)

        this.state = { error:null,
            songs: [],
            currentSong: null,
            currentIndex: -1
        }
    }


    componentDidMount() {
        this.getAlbumSongs(this.props.id)
    }

    getAlbumSongs(id) {
        this.service.getById(id)
            .then(response => {
                this.setState({
                    songs: response.data.albumSongs
                });
            })
            .catch(e => {
                console.log(e);
            });
    }

    render() {
        const {
            songs,
            currentIndex
        } = this.state;
        return (
            <div className="list row">
                <ul className="list-group">
                    {songs &&
                        songs.map((song, index) => (
                            <li
                                className={
                                    "list-group-item " +
                                    (index === currentIndex ? "active" : "")
                                }
                                key={index}
                            >
                                {song.name}
                            </li>
                        ))}

                </ul>

            </div>

        )
    }
}