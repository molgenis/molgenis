/**
 * @module FormControlDescription
 */

import React from 'react';
import _ from 'underscore';
import api from '../RestClientV1';
import URI from 'urijs';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

var span = React.DOM.span, a = React.DOM.a;

/**
 * @memberOf component
 */
var FormControlDescription = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'FormControlDescription',
	propTypes : {
		description : React.PropTypes.string.isRequired
	},
	render : function() {
		var text = this.props.description;

		var keyIdx = 0;
		var idx = 0;
		var DescriptionParts = [];
		URI.withinString(text, function(url, start, end) {
			if (start > idx) {
				DescriptionParts.push(span({
					key : '' + keyIdx++
				}, text.substr(idx, start)));
			}
			DescriptionParts.push(a({
				href : url,
				target : '_blank',
				key : '' + keyIdx++
			}, url));

			idx = end;
			return url;
		});
		if (idx < text.length) {
			DescriptionParts.push(span({
				key : '' + keyIdx++
			}, text.substr(idx)));
		}
		return span({
			className : 'help-block'
		}, DescriptionParts);
	}
});

export default React.createFactory(FormControlDescription);
