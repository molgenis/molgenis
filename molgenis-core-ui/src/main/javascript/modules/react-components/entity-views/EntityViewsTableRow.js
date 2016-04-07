import React from 'react';
import { Button } from '../Button';

var EntityViewsTableRow = React.createClass({
	displayName: 'EntityViewsTableRow',
	propTypes: {
		row: React.PropTypes.object.isRequired,
		onEditClick: React.PropTypes.func.isRequired,
		onDeleteClick: React.PropTypes.func.isRequired
	},
	render: function() {
		var { row, onEditClick, onDeleteClick } = this.props;
		return <tr>
			<td>
				<Button type='button'  size='xsmall' onClick={onDeleteClick.bind(null, row)} icon='trash' style='danger' />
				<Button type='button'  size='xsmall' onClick={onEditClick.bind(null, row)} icon='pencil' style='default' />
			</td>
			<td>{row.name}</td>
			<td>{row.masterEntity}</td>
			<td>{row.joinedEntities.map(function(joinedEntity) {
				
			})}</td>
		</tr>
	}
});

export { EntityViewsTableRow };
export default React.createFactory(EntityViewsTableRow);