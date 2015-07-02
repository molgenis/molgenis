/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div;
	
	var api = new molgenis.RestClient();
	
	/**
	 * @memberOf component
	 */
	var EntitySelectBox = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'EntitySelectBox',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			mode: React.PropTypes.oneOf(['view', 'create']),
			name: React.PropTypes.string,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			multiple: React.PropTypes.bool,
			required: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			focus: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.object, React.PropTypes.array]),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				mode: 'view'
			};
		},
		getInitialState: function() {
			return {
				entity: null,
				modal: false
			};
		},
		render: function() {
			if(this.state.entity === null) {
				// entity meta data not fetched yet
				return molgenis.ui.Spinner();
			}
			
			var props = this.props;
			var entity = this.state.entity;
							
			var options = {
				enable: !this.props.disabled,
				id: entity.idAttribute,
				multiple: props.multiple,
				allowClear : props.required ? false : true,
				placeholder : props.placeholder || ' ', // cannot be an empty string
				closeOnSelect: props.multiple !== true,
			    query: this._select2Query,
			    formatResult: this._select2FormatResult,
			    formatSelection: this._select2FormatSelection
			};
			
			return molgenis.ui.wrapper.Select2({
				options : options,
				name : this.props.name,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				focus: this.props.focus,
				value : this.props.value,
				addonBtn: this.props.mode === 'create' && entity.writable === true,
				addonBtnTitle: this.props.mode === 'create' && entity.writable === true ? 'Create ' + this.state.entity.label : undefined,
				onAddonBtnClick: this._handleAddonBtnClick,
				onChange : this._handleChange
			});
		},
		renderLayer: function() {
			if(this.state.entity === null) {
				return molgenis.ui.Spinner();
			} else if(this.props.mode === 'create') {
				return this.state.modal ? molgenis.ui.Form({
					entity : this.state.entity.name,
					showHidden : true,
					cancelBtn : true,
					modal: true,
					onSubmitCancel : this._onModalHide,
					onSubmitSuccess: this._onModalHide
				}) : div();
			} else {
				return div();
			}
		},
		_handleChange: function(value) {
			var val = this.props.multiple && value.length === 0 ? undefined : value;
			this.props.onValueChange({value: val});
		},
		_handleAddonBtnClick: function() {
			this.setState({modal: true});
		},
		_onModalHide: function() {
			this.setState({modal: false});
		},
		_createQuery: function(term) {
			var rules = [];
			var attrs = this._getAttrs();
			for(var i = 0; i < attrs.length; ++i) {
				if(i > 0) {
					rules.push({operator: 'OR'});	
				}
				rules.push({field: attrs[i], operator: 'LIKE', value: term});
			}
			return rules;
		},
		_select2FormatResult : function(item) {
			var attrs = this._getAttrs();
			if (attrs.length > 1) {
				var items = [];
				items.push('<div class="row">');

				var width = Math.round(12 / attrs.length); // FIXME fix in case of 5, 7 etc. lookup attributes
				for(var i = 0; i < attrs.length; ++i) {
					var attrName = attrs[i];
					var attrLabel = this.state.entity.attributes[attrName].label;
					var attrValue = item[attrName] !== undefined ? item[attrName] : '';
					items.push('<div class="col-md-' + width + '">');
					items.push(htmlEscape(attrLabel) + ': <b>' + htmlEscape(attrValue) + '</b>');
					items.push('</div>');
				}

				items.push('</div>');

				return items.join('');
			} else {
				return item[attrs[0]];
			}
		},
		_select2FormatSelection : function(item) {
			return item[this.state.entity.labelAttribute];
		},
		_select2Query : function (query) {
			var num = 25;
		    var q = {
				q : {
					start : (query.page - 1) * num,
					num : num,
					sort : {
						orders : [ {
							direction : 'ASC',
							property : this._getAttrs()[0]
						} ]
					},
					q: query.term.length > 0 ? this._createQuery(query.term) : undefined,
					expand: this._getAttrsWithRefEntity()
				}
			};
	    	
	    	api.getAsync(this.state.entity.hrefCollection, q).done(function(data) {
	    		query.callback({results: data.items, more: data.nextHref ? true : false});
	    	});
		},
		_getAttrs: function() {
			// display lookup attributes in dropdown or label attribute if no lookup attributes are defined
			var entity = this.state.entity; 
			if(entity.lookupAttributes && entity.lookupAttributes.length > 0) {
				return entity.lookupAttributes;
			} else {
				return [entity.labelAttribute];
			}
		},
		_getAttrsWithRefEntity: function() {
			var attrsWithRefEntity = _.filter(this.state.entity.attributes, function(attr) {
				switch(attr.fieldType) {
					case 'CATEGORICAL':
					case 'CATEGORICAL_MREF':
					case 'MREF':
					case 'XREF':
						return true;
					default:
						return false;
				}
			});
			return _.map(attrsWithRefEntity, function(attr) {
				return attr.name;
			});
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		EntitySelectBox: React.createFactory(EntitySelectBox)
	});
}(_, React, molgenis));