import React from 'react';
import { Button } from '../Button';

var EntityViewsTableRow = React.createClass({
	displayName: 'EntityViewsTableRow',
	propTypes: {
		row: React.PropTypes.object.isRequired,
		deleteEntityView: React.PropTypes.func
	},
	render: function() {
		return <tr>
			<td><Button size='xsmall' icon='trash' style='danger' onClick={this.props.deleteEntityView.bind(null, this.props.row)} /></td>
			<td>{this.props.row.name}</td>
			<td>{this.props.row.masterEntity}</td>
			<td>{this.props.row.slaveEntities.map(function(slaveEntity, index) {
				return index > 0 ? ', ' + slaveEntity.slaveEntity : slaveEntity.slaveEntity   
			})}</td>
		</tr>
	}
});

export { EntityViewsTableRow };
export default React.createFactory(EntityViewsTableRow);