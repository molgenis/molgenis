/**
 * @module FormControls
 */
"use strict";

import React from 'react';
import _ from 'underscore';
import api from '../RestClientV1';

import FormControlGroup from './FormControlGroup';
import FormControl from './FormControl';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

var div = React.DOM.div;

/**
 * @memberOf FormControls
 */
var FormControls = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'FormControls',
	propTypes : {
		entity : React.PropTypes.object.isRequired,
		value : React.PropTypes.object,
		mode : React.PropTypes.oneOf([ 'create', 'edit', 'view' ]),
		formLayout : React.PropTypes.oneOf([ 'horizontal', 'vertical' ]),
		colOffset : React.PropTypes.number,
		hideOptional : React.PropTypes.bool,
		showHidden : React.PropTypes.bool,
		categoricalMrefShowSelectAll : React.PropTypes.bool,
		showAsteriskIfNotNillable : React.PropTypes.bool,
		enableFormIndex : React.PropTypes.bool,
		enableAlertMessageInFormIndex : React.PropTypes.bool,
		errorMessages : React.PropTypes.object.isRequired,
		onValueChange : React.PropTypes.func.isRequired,
		onBlur : React.PropTypes.func.isRequired
	},
	render : function() {
		// add control for each attribute
		var foundFocusControl = false;
		var attributes = this.props.entity.attributes;
		var controls = [];
		for ( var key in attributes) {
			if (attributes.hasOwnProperty(key)) {
				var attr = attributes[key];
				if (this.props.mode !== 'create' || (this.props.mode === 'create' && attr.auto !== true)) {
					var ControlFactory = attr.fieldType === 'COMPOUND' ? FormControlGroup : FormControl;
					var controlProps = {
						entity : this.props.entity,
						entityInstance : this.props.value,
						attr : attr,
						value : attr.fieldType === 'COMPOUND' ? this.props.value : (this.props.value ? this.props.value[key] : undefined),
						mode : this.props.mode,
						formLayout : this.props.formLayout,
						colOffset : this.props.colOffset,
						onBlur : this.props.onBlur,
						categoricalMrefShowSelectAll : this.props.categoricalMrefShowSelectAll,
						showAsteriskIfNotNillable : this.props.showAsteriskIfNotNillable,
						onValueChange : this.props.onValueChange,
						key : key
					};

					if (attr.fieldType === 'COMPOUND') {
						_.extend(controlProps, {
							errorMessages : this.props.errorMessages,
							hideOptional : this.props.hideOptional
						});
					} else {
						controlProps['errorMessage'] = this.props.errorMessages[attr.name];
					}

					// IE9 does not support the autofocus attribute, focus
					// the first visible input manually
					if (this.props.mode !== 'view' && !foundFocusControl && attr.visible === true && (this.props.mode === 'create' || attr.readOnly !== true)) {
						_.extend(controlProps, {
							focus : true
						});
						foundFocusControl = true;
					}

					var Control = ControlFactory(controlProps);
					if ((attr.nillable === true && this.props.hideOptional === true || (this.props.showHidden === false && attr.visible === false))
							|| ((attr.visibleExpression !== undefined) && (this.props.entity.allAttributes[attr.name].visible === false))) {
						Control = div({
							className : 'hide',
							key : key + '-hide'
						}, Control);
					} else if (this.props.enableFormIndex === true && attr.fieldType === 'COMPOUND') {
						controls.push(div({
							id : this._getLinkId(attr),
							className : 'anchor',
							key : key + '-link'
						}));
					}
					controls.push(Control);
				}
			}
		}
		return div({}, controls);
	},
	_getLinkId : function(attr) {
		return attr.name + '-link';
	}
});

export default React.createFactory(FormControls);
