define(function(require, exports, module) {
	/**
	 * @module mixin
	 */

	'use strict';

	var AttributeLoaderMixin = require('component/mixin/AttributeLoaderMixin');
	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');
	var EntityInstanceLoaderMixin = require('component/mixin/EntityInstanceLoaderMixin');
	var EntityLoaderMixin = require('component/mixin/EntityLoaderMixin');
	var GroupMixin = require('component/mixin/GroupMixin');
	var I18nStringsMixin = require('component/mixin/I18nStringsMixin');
	var ReactLayeredComponentMixin = require('component/mixin/ReactLayeredComponentMixin');

	var mixin = {
		'AttributeLoaderMixin' : AttributeLoaderMixin,
		'DeepPureRenderMixin' : DeepPureRenderMixin,
		'EntityInstanceLoaderMixin' : EntityInstanceLoaderMixin,
		'EntityLoaderMixin' : EntityLoaderMixin,
		'GroupMixin' : GroupMixin,
		'I18nStringsMixin' : I18nStringsMixin,
		'ReactLayeredComponentMixin' : ReactLayeredComponentMixin
	}

	module.exports = mixin;
});