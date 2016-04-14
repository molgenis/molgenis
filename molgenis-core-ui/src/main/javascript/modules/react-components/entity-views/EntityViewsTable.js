import React from 'react';
import { EntityViewsTableRow } from './EntityViewsTableRow';

var EntityViewsTable = React.createClass({
	displayName: 'EntityViewsTable',
	propTypes: {
		views: React.PropTypes.array,
		deleteEntityView: React.PropTypes.func
	},
	render: function() {
		var { deleteEntityView } = this.props;
		return <div>
			{this.props.views.length > 0 ? 
				<table className='table table-bordered'>
					<thead>
						<th></th>
						<th>View name</th>
						<th>Master entity name</th>
						<th>Joined entities</th>
					</thead>
					<tbody>
					{this.props.views.map(function(row, index){	
						return <EntityViewsTableRow	row={row} deleteEntityView={deleteEntityView} key={index} />;
					})}
					</tbody>
				</table> 
			: null}
		</div>
	}
});

export { EntityViewsTable };
export default React.createFactory(EntityViewsTable);