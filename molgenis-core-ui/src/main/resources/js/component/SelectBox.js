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
			options: React.PropTypes.array,
			onChange: React.PropTypes.func
		},
		getDefaultProps: function(){
			return {
				options: [{value: 'val1', text: 'option1'}]
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