import axios from "axios";

class PlaylistService {
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
        return this.http.get(`http://localhost:8082/api/songcollection/songs?page=${page}&items_per_page=${items_per_page}`);
    }

    getUserPlaylists(id) {
        return this.http_auth.get(`http://localhost:8082/api/users/${id}/profile/playlists`);
    }

    getPlaylistById(idUser, idPlaylist) {
        return this.http_auth.get(`http://localhost:8082/api/users/${idUser}/profile/playlists/${idPlaylist}`);
    }

    create(data, idUser) {
        return this.http_auth.post(`http://localhost:8082/api/users/${idUser}/profile/playlists`, data);
    }

    update(id, data) {
        return this.http_auth.put(`http://localhost:8082/api/songcollection/songs/${id}`, data);
    }

}

export default PlaylistService;