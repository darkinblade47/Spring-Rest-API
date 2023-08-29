import {Component} from "react";


export default class SuppressedComponent extends Component {
    constructor(props) {
        super(props);
        this.suppress = this.suppress.bind(this);

        this.suppress();
    }


    suppress() {
        const consoleWarn = console.warn;
        const SUPPRESSED_WARNINGS = ['MUI:', 'Select',
            'A component is changing',
            'A component contains an input',
            'You have provided'];

        console.warn = function filterWarnings(msg, ...args) {
            if (!SUPPRESSED_WARNINGS.some((entry) => msg.includes(entry))) {
                consoleWarn(msg, ...args);
            }
        };
    }
}