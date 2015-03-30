/* global _: false, React: false, tinymce: false, molgenis: true */
(function(_, React, tinymce, molgenis) {
	"use strict";
	
	var textarea = React.DOM.textarea;
	
	/**
	 * React component for HTML WYSIWYG editor (http://www.tinymce.com/)
	 * 
	 * @memberOf component.wrapper
	 */
	var TinyMce = React.createClass({ // FIXME continue with component
		displayName: 'TinyMce',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		propTypes: {
			name: React.PropTypes.string,
			required: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.string,
			onChange: React.PropTypes.func.isRequired,
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			if(value !== nextProps.value) {
				this.setState({value: nextProps.value});
			}
		},
		componentDidMount: function() {
			tinymce.init({
			    selector: "textarea#elm1",
			    theme: "modern",
			    plugins: [
			        "advlist autolink lists link charmap print preview anchor",
			        "searchreplace visualblocks code fullscreen",
			        "insertdatetime table contextmenu paste"
			    ],
			    convert_urls: false,
			    toolbar: "insertfile undo redo | styleselect fontselect fontsizeselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link",
			    setup : function(ed) {
		            ed.on('change', function(e) {
		            	$('#submitBtn').prop('disabled', false);
		            });
	            }
			});
		},
		componentWillUnmount: function() {
			tinymce.get('textarea_e1m1').remove();
		},
		render: function() {
			return textarea({
				id: 'elm1', // FIXME assign random id when creating component
				className : 'form-control hidden',
				name : this.props.name,
				required : this.props.required,
				disabled: this.props.disabled,
				readOnly: this.props.readOnly,
				value : this.state.value, // FIXME see test-form html-required validation error
				onChange: this._handleChange,
			});
		},
		componentDidUpdate: function() {
			if (this.isMounted()) {
				this._updateAce();	
			}
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
		TinyMce: React.createFactory(TinyMce)
	});
}(_, React, tinymce, molgenis));