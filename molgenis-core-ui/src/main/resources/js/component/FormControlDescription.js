define(function(require, exports, module) {

	"use strict";

	var React = require('react-with-addons.min');
	var molgenis = require('molgenis');
	var _ = require('underscore-min');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');

	var span = React.DOM.span, a = React.DOM.a;
	var api = new molgenis.RestClient();

	/**
	 * @memberOf component
	 */
	exports.FormControlDescription = React.createClass({
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
});