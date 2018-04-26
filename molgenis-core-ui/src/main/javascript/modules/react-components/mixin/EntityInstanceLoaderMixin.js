import RestClient from "rest-client/RestClientV1";
import _ from "underscore";

var api = new RestClient();

/**
 * Loads an entity instance (row of data) from this.props.entityInstance and stores in this.state.entityInstance
 *
 * @memberOf component.mixin
 */
const EntityInstanceLoaderMixin = {
    _isEntityInstanceLoaded: function (entityInstance) {
        return (this.props.mode !== 'create') && (typeof entityInstance === 'object') && (_.size(entityInstance) > 1);
    },
    _initEntityInstance: function (entity, entityInstance) {
        // fetch entity instance if not exists
        if (typeof entityInstance === 'object') {
            if (!this._isEntityInstanceLoaded(entityInstance)) {
                this._loadEntityInstance(entity, entityInstance.href);
            } else {
                this._setEntityInstance(entity, entityInstance);
            }
        } else if (typeof entityInstance === 'string') {
            if (entity && entity.name) {
                var href = entityInstance.startsWith('/api/') ? entityInstance : '/api/v1/' + this.state.entity.name + '/' + entityInstance;
                this._loadEntityInstance(entity, href);
            }
        } else if (typeof entityInstance === 'number') {
            if (entity && entity.name) {
                var href = '/api/v1/' + this.state.entity.name + '/' + entityInstance.toString();
                this._loadEntityInstance(entity, href);
            }
        }
    },
    _loadEntityInstance: function (entity, href) {
        if (entity && entity.name) {
            // expand attributes with ref entity
            var expands = _.chain(entity.allAttributes).filter(function (attr) {
                return attr.refEntity !== undefined;
            }).map(function (attr) {
                return attr.name;
            }).value();

            api.getAsync(href, expands.length > 0 ? {'expand': expands} : undefined).done(function (entityInstance) {
                if (this.isMounted()) {
                    this._setEntityInstance(entity, entityInstance);
                }
            }.bind(this));
        }
    },
    _setEntityInstance: function (entity, entityInstance) {
        if (this._willSetEntityInstance) {
            this._willSetEntityInstance(entity, entityInstance);
        }

        this.setState({entityInstance: entityInstance});

        if (this._onEntityInstanceInit) {
            this._onEntityInstanceInit(entityInstance);
        }
    },
    _onEntityInit: function (entity) {
        if (this.props.mode === 'create') {
            this._setEntityInstance(entity, {});
        } else {
            this._initEntityInstance(entity, this.props.entityInstance);
        }
    }
};

export default EntityInstanceLoaderMixin;