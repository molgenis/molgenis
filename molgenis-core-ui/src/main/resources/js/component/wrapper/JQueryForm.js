/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";
    
    var form = React.DOM.form;
			
    /**
	 * React component for jQuery Form Plugin (http://malsup.com/jquery/form/)
	 * 
	 * @memberOf component.wrapper
	 */
	var JQueryForm = React.createClass({
		displayName: 'JQueryForm',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		propTypes: {
			className: React.PropTypes.string,
			action: React.PropTypes.string.isRequired,
			method: React.PropTypes.string,
			noValidate: React.PropTypes.bool,
			beforeSubmit: React.PropTypes.func,
			success: React.PropTypes.func,
			error: React.PropTypes.func,
		},
		componentDidMount: function() {
			var $form = $(this.refs.form.getDOMNode());
			$form.ajaxForm({
				resetForm: false,
				beforeSubmit : this.props.beforeSubmit,
				success : this.props.success,
				error : this.props.error
			});
		},
		componentWillUnmount: function() {
			var $form = $(this.refs.form.getDOMNode());
			$form.ajaxFormUnbind();
		},
		render: function() {
			return (
				form(_.extend({}, this.props, {ref: 'form'}),
					this.props.children
				)
			);
		}
	});
	
    // export component
    molgenis.ui = molgenis.ui || {};
    molgenis.ui.wrapper = molgenis.ui.wrapper || {};
    _.extend(molgenis.ui.wrapper, {
        JQueryForm: React.createFactory(JQueryForm)
    });
}(_, React, molgenis));