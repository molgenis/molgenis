/**
 * @module FormIndex
 */

import React from 'react';
import _ from 'underscore';
import api from '../RestClientV1';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

var div = React.DOM.div, ol = React.DOM.ol, li = React.DOM.li, a = React.DOM.a;

/**
 * @memberOf FormIndex
 */
var FormIndex = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'FormIndex',
	propTypes : {
		entity : React.PropTypes.object.isRequired,
		errorMessageAlertMessage : React.PropTypes.object,
		submitAlertMessage : React.PropTypes.object
	},
	render : function() {
		var IndexItems = [];
		var attrs = this.props.entity.attributes;
		for ( var key in attrs) {
			if (attrs.hasOwnProperty(key)) {
				var attr = attrs[key];
				if (attr.fieldType === 'COMPOUND') {
					var IndexItem = (li({
						key : attr.name,
						className : 'list-group-item'
					}, a({
						href : this._getLinkName(attr)
					}, attr.label)));
					IndexItems.push(IndexItem);
				}
			}
		}

		return (div({
			id : 'sidebar',
			className : 'affix'
		}, this.props.errorMessageAlertMessage, this.props.submitAlertMessage, ol({
			style : {
				listStyleType : 'none'
			},
			className : 'list-group'
		}, IndexItems)));
	},
	_getLinkName : function(attr) {
		return '#' + attr.name + '-link';
	}
});

export default React.createFactory(FormIndex);
