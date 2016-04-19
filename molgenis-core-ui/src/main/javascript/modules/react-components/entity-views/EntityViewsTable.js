import React from 'react';
import { Button } from '../Button';

var EntityViewsTable = React.createClass({
	displayName: 'EntityViewsTable',
	propTypes: {
		views: React.PropTypes.array,
		deleteEntityView: React.PropTypes.func
	},
	_renderMapping: function(view, slave, slaveIndex, mapping, mappingIndex) {
        var { deleteEntityView } = this.props;
		const {name, masterEntity} = view;
        return <tr>
            <td>{!slaveIndex && !mappingIndex && <Button size='xsmall' icon='trash' style='danger' onClick={deleteEntityView.bind(null, view)}/>}</td>
            <td>{!slaveIndex && !mappingIndex && name}</td>
            <td>{!slaveIndex && !mappingIndex && masterEntity}</td>
            <td>{!mappingIndex && slave.slaveEntity}</td>
            <td>{mapping.masterAttribute} = {mapping.joinAttribute}</td>
		</tr>;
	},
    _renderSlave: function(view, slave, slaveIndex) {
        return slave.joinedAttributes.map( (mapping, mappingIndex) => this._renderMapping(view, slave, slaveIndex, mapping, mappingIndex));
    },
	_renderView: function(view){
        const slaveRows = view.slaveEntities.map( (slave, slaveIndex) => this._renderSlave(view, slave, slaveIndex) )
        // reduce to single array
		return slaveRows.reduce((a, b) => a.concat(b), []);
	},
	render: function() {
		return <div>
			{this.props.views.length > 0 ?
				<table className='table table-bordered'>
					<thead>
						<th></th>
						<th>View name</th>
						<th>Master entity name</th>
						<th>Joined entities</th>
                        <th>Joined attributes</th>
					</thead>
					<tbody>
					{this.props.views.map((view, index) => this._renderView(view))}
					</tbody>
				</table>
			: null}
		</div>
	}
});

export { EntityViewsTable };
export default React.createFactory(EntityViewsTable);