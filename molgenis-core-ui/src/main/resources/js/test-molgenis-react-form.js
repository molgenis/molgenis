$(function($, molgenis) {
	"use strict";
	
	var div = React.DOM.div, label = React.DOM.label, input = React.DOM.input, hr = React.DOM.hr;
	
	var FormSelect = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'FormSelect',
		getInitialState: function() {
			return {selectedEntity: null, layout: 'vertical'};
		},
		render: function() {console.log('render FormSelect', this.state, this.props);
			var elements = [];
			elements.push(
				div({className: 'well', key: 'select'},
					div({className: 'row'},
						div({className: 'col-md-12'},
							div({className: 'form-group'},
								molgenis.control.EntityControl({entity: this.props.entity, onValueChange: this._onHandleValueChange})
							),
							div({className: 'checkbox', onChange: this._handleCheckboxChange},
								label({},
									input({type: 'checkbox'}, 'Horizontal form')
								)
							)
						)
					)
				)
			);
			
			if(this.state.selectedEntity) {
				elements.push(
					div({className: 'row', key: 'form'},
						div({className: 'col-md-12'},
								molgenis.control.Form({entity: this.state.selectedEntity, layout: this.state.layout})
						)
					)	
				);
			}
			return div({}, elements);
		},
		_onHandleValueChange: function(event) {
			var entity = '/api/v1/' + event.value.fullName + '/meta?expand=attributes';
			this.setState({selectedEntity: entity});
		},
		_handleCheckboxChange: function(event) {
			this.setState({layout: event.target.checked ? 'horizontal' : 'vertical'});
		}
	});
	
	molgenis.test = molgenis.test || {};
	molgenis.test.control = molgenis.test.control || {};
	
	$.extend(molgenis.test.control, {
		FormSelect: React.createFactory(FormSelect)
	});
}($, window.top.molgenis = window.top.molgenis || {}));

$(function($, molgenis) {
	"use strict";
	
	$.get('/api/v1/entities/meta').done(function(meta) {
		var $container = $('#form-container');
		React.render(molgenis.test.control.FormSelect({entity: meta}), $container[0]);
	});
}($, window.top.molgenis = window.top.molgenis || {}));