import {Component} from "react";
import no from './no.jpg';

export default class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = {error: null, errorInfo: null};
    }

    componentDidCatch(error, errorInfo) {
        if (error.response.data.error_code == "403")
            this.setState({
                error: error,
                errorInfo: "403 Forbidden"
            })
    }

    render() {
        if (this.state.errorInfo) {
            return (
                <div>
                    <h1>403</h1>
                    <h2>Forbidden</h2>
                    <img src={no} alt="YOU SHALL NOT PASS"/>
                </div>
            );
        }
        return this.props.children;
    }
}