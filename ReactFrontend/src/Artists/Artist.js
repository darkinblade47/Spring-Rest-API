import ArtistService from "./ArtistService";
import ArtistSongs from "../Songs/ArtistSongs";
import SuppressedComponent from "../SuppressedComponent";

export default class Artist extends SuppressedComponent {
    constructor(props) {
        super(props);
        this.getArtistInfo = this.getArtistInfo.bind(this);

        this.service = new ArtistService(this.props.token);
        this.state = { error:null,
            artist: null,
            songs: [],
            currentSong: null,
            currentIndex: null,
            statusMessage: ""
        };
    }

    componentDidMount() {
        this.getArtistInfo(this.props.id);
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.token !== this.props.token) {
            this.service = new ArtistService(nextProps.token)
        }

        return true
    }

    getArtistInfo(id) {
        let artist = null;

        this.service.getById(id).then(response => {
            artist = response.data
            console.log(artist);
            this.setState({
                artist: artist,
                songs: artist.songs

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
                        error: error
                    });
                }
            });
    }

    render() {
        const {artist, songs, error} = this.state;
        if (error != null) throw error;
        return (
            <div>
                {artist != null ? (
                    <div className="col-md-6">
                        <h3>My rich artist profile</h3>
                        <label>Hello {artist.name}, this is your musical masterpiece</label>

                        {songs && <ArtistSongs songs={artist.songs}/>}
                    </div>
                ) : (
                    <div> Your profile is kinda empty</div>
                )}
            </div>
        );
    }
}


