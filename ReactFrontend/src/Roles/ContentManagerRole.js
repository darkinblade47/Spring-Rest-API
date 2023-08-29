import ArtistList from "../Artists/ArtistList";
import {TabContext, TabList, TabPanel} from "@mui/lab";
import {Box, Tab} from "@mui/material";
import SongList from "../Songs/SongList";
import SuppressedComponent from "../SuppressedComponent";

class ContentManagerRole extends SuppressedComponent {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);

        this.state = { error:null,
            currentTab: '1'
        }
    }

    handleChange(e, newValue) {
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
                            <TabList onChange={this.handleChange}>
                                <Tab label="Artists" value="1"/>
                                <Tab label="Songs and albums" value="2"/>
                            </TabList>
                        </Box>
                        <TabPanel value="1"><ArtistList role={"content_manager"} token={this.props.data}
                                                        refresh={this.props.refresh}/></TabPanel>
                        <TabPanel value="2"><SongList role={"content_manager"} token={this.props.data}
                                                      refresh={this.props.refresh}/></TabPanel>
                    </TabContext>
                </Box>
            </div>
        )
    }
}

export default ContentManagerRole;
