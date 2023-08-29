import axios from "axios";

class ArtistService {
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

    getAll() {
        return this.http.get("http://localhost:8080/api/songcollection/artists");
    }

    getById(id) {
        return this.http.get(`http://localhost:8080/api/songcollection/artists/${id}`);
    }

    getSongsById(id) {
        return this.http.get(`http://localhost:8080/api/songcollection/artists/${id}/songs`);
    }

    create(data) {
        return this.http_auth.post("http://localhost:8080/api/songcollection/artists", data);
    }

    update(id, data) {
        return this.http_auth.put(`http://localhost:8080/api/songcollection/artists/${id}`, data);
    }

    delete(id) {
        return this.http_auth.delete(`http://localhost:8080/api/songcollection/artists/${id}`);
    }

    findBy(name, match, isActive) {
        return this.http.get(`http://localhost:8080/api/songcollection/artists?name=${name}&match=${match}&isActive=${isActive}`);
    }
}

export default ArtistService;