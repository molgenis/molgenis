/* global _: false, molgenis: true */
(function(_, molgenis) {
	"use strict";
	
	var api = new molgenis.RestClient();
	
	/**
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
			return entity.idAttribute !== undefined;
		},
		_initEntity: function(entity) {
			// fetch entity meta if not exists
			if(typeof entity === 'object') {
				if(!this._isLoaded(entity)) {
					this._loadEntity(entity.href);
				} else {
					this.setState({entity: entity});
				}
			} else if (typeof entity === 'string') {
				var href = entity.startsWith('/api/') ? entity : '/api/v1/' + entity + '/meta';					
				this._loadEntity(href);
			}
		},
		_loadEntity: function(href) {
			api.getAsync(href, {'expand': ['attributes']}).done(function(entity) {
				if (this.isMounted()) {
					this.setState({entity: entity});
				}
			}.bind(this));
		},
		_setEntity: function(entity) {
			this.setState({entity: entity});
			if(this.props.onEntityInit) {
				this.props.onEntityInit(entity);
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