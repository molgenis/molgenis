/**
 * @module mixin
 */

var AttributeLoaderMixin = require('./AttributeLoaderMixin');
var DeepPureRenderMixin = require('./DeepPureRenderMixin');
var EntityInstanceLoaderMixin = require('./EntityInstanceLoaderMixin');
var EntityLoaderMixin = require('./EntityLoaderMixin');
var GroupMixin = require('./GroupMixin');
var I18nStringsMixin = require('./I18nStringsMixin');
var ReactLayeredComponentMixin = require('./ReactLayeredComponentMixin');

var mixin = {
	'AttributeLoaderMixin' : AttributeLoaderMixin,
	'DeepPureRenderMixin' : DeepPureRenderMixin,
	'EntityInstanceLoaderMixin' : EntityInstanceLoaderMixin,
	'EntityLoaderMixin' : EntityLoaderMixin,
	'GroupMixin' : GroupMixin,
	'I18nStringsMixin' : I18nStringsMixin,
	'ReactLayeredComponentMixin' : ReactLayeredComponentMixin
}

export default mixin;
