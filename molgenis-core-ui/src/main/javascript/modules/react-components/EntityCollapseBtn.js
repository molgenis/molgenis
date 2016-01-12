/**
 * @module EntityCollapseBtn
 */
"use strict";

import React from 'react';
import _ from 'underscore';

import Button from './Button';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

/**
 * @memberOf EntityCollapseBtn
 */
var EntityCollapseBtn = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'EntityCollapseBtn',
	propTypes : {
		attrPath : React.PropTypes.array.isRequired,
		onCollapse : React.PropTypes.func,
	},
	getDefaultProps : function() {
		return {
			onCollapse : function() {
			}
		};
	},
	render : function() {
		return Button({
			icon : 'collapse-up',
			size : 'xsmall',
			title : 'Collapse entity',
			onClick : this._handleCollapse
		});
	},
	_handleCollapse : function() {
		this.props.onCollapse({
			attrPath : this.props.attrPath
		});
	}
});

export default React.createFactory(EntityCollapseBtn);
