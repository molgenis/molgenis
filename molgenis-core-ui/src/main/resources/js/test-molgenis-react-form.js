$(function() {
	
	var div = React.DOM.div;
	
	var FormSelect = React.createClass({
//		mixins: [DeepPureRenderMixin],
		displayName: 'FormSelect',
		getInitialState: function() {
			return {selectedEntity: null};
		},
		render: function() {console.log('render FormSelect', this.state, this.props);
			var elements = [];
			elements.push(
				div({className: 'row', key: 'select'},
					div({className: 'col-md-12'},
						div({className: 'form-group'},
							molgenis.controls.EntityControl({entity: this.props.entity, onValueChange: this._onHandleValueChange})
						)
					)
				)
			);
			
			if(this.state.selectedEntity) {
				elements.push(
					div({className: 'row', key: 'form'},
						div({className: 'col-md-12'},
							molgenis.Form({entity: this.state.selectedEntity})
						)
					)	
				);
			}
			return div({}, elements);
		},
		_onHandleValueChange: function(event) {
			var entity = '/api/v1/' + event.value.fullName + '/meta?expand=attributes';
			this.setState({selectedEntity: entity});
		}
	});
	
	$.get('/api/v1/entities/meta').done(function(meta) {
		var $container = $('.container-fluid');
		React.render(FormSelect({entity: meta}), $container[0]);
	});
});