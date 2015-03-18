/* global _: false, React: false, ace: false, molgenis: true */
(function(_, React, ace, molgenis) {
	"use strict";
	
	var div = React.DOM.div, textarea = React.DOM.textarea;
	
	ace.config.set("basePath", "/js/ace/src-min-noconflict");
	
	/**
	 * React component for code editor Ace (http://ace.c9.io/)
	 * 
	 * @memberOf component.wrapper
	 */
	var Ace = React.createClass({
		displayName: 'Ace',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		propTypes: {
			name: React.PropTypes.string,
			required: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			height: React.PropTypes.number,
			theme: React.PropTypes.string,
			mode: React.PropTypes.string,
			value: React.PropTypes.string,
			onChange: React.PropTypes.func.isRequired,
		},
		getDefaultProps: function() {
			return {
				height: 250,
				theme: 'eclipse',
				mode: 'r'
			};
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({value: nextProps.value});
		},
		componentDidMount: function() {
			var container = this.refs.editor.getDOMNode();
			var editor = ace.edit(container);
			editor.setTheme('ace/theme/' + this.props.theme);
			
			var session = editor.getSession();
			session.setMode('ace/mode/' + this.props.mode);
			session.setValue(this.state.value);
			
			session.on('change', function() {
				var value = session.getValue();
				this.setState({value: value});
				this.props.onChange(value);
			}.bind(this));
		},
		componentWillUnmount: function() {
			var container = this.refs.editor.getDOMNode();
			var editor = ace.edit(container);
			editor.destroy();
		},
		render: function() {
			if (this.isMounted()) {
				var container = this.refs.editor.getDOMNode();
				var editor = ace.edit(container);	
				editor.setReadOnly(this.props.readOnly === true || this.props.disabled === true);
			}
			
			// editor won't show up unless height is defined
			return div({},
				div({ref: 'editor', style: {height: this.props.height}}),
				textarea({
					className : 'form-control hidden',
					name : this.props.name,
					required : this.props.required,
					disabled: this.props.disabled,
					readOnly: this.props.readOnly,
					value : this.state.value, // FIXME see test-form html-required validation error
					onChange: this._handleChange,
				})
			);
		},
		_handleChange: function(value) {
			this.setState({value: value});
			this.props.onChange(value);
		},
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.wrapper = molgenis.ui.wrapper || {};
	_.extend(molgenis.ui.wrapper, {
		Ace: React.createFactory(Ace)
	});
}(_, React, ace, molgenis));