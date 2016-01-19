/* global _: false, molgenis: true */
(function(_, molgenis) {
	"use strict";
	
	var api = new molgenis.RestClient();
	
	/**
	 * Loads an entity (meta data) from this.props.entity and stores in this.state.entity
	 * 
	 * @memberOf component.mixin
	 */
	var EntityLoaderMixin = {
		componentDidMount: function() {
			this._initEntity(this.props.entity);
		},
		componentWillReceiveProps : function(nextProps) {
			if(!_.isEqual(this.props.entity, nextProps.entity)) {
				this._initEntity(nextProps.entity);
			}
		},
		_isLoaded: function(entity) {
			if(entity.idAttribute !== undefined) {
				return false;
			}
			else if(entity.attributes && entity.attributes.length > 0) {
				return _.size(entity.attributes[0] > 1);
			} else {
				return true;
			}
		},
		_initEntity: function(entity) {
			// fetch entity meta if not exists
			if(entity === undefined) {
				this._setEntity(entity);
			} else if(typeof entity === 'object') {
				if(!this._isLoaded(entity)) {
					this._loadEntity(entity.href);
				} else {
					this._setEntity(entity);
				}
			} else if (typeof entity === 'string') {
				var href = entity.startsWith('/api/') ? entity : '/api/v1/' + entity + '/meta';					
				this._loadEntity(href);
			}
		},
		_loadEntity: function(href) {
			api.getAsync(href, {'expand': ['attributes']}).done(function(entity) {
				if (this.isMounted()) {
					var attributes = molgenis.getAllAttributes(entity.attributes, api);
					entity.allAttributes = {}
					for (var i = 0; i < attributes.length; i++) {
						entity.allAttributes[attributes[i].name] = attributes[i];
					}
					
					this._setEntity(entity);
				}
			}.bind(this));
		},
		_setEntity: function(entity) {
			this.setState({entity: entity});
			if(this._onEntityInit) {
				this._onEntityInit(entity);
			}
		}
	};
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.mixin = molgenis.ui.mixin || {};
	_.extend(molgenis.ui.mixin, {
		EntityLoaderMixin: EntityLoaderMixin
	});
}(_, molgenis));