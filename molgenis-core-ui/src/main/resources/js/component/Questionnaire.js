/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";
    
    var div = React.DOM.div;
    
    var Questionnaire = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin],
    	displayName: 'Questionnaire',
    	propTypes: {
    		entity: React.PropTypes.string.isRequired,
    		entityInstance: React.PropTypes.string.isRequired,
    		onContinueLaterClick: React.PropTypes.func,
    		successUrl: React.PropTypes.string
    	},
    	getDefaultProps: function() {
    		return {
    			successUrl: null,
    			onContinueLaterClick: function() {}
    		};
    	},
    	getInitialState: function() {
			return {
				entity : null,			// transfered from props to state, loaded from server if required
				entityInstance : null,	// transfered from props to state, loaded from server if required
			};
    	},
    	_onSubmitClick: function(e) {
    		this.refs.form.submit(e);
    	},
    	render: function() {
    		if(this.state.entity === null || this.state.entityInstance === null) {
				return molgenis.ui.Spinner();
			}
			
    		// a edit form with save-on-blur doesn't have a submit button 
    		var QuestionnaireButtons = this.state.entityInstance.status !== 'SUBMITTED' ? (
    			div({className: 'row', style: {textAlign: 'right'}},
					div({className: 'col-md-12'},
						molgenis.ui.Button({text: 'Save and Continue Later', onClick: this.props.onContinueLaterClick}),
						molgenis.ui.Button({type: 'button', style: 'primary', css: {marginLeft: 5}, text: 'Submit', onClick: this._onSubmitClick})
					)
				)
			) : null;
	
    		var Form = molgenis.ui.Form({
    			entity: this.state.entity,
    			entityInstance: this.state.entityInstance,
    			mode: this.state.entityInstance.status === 'SUBMITTED' ? 'view' : 'edit',
    			formLayout: 'vertical',
    			modal: false,
    	        enableOptionalFilter: false,
    	        saveOnBlur: true,
    	        enableFormIndex: true,
    	        categorigalMrefShowSelectAll: false,
    	        beforeSubmit: this._handleBeforeSubmit,
    	        onValueChange: this._handleValueChange,
    	        onSubmitSuccess: this._handleSubmitSuccess,
    	        ref: 'form'
    	    }, QuestionnaireButtons); 
    		
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
					arr[i].value = 'SUBMITTED';
					break;
				}
			}
		},
		_handleSubmitSuccess: function() {
			if (this.props.successUrl !== null) {
				document.location = this.props.successUrl;
			}
		}
    });
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
    	Questionnaire: React.createFactory(Questionnaire)
    });
}(_, React, molgenis));	    