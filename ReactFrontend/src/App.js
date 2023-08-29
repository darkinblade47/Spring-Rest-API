import './App.css';
import Login from "./LogInOut/Login";
import {Component} from "react";
import ClientRole from "./Roles/ClientRole";
import {Logout} from "./LogInOut/Logout";
import ArtistRole from "./Roles/ArtistRole";
import ContentManagerRole from "./Roles/ContentManagerRole";
import {TabContext, TabList, TabPanel} from "@mui/lab";
import {Box, Tab} from "@mui/material";
import SongList from "./Songs/SongList";
import ArtistList from "./Artists/ArtistList";
import AuthService from "./AuthSoap";
import ErrorBoundary from "./ErrorBoundary";

class App extends Component {
    constructor(props) {
        super(props);
        this.handler = this.handler.bind(this);
        this.handleTabChange = this.handleTabChange.bind(this);
        this.relogin = this.relogin.bind(this);

        this.state = { error:null,
            //currentTab: "login",
            userRoles: [],
            currentTab: "0",
            jwt: "",
            id: "",

            username: "",
            password: ""
        };
    }

    handler(tab, token, id, username, password) {
        this.setState({
            userRoles: tab,
            jwt: token,
            id: id,
            username: username,
            password: password
        })
    }


    handleTabChange(e, newValue) {
        this.setState({
            currentTab: newValue
        })
    }

    relogin() {
        AuthService.relogin(this.state.username, this.state.password).then((response) =>
            this.setState({
                jwt: response
            })
        )
        this.forceUpdate();

    }

    render() {
        const {jwt} = this.state;
        return (
            <div>
                <nav className="navbar navbar-expand navbar-dark bg-dark">
                    <div className="navbar-brand">
                        sPOSify
                    </div>
                    {
                        this.state.currentTab !== "0" &&
                        <div className="navbar-nav ml-auto">
                            <li className="nav-item">
                                <Logout handler={this.handler} token={this.state.jwt}/>
                            </li>
                        </div>
                    }
                </nav>
                <ErrorBoundary>
                    <div>
                        <TabContext value={this.state.currentTab}>
                            <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                                <TabList onChange={this.handleTabChange}>
                                    {this.state.jwt === "" && <Tab label="Login" value="0"/>}
                                    {!(this.state.userRoles.includes("content_manager") || this.state.userRoles.includes("client")) &&
                                        <Tab label="Songs and albums for everyone" value="1"/>}
                                    {!(this.state.userRoles.includes("content_manager") || this.state.userRoles.includes("client")) &&
                                        <Tab label="Artists for everyone" value="2"/>}
                                    {this.state.userRoles.includes("content_manager") &&
                                        <Tab label="Content Manager" value="3"/>}
                                    {this.state.userRoles.includes("artist") && <Tab label="Artist" value="4"/>}
                                    {this.state.userRoles.includes("client") && <Tab label="Client" value="5"/>}
                                </TabList>
                            </Box>
                            {this.state.jwt === "" && <TabPanel value="0"><Login handler={this.handler}/></TabPanel>}
                            {!(this.state.userRoles.includes("content_manager") || this.state.userRoles.includes("client")) &&
                                <TabPanel value="1"><SongList token={null}/></TabPanel>}
                            {!(this.state.userRoles.includes("content_manager") || this.state.userRoles.includes("client")) &&
                                <TabPanel value="2"><ArtistList token={null}/></TabPanel>}
                            {this.state.userRoles.includes("content_manager") &&
                                <TabPanel value="3"><ContentManagerRole data={jwt}
                                                                        refresh={this.relogin}/></TabPanel>}
                            {this.state.userRoles.includes("artist") &&
                                <TabPanel value="4"><ArtistRole data={jwt} id={this.state.id}
                                                                refresh={this.relogin}/></TabPanel>}
                            {this.state.userRoles.includes("client") &&
                                <TabPanel value="5"><ClientRole data={jwt} id={this.state.id}
                                                                refresh={this.relogin}/></TabPanel>}
                        </TabContext>
                    </div>
                </ErrorBoundary>

            </div>
        );
    }
}

export default App;
