/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var span = React.DOM.span;
	
	/**
	 * @memberOf component
	 */
	var Popover = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Popover',
		propTypes: {
			value: React.PropTypes.string.isRequired,
			popoverValue: React.PropTypes.string.isRequired,
		},
		componentDidMount: function() {
			var $container = $(this.refs.popover.getDOMNode());
			$container.popover({
				trigger : 'hover click',
				placement : 'bottom',
				container : 'body'
			});
		},
		componentWillUnmount: function() {
			var $container = $(this.refs.popover.getDOMNode());
			$container.popover('destroy');
		},
		render: function() {
			return span({
				'data-content' : this.props.popoverValue,
				'data-toggle' : 'popover',
				ref: 'popover'
			}, this.props.value);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Popover: React.createFactory(Popover)
	});
}(_, React, molgenis));