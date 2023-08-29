import axios from "axios";

class SongsService {
    constructor(token) {
        this.token = token;
        this.http = axios.create({
            headers: {
                "Content-type": "application/json"
            }
        });

        this.http_auth = axios.create({
            headers: {
                "Content-type": "application/json",
                "Authorization": "Bearer " + this.token,
            }
        });
    }


    getAllPaged(page, items_per_page) {
        return this.http.get(`http://localhost:8080/api/songcollection/songs?page=${page}&items_per_page=${items_per_page}`);
    }

    getById(id) {
        return this.http.get(`http://localhost:8080/api/songcollection/songs/${id}`);
    }

    getArtistSongs(id) {
        return this.http.get(`http://localhost:8080/api/songcollection/artists/${id}/songs/`);
    }

    create(data) {
        return this.http_auth.post("http://localhost:8080/api/songcollection/songs", data);
    }

    update(id, data) {
        return this.http_auth.put(`http://localhost:8080/api/songcollection/songs/${id}`, data);
    }

}

export default SongsService;