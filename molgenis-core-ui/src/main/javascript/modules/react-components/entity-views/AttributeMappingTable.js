import React from 'react';
import { Button } from '../Button';
import { EntitySelectBox } from '../EntitySelectBox';

/**
 * TODO 
 * - For every row in the EntityView table with the 'viewToEdit' create 
 */
var AttributeMappingTable = React.createClass({
	displayName: 'AttributeMappingTable',
	propTypes: {
		setTableMode: React.PropTypes.func,
		rowToEdit: React.PropTypes.string,
		addJoinEntity: React.PropTypes.func
	},
	render: function() {
		var { rowToEdit, addJoinEntity } = this.props;
		return <div>
			<Button 
				onClick={this.props.setTableMode.bind(null, 'ENTITY_VIEW_TABLE')} 
				text='Back to Views'
				style='default'
				type='button'
			/>
			<h1>AttributeMapping for {rowToEdit.name}</h1>
			<p>In the table below, you can select Entities you want to join on Master Entity {rowToEdit.masterEntity}</p>
			<table className='table table-bordered'>
				<thead>
					<th>{rowToEdit.masterEntity}</th>
					{rowToEdit.joinedEntities.map(function(joinedEntity){
						<th>joinedEntity.joinEntity</th>
					})}
					<th><span className='pull-right'><Button text='Add a Join Entity' style='success' icon='plus' onClick={function(){}} /></span></th>
				</thead>
				<tbody>
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	}
});

export { AttributeMappingTable };
export default React.createFactory(AttributeMappingTable);