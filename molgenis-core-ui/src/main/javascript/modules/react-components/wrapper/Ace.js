import React from "react";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";
try {
    var ace = require('brace'); // fails server-side
} catch (exception) {
    ace = React.DOM.textarea
}

var div = React.DOM.div, textarea = React.DOM.textarea;

/**
 * React component for code editor Ace (http://ace.c9.io/)
 *
 * @memberOf component.wrapper
 */
var Ace = React.createClass({
    displayName: 'Ace',
    mixins: [DeepPureRenderMixin],
    propTypes: {
        name: React.PropTypes.string,
        required: React.PropTypes.bool,
        readOnly: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        maxLength: React.PropTypes.number,
        height: React.PropTypes.number,
        theme: React.PropTypes.string,
        mode: React.PropTypes.oneOf(['ftl', 'html', 'javascript', 'json',
            'markdown', 'markdown', 'mysql', 'python', 'r', 'plain_text',
            'properties', 'text', 'xml']),
        value: React.PropTypes.string,
        onChange: React.PropTypes.func.isRequired,
    },
    getDefaultProps: function () {
        return {
            height: 250,
            theme: 'eclipse',
            mode: 'r'
        };
    },
    getInitialState: function () {
        return {value: this.props.value};
    },
    componentWillReceiveProps: function (nextProps) {
        this.setState({value: nextProps.value});
    },
    componentDidMount: function () {
        var container = this.refs.editor.getDOMNode();
        var editor = ace.edit(container);
        editor.setTheme('ace/theme/' + this.props.theme);

        var session = editor.getSession();
        session.setMode('ace/mode/' + this.props.mode);
        if (this.props.tail) {
            session.setValue(this.state.value, 1);
            editor.scrollToRow(session.getLength() - 1)
        } else {
            session.setValue(this.state.value, -1);
        }

        session.on('change', function () {
            var value = session.getValue();
            this.setState({value: value});
            if (this.props.onChange) {
                this.props.onChange(value);
            }
        }.bind(this));

        this._updateAce();
    },
    componentWillUnmount: function () {
        var container = this.refs.editor.getDOMNode();
        var editor = ace.edit(container);
        editor.destroy();
    },
    render: function () {
        // editor won't show up unless height is defined
        return div({},
            div({ref: 'editor', style: {height: this.props.height}}),
            textarea({
                className: 'form-control hidden',
                name: this.props.name,
                required: this.props.required,
                disabled: this.props.disabled,
                readOnly: this.props.readOnly,
                maxLength: this.props.maxLength,
                value: this.state.value,
                onChange: this._handleChange,
            })
        );
    },
    componentDidUpdate: function () {
        if (this.isMounted()) {
            this._updateAce();
        }
    },
    _updateAce: function () {
        var container = this.refs.editor.getDOMNode();
        var editor = ace.edit(container);
        var session = editor.getSession();
        editor.setReadOnly(this.props.readOnly === true || this.props.disabled === true);
        if (editor.getValue() !== this.state.value) {
            // I THINK this always means the value got updated programmatically so we can safely update the editor's value
            if (this.props.tail) {
                session.setValue(this.state.value, 1);
                editor.scrollToRow(session.getLength() - 1)
            } else {
                session.setValue(this.state.value, -1);
            }
        }
    },
    _handleChange: function (value) {
        // apply constraint: maximum number of characters allowed in input
        if (this.props.maxLength) {
            value = value.substr(0, this.props.maxLength);
        }
        this.setState({value: value});
        this.props.onChange(value);
    }
});

export {Ace}
export default React.createFactory(Ace);