/**
 * @module mixin
 */

import AttributeLoaderMixin from './AttributeLoaderMixin';
import DeepPureRenderMixin from './DeepPureRenderMixin';
import EntityInstanceLoaderMixin from './EntityInstanceLoaderMixin';
import EntityLoaderMixin from './EntityLoaderMixin';
import GroupMixin from './GroupMixin';
import I18nStringsMixin from './I18nStringsMixin';
import ReactLayeredComponentMixin from './ReactLayeredComponentMixin';

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
