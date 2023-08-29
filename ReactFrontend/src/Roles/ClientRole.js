import SongList from "../Songs/SongList";
import {TabContext, TabList, TabPanel} from "@mui/lab";
import {Box, Tab} from "@mui/material";
import Playlist from "../Playlist/Playlist";
import SuppressedComponent from "../SuppressedComponent";

class ClientRole extends SuppressedComponent {

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);

        this.state = { error:null,
            currentTab: "1"
        }
    }

    componentDidCatch(error, errorInfo) {
        throw error
    }

    handleChange(e, newValue) {
        console.log(e)
        this.setState({
            currentTab: newValue
        })
    }

    render() {

        return (
            <div>
                <TabContext value={this.state.currentTab}>
                    <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                        <TabList onChange={this.handleChange}>
                            <Tab label="Songs and albums" value="1"/>
                            <Tab label="Playlists" value="2"/>
                        </TabList>
                    </Box>
                    <TabPanel value="1"><SongList token={this.props.data} refresh={this.props.refresh}/></TabPanel>
                    <TabPanel value="2"><Playlist token={this.props.data} id={this.props.id}
                                                  refresh={this.props.refresh}/> </TabPanel>
                </TabContext>
            </div>
        )
    }
}

export default ClientRole;
