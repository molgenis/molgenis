$(function() {
	var template = Handlebars.compile($("#attr-control-form-template").html());

	function onValueChange(event) {
		console.log(event);
	}
	
	$.get('/api/v1/org_molgenis_test_TypeTest/meta?expand=attributes').done(function(meta) {
		var $container = $('#input-container');
		for(var key in meta.attributes) {
			var attr = meta.attributes[key];
			if(attr.name !== 'id') {

				if(attr.name === 'xcategorical_value') {
					

					$container.append(template({name: attr.name}));
					var value = [{value: "ref2", label: "label2"}];
					React.render(molgenis.control.EntityControl({entity: attr.refEntity, multiple: true, onValueChange: onValueChange, value: value}), $container[0]);

					//try {
// 						molgenis.controls.create(attr, {
// 							attr: attr,
// 							onValueChange : function(value) {
// 								console.log(value);
// 							}
// 						}, $('#' + attr.name + '-filter-container'));
					//} catch(err) {
						//console.log(err);
					//}
				}
				 if(attr.name === 'xenum') {
					var $container = $('#input-container-enum');
					var value = ["enum2"];
					React.render(molgenis.control.AttributeControl({attr: attr, multiple: true, onValueChange: onValueChange, value: value}), $container[0]);
				}
			}
		}
	});
	
	var options = [{value: 'd', label: 'Dog'}, {value: 'c', label: 'Cat'}, {value: 'm', label: 'Mouse'}];
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked'}), $('#input-container-radiogroup')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', value: 'c'}), $('#input-container-radiogroup-value')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true}), $('#input-container-radiogroup-nillable')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true, value: 'c'}), $('#input-container-radiogroup-nillable-value')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true, value: null}), $('#input-container-radiogroup-nillable-value-null')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'inline'}), $('#input-container-radiogroup-inline')[0]);
	React.render(molgenis.control.RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', disabled: true}), $('#input-container-radiogroup-inline-disabled')[0]);
	
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked'}), $('#input-container-checkboxgroup')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', value: ['c']}), $('#input-container-checkboxgroup-value')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true}), $('#input-container-checkboxgroup-nillable')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true, value: ['c']}), $('#input-container-checkboxgroup-nillable-value')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', nillable: true, value: [null]}), $('#input-container-checkboxgroup-nillable-value-null')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'inline'}), $('#input-container-checkboxgroup-inline')[0]);
	React.render(molgenis.control.CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', disabled: true}), $('#input-container-checkboxgroup-inline-disabled')[0]);

	React.render(molgenis.control.BoolControl({onValueChange: onValueChange}), $('#input-container-bool')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, value: true}), $('#input-container-bool-value-true')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, value: false}), $('#input-container-bool-value-false')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, nillable: true}), $('#input-container-bool-nillable')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, nillable: true, value: null}), $('#input-container-bool-nillable-value')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, multiple: true}), $('#input-container-bool-multiple')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, multiple: true, value: [true, false]}), $('#input-container-bool-multiple-value')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, multiple: true, nillable: true}), $('#input-container-bool-multiple-nillable')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, multiple: true, nillable: true, value: [false, null]}), $('#input-container-bool-multiple-nillable-value')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, type: 'group'}), $('#input-container-bool-group')[0]);
	React.render(molgenis.control.BoolControl({onValueChange: onValueChange, type: 'group', value: true}), $('#input-container-bool-group-value')[0]);
});