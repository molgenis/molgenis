$(function($, molgenis) {
	"use strict";
	
	var div = React.DOM.div;
	
	var FormSelect = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'FormSelect',
		getInitialState: function() {
			return {selectedEntityType: null, formLayout: 'vertical', mode: 'create'};
		},
		render: function() {
			var elements = [];
			elements.push(
				div({className: 'well', key: 'select'},
					div({className: 'row'},
						div({className: 'col-md-12'},
							div({className: 'form-group'},
 								molgenis.ui.EntitySelectBox({
									entity : this.props.entity,
									value: this.state.selectedEntityType,
									onValueChange : function(e) {
										this.setState({selectedEntityType: e.value, selectedEntityTypeHref: '/api/v1/' + e.value.fullName + '/meta', selectedEntity: undefined});
									}.bind(this)
								})
							),
							this.state.selectedEntityType ? molgenis.ui.RadioGroup({
								options : [ {value : 'create', label : 'Create'}, {value : 'edit', label : 'Edit'}, {value : 'view', label : 'View'} ],
								required: true,
								layout: 'horizontal',
								name: 'mode',
								value: this.state.mode,
								onValueChange: function(e) {
									if(e.value === 'create') {
										this.setState({mode: e.value, selectedEntity : undefined});
									}
									else {
										this.setState({mode: e.value});
									}
								}.bind(this)
							}) : null,
							this.state.selectedEntityType && (this.state.mode === 'edit' || this.state.mode === 'view') ? molgenis.ui.EntitySelectBox({
								entity : this.state.selectedEntityTypeHref,
								value: this.state.selectedEntity,
								onValueChange : function(e) {
									this.setState({selectedEntity: e.value});
								}.bind(this)
							}) : null,
							this.state.selectedEntityType ? molgenis.ui.RadioGroup({
								options : [ {value : 'horizontal',label : 'Horizontal'}, {value : 'vertical', label : 'Vertical'} ],
								required: true,
								layout: 'horizontal',
								name: 'layout',
								value: 'vertical',
								onValueChange: function(e) {
									this.setState({formLayout: e.value});
								}.bind(this)
							}) : null
						)
					)
				)
			);
			
			if(this.state.selectedEntityType && (this.state.mode === 'create' || this.state.selectedEntity)) {
				//var value = {"href":"/api/v1/org_molgenis_test_TypeTest/1","xboolnillable":true,"xbool":true,"id":1,"xcategoricalreadonly_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xcategoricalnillable_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xcategorical_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xdatetimenillable":"1985-08-12T08:12:13+0200","xboolreadonly":true,"xdatereadonly":"1985-08-01","xdatenillable":"1985-08-01","xdatetime":"1985-08-12T08:12:13+0200","xdate":"1985-08-01","xdatetimereadonly":"1985-08-12T08:12:13+0200","xdecimalnillable":1.23,"xdecimalreadonly":1.23,"xenumnillable":"enum1","xemailreadonly":"molgenis@gmail.com","xemailnillable":"molgenis@gmail.com","xdecimal":1.23,"xenum":"enum1","xemail":"molgenis@gmail.com","xhyperlinkreadonly":"http://www.molgenis.org/","xhyperlinknillable":"http://www.molgenis.org/","xenumreadonly":"enum1","xhtmlnillable":"<h1>html</h1>","xhtmlreadonly":"<h1>html</h1>","xhyperlink":"http://www.molgenis.org/","xintnillable":1,"xhtml":"<h1>html</h1>","xint":5,"xintrangereadonly":1,"xintrangenillable":2,"xintcomputed":5,"xlongreadonly":1,"xlongnillable":1,"xintreadonly":5,"xlongrange":2,"xintrange":1,"xlong":1,"xcategoricalmrefreadonly_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xcategoricalmrefreadonly_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xcategoricalmrefnillable_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xcategoricalmrefnillable_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xcategoricalmref_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xcategoricalmref_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xmrefreadonly_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xmrefreadonly_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xmrefnillable_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xmrefnillable_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xmref_value":{"href":"/api/v1/org_molgenis_test_TypeTest/1/xmref_value","start":0,"num":100,"total":1,"items":[{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"}]},"xlongrangereadonly":2,"xlongrangenillable":2,"xscriptnillable":"x <- c(1,2,3,4,5,6)   # Create ordered collection (vector)\nprint(y)              # print (vector) y\nmean(y)               # Calculate average (arithmetic mean) of (vector) y; result is scalar\nvar(y)                # Calculate sample variance","xscript":"x <- c(1,2,3,4,5,6)   # Create ordered collection (vector)\nprint(y)              # print (vector) y\nmean(y)               # Calculate average (arithmetic mean) of (vector) y; result is scalar\nvar(y)                # Calculate sample variance","xxref_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xscriptreadonly":"x <- c(1,2,3,4,5,6)   # Create ordered collection (vector)\nprint(y)              # print (vector) y\nmean(y)               # Calculate average (arithmetic mean) of (vector) y; result is scalar\nvar(y)                # Calculate sample variance","xstringreadonly":"str1","xstringnillable":"str1","xtextreadonly":"text","xtextnillable":"text","xstring":"str1","xtext":"text","xxrefreadonly_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xxrefnillable_value":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xxref_unique":{"href":"/api/v1/org_molgenis_test_TypeTestRef/ref1","value":"ref1","label":"label1"},"xstringnillable_hidden":"hidden","xstring_unique":"str1","xstring_hidden":"hidden","xint_unique":1};
				
				elements.push(
					div({className: 'row', key: 'form'},
						div({className: 'col-md-12'},
								molgenis.ui.Form({entity: this.state.selectedEntityTypeHref, value: this.state.selectedEntity, formLayout: this.state.formLayout, mode: this.state.mode})
						)
					)	
				);
			}
			return div({}, elements);
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
		React.render(molgenis.test.control.FormSelect({
			entity : meta
		}), $container[0]);
	});
}($, window.top.molgenis = window.top.molgenis || {}));