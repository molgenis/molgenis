$(function() {

	function onValueChange(event) {
		$('#value').html(JSON.stringify(event));
	}
	
	var InputControl = molgenis.control.InputControl;
	React.render(InputControl({type: 'text', onValueChange: onValueChange}), $('#input-container-input-text')[0]);
	React.render(InputControl({type: 'text', value: 'abcd', onValueChange: onValueChange}), $('#input-container-input-text-value')[0]);
	React.render(InputControl({type: 'text', placeholder: 'Placeholder', onValueChange: onValueChange}), $('#input-container-input-text-placeholder')[0]);
	React.render(InputControl({type: 'text', required: true, onValueChange: onValueChange}), $('#input-container-input-text-required')[0]);
	React.render(InputControl({type: 'text', readOnly: true, onValueChange: onValueChange}), $('#input-container-input-text-readonly')[0]);
	React.render(InputControl({type: 'text', disabled: true, onValueChange: onValueChange}), $('#input-container-input-text-disabled')[0]);
	React.render(InputControl({type: 'number', onValueChange: onValueChange}), $('#input-container-input-number')[0]);
	React.render(InputControl({type: 'number', value: '4', onValueChange: onValueChange}), $('#input-container-input-number-value')[0]);
	React.render(InputControl({type: 'checkbox', onValueChange: onValueChange}), $('#input-container-input-checkbox')[0]);
	React.render(InputControl({type: 'checkbox', value: 'checkbox-value', onValueChange: onValueChange}), $('#input-container-input-checkbox-value')[0]);
	React.render(InputControl({type: 'checkbox', checked: true, onValueChange: onValueChange}), $('#input-container-input-checkbox-checked')[0]);
	React.render(InputControl({type: 'radio', onValueChange: onValueChange}), $('#input-container-input-radio')[0]);
	React.render(InputControl({type: 'radio', value: 'radio-value', onValueChange: onValueChange}), $('#input-container-input-radio-value')[0]);
	React.render(InputControl({type: 'radio', checked: true, onValueChange: onValueChange}), $('#input-container-input-radio-checked')[0]);
	
	var options = [{value: 'd', label: 'Dog'}, {value: 'c', label: 'Cat'}, {value: 'm', label: 'Mouse'}];
	
	var RadioGroupControl = molgenis.control.RadioGroupControl;
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked'}), $('#input-container-radiogroup')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', value: 'c'}), $('#input-container-radiogroup-value')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true}), $('#input-container-radiogroup-required')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true, value: 'c'}), $('#input-container-radiogroup-required-value')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true, value: null}), $('#input-container-radiogroup-required-value-null')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'inline'}), $('#input-container-radiogroup-inline')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', readOnly: true}), $('#input-container-radiogroup-inline-readonly')[0]);
	React.render(RadioGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', disabled: true}), $('#input-container-radiogroup-inline-disabled')[0]);
	
	var CheckboxGroupControl = molgenis.control.CheckboxGroupControl;
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked'}), $('#input-container-checkboxgroup')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', value: ['c']}), $('#input-container-checkboxgroup-value')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true}), $('#input-container-checkboxgroup-required')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true, value: ['c']}), $('#input-container-checkboxgroup-required-value')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'stacked', required: true, value: [null]}), $('#input-container-checkboxgroup-required-value-null')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'inline'}), $('#input-container-checkboxgroup-inline')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', readOnly: true}), $('#input-container-checkboxgroup-inline-readonly')[0]);
	React.render(CheckboxGroupControl({options: options, onValueChange: onValueChange, layout: 'inline', disabled: true}), $('#input-container-checkboxgroup-inline-disabled')[0]);

	var DateControl = molgenis.control.DateControl;
	React.render(DateControl({onValueChange: onValueChange}), $('#input-container-date')[0]);
	React.render(DateControl({value: '2014-12-31', onValueChange: onValueChange}), $('#input-container-date-value')[0]);
	React.render(DateControl({placeholder: 'Select a date', onValueChange: onValueChange}), $('#input-container-date-placeholder')[0]);
	React.render(DateControl({readOnly: true, onValueChange: onValueChange}), $('#input-container-date-readonly')[0]);
	React.render(DateControl({disabled: true, onValueChange: onValueChange}), $('#input-container-date-disabled')[0]);
	React.render(DateControl({required: true, onValueChange: onValueChange}), $('#input-container-date-required')[0]);
	React.render(DateControl({time: true, onValueChange: onValueChange}), $('#input-container-datetime')[0]);
	React.render(DateControl({time: true, value: '2014-12-31T23:59:58+0100', onValueChange: onValueChange}), $('#input-container-datetime-value')[0]);
	
	var TextAreaControl = molgenis.control.TextAreaControl;
	React.render(TextAreaControl({onValueChange: onValueChange}), $('#input-container-text')[0]);
	React.render(TextAreaControl({value: 'Lorum ipsum', onValueChange: onValueChange}), $('#input-container-text-value')[0]);
	React.render(TextAreaControl({placeholder: 'Enter some text', onValueChange: onValueChange}), $('#input-container-text-placeholder')[0]);
	React.render(TextAreaControl({readOnly: true, onValueChange: onValueChange}), $('#input-container-text-readonly')[0]);
	React.render(TextAreaControl({disabled: true, onValueChange: onValueChange}), $('#input-container-text-disabled')[0]);
	
	var CodeEditorControl = molgenis.control.CodeEditorControl;
	React.render(CodeEditorControl({onValueChange: onValueChange}), $('#input-container-codeeditor')[0]);
	React.render(CodeEditorControl({value: 'Lorum ipsum', onValueChange: onValueChange}), $('#input-container-codeeditor-value')[0]);
	React.render(CodeEditorControl({placeholder: 'Enter some text', onValueChange: onValueChange}), $('#input-container-codeeditor-placeholder')[0]);
	React.render(CodeEditorControl({readOnly: true, onValueChange: onValueChange}), $('#input-container-codeeditor-readonly')[0]);
	React.render(CodeEditorControl({disabled: true, onValueChange: onValueChange}), $('#input-container-codeeditor-disabled')[0]);
	
	var BoolControl = molgenis.control.BoolControl;
	React.render(BoolControl({onValueChange: onValueChange,required: true}), $('#input-container-bool')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, value: true}), $('#input-container-bool-value-true')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, value: false}), $('#input-container-bool-value-false')[0]);
	React.render(BoolControl({onValueChange: onValueChange}), $('#input-container-bool-nillable')[0]);
	React.render(BoolControl({onValueChange: onValueChange, value: null}), $('#input-container-bool-nillable-value')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, multiple: true}), $('#input-container-bool-multiple')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, multiple: true, value: [true, false]}), $('#input-container-bool-multiple-value')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, multiple: true, nillable: true}), $('#input-container-bool-multiple-nillable')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, multiple: true, nillable: true, value: [false, null]}), $('#input-container-bool-multiple-nillable-value')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, type: 'group'}), $('#input-container-bool-group')[0]);
	React.render(BoolControl({onValueChange: onValueChange, required: true, type: 'group', value: true}), $('#input-container-bool-group-value')[0]);
});