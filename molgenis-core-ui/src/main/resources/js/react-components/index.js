define(function(require, exports, module) {
	/**
	 * @module
	 */

	'use strict';

	var mixin = require('component/global-ui/Mixin');
	var wrapper = require('component/global-ui/Wrapper');

	// All the React components
	var Button = require('component/Button');
	var AggregateTable = require('component/AggregateTable');
	var AlertMessage = require('component/AlertMessage');
	var AttributeControl = require('component/AttributeControl');
	var BoolControl = require('component/BoolControl');
	var Button = require('component/Button');
	var CheckboxGroup = require('component/CheckboxGroup');
	var CodeEditor = require('component/CodeEditor');
	var DateControl = require('component/DateControl');
	var Dialog = require('component/Dialog');
	var EntitySelectBox = require('component/EntitySelectBox');
	var Form = require('component/Form');
	var FormButtons = require('component/FormButtons');
	var FormControl = require('component/FormControl');
	var FormControlDescription = require('component/FormControlDescription');
	var FormControlGroup = require('component/FormControlGroup');
	var FormControls = require('component/FormControls');
	var FormIndex = require('component/FormIndex');
	var Icon = require('component/Icon');
	var Input = require('component/Input');
	var LanguageSelectBox = require('component/LanguageSelectBox');
	var Modal = require('component/Modal');
	var Pager = require('component/Pager');
	var Popover = require('component/Popover');
	var Questionnaire = require('component/Questionnaire');
	var RadioGroup = require('component/RadioGroup');
	var RangeSlider = require('component/RangeSlider');
	var SelectBox = require('component/SelectBox');
	var Spinner = require('component/Spinner');
	var Table = require('component/Table');
	var TextArea = require('component/TextArea');

	exports.ui = {
		'mixin' : mixin,
		'wrapper' : wrapper,
		'Button' : Button,
		'AggregateTable' : AggregateTable,
		'AlertMessage' : AlertMessage,
		'AttributeControl' : AttributeControl,
		'BoolControl' : BoolControl,
		'Button' : Button,
		'CheckboxGroup' : CheckboxGroup,
		'CodeEditor' : CodeEditor,
		'DateControl' : DateControl,
		'Dialog' : Dialog,
		'EntitySelectBox' : EntitySelectBox,
		'Form' : Form,
		'FormButtons' : FormButtons,
		'FormControl' : FormControl,
		'FormControlDescription' : FormControlDescription,
		'FormControlGroup' : FormControlGroup,
		'FormControls' : FormControls,
		'FormIndex' : FormIndex,
		'Icon' : Icon,
		'Input' : Input,
		'LanguageSelectBox' : LanguageSelectBox,
		'Modal' : Modal,
		'Pager' : Pager,
		'Popover' : Popover,
		'Questionnaire' : Questionnaire,
		'RadioGroup' : RadioGroup,
		'RangeSlider' : RangeSlider,
		'SelectBox' : SelectBox,
		'Spinner' : Spinner,
		'Table' : Table,
		'TextArea' : TextArea
	};

	module.exports = ui;
});