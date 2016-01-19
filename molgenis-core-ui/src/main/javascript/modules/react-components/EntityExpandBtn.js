/**
 * @module EntityExpandBtn
 */

import React from 'react';
import _ from 'underscore';

import Button from './Button';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

/**
 * @memberOf EntityExpandBtn
 */
var EntityExpandBtn = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'EntityExpandBtn',
	propTypes : {
		attrPath : React.PropTypes.array.isRequired,
		onExpand : React.PropTypes.func,
	},
	getDefaultProps : function() {
		return {
			onExpand : function() {
			}
		};
	},
	render : function() {
		return Button({
			icon : 'expand',
			size : 'xsmall',
			title : 'Expand entity',
			onClick : this._handleExpand
		});
	},
	_handleExpand : function() {
		this.props.onExpand({
			attrPath : this.props.attrPath
		});
	}
});

export default React.createFactory(EntityExpandBtn);
