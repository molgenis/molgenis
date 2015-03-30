/* global $: false, _: false, React: false, molgenis: true */
(function($, _, React, molgenis) {
	"use strict";

	var div = React.DOM.div;
	
	/**
	 * React component for range slider jQRangeSlider (http://ghusse.github.io/jQRangeSlider/)
	 * 
	 * @memberOf component.wrapper
	 */
	var JQRangeSlider = React.createClass({
		displayName: 'JQRangeSlider',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		propTypes: {
			id: React.PropTypes.string,
			options: React.PropTypes.object,
			disabled: React.PropTypes.bool,
			onChange: React.PropTypes.func.isRequired
		},
		componentDidMount: function() {
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.editRangeSlider(this.props.options);

			if(this.props.disabled) {
				$container.editRangeSlider('disable');
			}

			var props = this.props;
			/* jshint unused: false */
			$container.on('userValuesChanged', function(e, data) {
				props.onChange([data.values.min, data.values.max]);
			});
			/* jshint unused: true */
		},
		componentWillUnmount: function() {
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.off();
			$container.editRangeSlider('destroy');
		},
		render: function() {
			// workaround for JQRangeSlider edit boxes going out of bounds
			return (
				div({className: 'row'},
					div({className: 'col-md-offset-1 col-md-10'},
						div({id: this.props.id, ref: 'rangeslider'})
					)
				)
			);
		},
		componentDidUpdate: function() {
			if(this.isMounted()) {
				var $container = $(this.refs.rangeslider.getDOMNode());
				$container.editRangeSlider(this.props.disabled ? 'disable' : 'enable');
				$container.editRangeSlider('values', this.props.value[0], this.props.value[1]);
			}
		},
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.wrapper = molgenis.ui.wrapper || {};
	_.extend(molgenis.ui.wrapper, {
		JQRangeSlider: React.createFactory(JQRangeSlider)
	});
}($, _, React, molgenis));