import React from 'react';
import { EntityViewsTableRow } from './EntityViewsTableRow';

var EntityViewsTable = React.createClass({
	displayName: 'EntityViewsTable',
	propTypes: {
		tableContent: React.PropTypes.object,
		entityEditFunction: React.PropTypes.func,
		entityDeleteFunction: React.PropTypes.func
	},
	getDefaultProps: function() {
		return {
			onEntityEdit: function(){},
			onEntityDelete: function(){}
		}
	},
	render: function() {
		var self = this;
		return <table className='table table-bordered'>
			<thead>
				<th></th>
				<th>View name</th>
				<th>Master entity name</th>
				<th>Joined entities</th>
			</thead>
			<tbody>
			{self.props.tableContent.items.map(function(row){	
				return <EntityViewsTableRow 
					row={row} 
					key={row.identifier} 
					onEditClick={self.props.entityEditFunction} 
					onDeleteClick={self.props.entityDeleteFunction} 
				/>;
			})}
			</tbody>
		</table>
		
	}
});

export { EntityViewsTable };
export default React.createFactory(EntityViewsTable);