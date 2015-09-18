/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div;
	var select = React.DOM.select;
	var option = React.DOM.option;
	
	/**
	 * @memberOf component
	 */
	var SelectBox = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'SelectBox',
		propTypes: {
			options: React.PropTypes.arrayOf(
				React.PropTypes.shape({
					value: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
					text: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number])
				})
			),
			onChange: React.PropTypes.func
		},
		getDefaultProps: function(){
			return {
				options: []
			}
		},
		render: function() {
			return (
				div(null,
					select({className: 'form-control', onChange: this.props.onChange}, this.props.options.map(function(item, i){
						return(
							option({value: item.value, key: i}, item.text)
						);
					}))
				)
			);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		SelectBox: React.createFactory(SelectBox)
	});
}(_, React, molgenis));