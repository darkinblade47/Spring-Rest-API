class AuthService {
    login(username, password) {
        return new Promise((resolve, reject) => {
            const xml =
                '<soap11env:Envelope xmlns:soap11env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sample="services.identitymanager.soap">' +
                '<soap11env:Body>' +
                '<sample:login>' +
                '<sample:username>' + username + '</sample:username>' +
                '<sample:password>' + password + '</sample:password>' +
                '</sample:login>' +
                '</soap11env:Body>' +
                '</soap11env:Envelope>';

            let xmlhttp = new XMLHttpRequest();
            var originalURL = "http://localhost:8000";
            xmlhttp.open('POST', originalURL);

            xmlhttp.onload = resolve;
            xmlhttp.onerror = reject;
            xmlhttp.send(xml);
        });
    }

    relogin(username, password) {
        return this.login(username, password).then(
            function (e) {
                const parser = new DOMParser();
                const doc = parser.parseFromString(e.target.response, "application/xml");
                return doc.getElementsByTagName("tns:loginResult")[0].childNodes[0].nodeValue.toString()
            })

    }

    authorize(jwt) {
        return new Promise((resolve, reject) => {
            const xml =
                '<soap11env:Envelope xmlns:soap11env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sample="services.identitymanager.soap">' +
                '<soap11env:Body>' +
                '<sample:authorize>' +
                '<sample:jwt>' + jwt + '</sample:jwt>' +
                '</sample:authorize>' +
                '</soap11env:Body>' +
                '</soap11env:Envelope>';

            let xmlhttp = new XMLHttpRequest();
            var originalURL = "http://localhost:8000";
            xmlhttp.open('POST', originalURL);

            xmlhttp.onload = resolve;
            xmlhttp.onerror = reject;
            xmlhttp.send(xml);
        });
    }

    logout(jwt) {
        return new Promise((resolve, reject) => {
            const xml =
                '<soap11env:Envelope xmlns:soap11env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sample="services.identitymanager.soap">' +
                '<soap11env:Body>' +
                '<sample:logout>' +
                '<sample:jwt>' + jwt + '</sample:jwt>' +
                '</sample:logout>' +
                '</soap11env:Body>' +
                '</soap11env:Envelope>';

            let xmlhttp = new XMLHttpRequest();
            var originalURL = "http://localhost:8000";
            xmlhttp.open('POST', originalURL);

            xmlhttp.onload = resolve;
            xmlhttp.onerror = reject;
            xmlhttp.send(xml);
        });
    }
}

export default new AuthService();
