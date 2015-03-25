/* global _: false, molgenis: true */
(function(_, molgenis) {
	"use strict";
	
	var api = new molgenis.RestClient();
	
	/**
	 * Loads an entity instance (row of data) from this.props.entityInstance and stores in this.state.entityInstance
	 * 
	 * @memberOf component.mixin
	 */
	var EntityInstanceLoaderMixin = {
		componentDidMount: function() {
			this._initEntityInstance(this.state.entity, this.props.entityInstance);
		},
		componentWillReceiveProps : function(nextProps) {
			if(!_.isEqual(this.props.entityInstance, nextProps.entityInstance)) {
				this._initEntityInstance(this.state.entity, nextProps.entityInstance);
			}
		},
		_isEntityInstanceLoaded: function(entityInstance) {
			return typeof entityInstance === 'object' && _.size(entityInstance) > 1; 
		},
		_initEntityInstance: function(entity, entityInstance) {
			// fetch entity instance if not exists
			if(typeof entityInstance === 'object') {
				if(!this._isEntityInstanceLoaded(entityInstance)) {
					this._loadEntityInstance(entityInstance.href);
				} else {
					this.setState({entityInstance: entityInstance});
				}
			} else if (typeof entityInstance === 'string') {
				if(entity && entity.name) {
					var href = entityInstance.startsWith('/api/') ? entityInstance : '/api/v1/' + this.state.entity.name + '/' + entityInstance;					
					this._loadEntityInstance(href);
				}
			}
		},
		_loadEntityInstance: function(href) {
			api.getAsync(href).done(function(entityInstance) {
				if (this.isMounted()) {
					this.setState({entityInstance: entityInstance});
				}
			}.bind(this));
		},
		_setEntityInstance: function(entityInstance) {
			this.setState({entityInstance: entityInstance});
			if(this._onEntityInstanceInit) {
				this._onEntityInstanceInit(entityInstance);
			}
		},
		_onEntityInit: function(entity) {
			this._initEntityInstance(entity, this.props.entityInstance);
		}
	};
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.mixin = molgenis.ui.mixin || {};
	_.extend(molgenis.ui.mixin, {
		EntityInstanceLoaderMixin: EntityInstanceLoaderMixin
	});
}(_, molgenis));