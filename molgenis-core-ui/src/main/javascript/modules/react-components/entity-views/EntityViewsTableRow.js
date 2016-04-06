import React from 'react';
import { Button } from '../Button';

var EntityViewsTableRow = React.createClass({
	displayName: 'EntityViewsTableRow',
	propTypes: {
		row: React.PropTypes.object.isRequired,
		onEditClick: React.PropTypes.func.isRequired,
		onDeleteClick: React.PropTypes.func.isRequired,
	},
	render: function() {
		var {row, onDeleteClick, onEditClick} = this.props;
		return <tr>
			<td>
				<Button type='button'  size='xsmall' onClick={onDeleteClick(row)} icon='trash' style='danger' />
				<Button type='button'  size='xsmall' onClick={onEditClick(row)} icon='pencil' style='default' />
			</td>
			<td>{row.viewName}</td>
			<td>{row.masterEntityName}</td>
			<td>{row.joinedEntityNames}</td>
		</tr>
	}
});

export { EntityViewsTableRow };
export default React.createFactory(EntityViewsTableRow);