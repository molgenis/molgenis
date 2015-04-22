/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";
    
    var div = React.DOM.div;
    
    var Questionnaire = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin],
    	displayName: 'Questionnaire',
    	propTypes: {
    		entity: React.PropTypes.string.isRequired,
    		entityInstance: React.PropTypes.string.isRequired
    	},
    	getInitialState: function() {
			return {
				entity : null,			// transfered from props to state, loaded from server if required
				entityInstance : null,	// transfered from props to state, loaded from server if required
			};
    	},
    	render: function() {
    		if(this.state.entity === null || this.state.entityInstance === null) {
				return molgenis.ui.Spinner();
			}
			
    		// a edit form with save-on-blur doesn't have a submit button 
    		var SubmitButton = (
    			div({className: 'row', style: {textAlign: 'right'}},
					div({className: 'col-md-12'},
						molgenis.ui.Button({type: 'submit', style: 'primary', text: 'Submit'})
					)
				)
			);
			
    		var Form = molgenis.ui.Form({
    			entity: this.state.entity,
    			entityInstance: this.state.entityInstance,
    			mode: 'edit',
    			formLayout: 'vertical',
    			modal: false,
    	        enableOptionalFilter: false,
    	        saveOnBlur: true,
    	        enableFormIndex: true,
    	        beforeSubmit: this._handleBeforeSubmit,
    	        onValueChange: this._handleValueChange
    	    }, SubmitButton); 
    		
    		return div(null,
    			Form
    		);
		},
		_handleValueChange:function(e) {
			// update value in entity instance
			var entityInstance = _.extend({}, this.state.entityInstance);
			entityInstance[e.attr] = e.value;
			this.setState({entityInstance: entityInstance});
		},
		_handleBeforeSubmit: function(arr, $form, options) {
			for(var i = 0; i < arr.length; ++i) {
				if(arr[i].name === 'status') {
					if(arr[i].value !== 'SUBMITTED') {
						// update status
						var entityInstance = _.extend({}, this.state.entityInstance, {status: 'SUBMITTED'});
						this.setState({
							entityInstance : entityInstance
						}, function() {
							// resubmit form
							$form.ajaxSubmit(options);
						});
						
						// do not submit form
						return false;
					}
					break;
				} 
			}
		}
    });
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
    	Questionnaire: React.createFactory(Questionnaire)
    });
}(_, React, molgenis));	    