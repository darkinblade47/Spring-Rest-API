import Artist from "../Artists/Artist";
import SuppressedComponent from "../SuppressedComponent";
import {Box, Tab} from "@mui/material";
import {TabContext, TabList, TabPanel} from "@mui/lab";
import ArtistList from "../Artists/ArtistList";
import SongList from "../Songs/SongList";

class ArtistRole extends SuppressedComponent {

    constructor(props) {
        super(props);
        this.handleTabChange = this.handleTabChange.bind(this);
        this.state = { error:null,
            currentTab: "0"
        }
    }

    handleTabChange(e, newValue) {
        this.setState({
            currentTab: newValue
        })
    }

    render() {
        return (
            <div>
                <Box sx={{width: '100%', typography: 'body1'}}>
                    <TabContext value={this.state.currentTab}>
                        <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                            <TabList onChange={this.handleTabChange}>
                                <Tab label="My artist profile" value="0"/>
                            </TabList>
                        </Box>
                        <TabPanel value="0"><Artist token={this.props.data} id={this.props.id}
                                                    refresh={this.props.refresh}/></TabPanel>
                    </TabContext>
                </Box>
            </div>
        )
    }
}

export default ArtistRole;
