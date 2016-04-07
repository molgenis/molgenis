import React from 'react';

import { Spinner } from '../Spinner';
import { EntityViewsTable } from './EntityViewsTable';
import { AttributeMappingTable } from './AttributeMappingTable';

var EntityViewContent = React.createClass({
	displayName: 'EntityViewContent',
	propTypes: {
		tableContent: React.PropTypes.object,
		entityEditFunction: React.PropTypes.func,
		entityDeleteFunction: React.PropTypes.func,
		tableMode: React.PropTypes.oneOf(['ENTITY_VIEW_TABLE', 'ATTRIBUTE_MAPPING_TABLE']),
		setTableMode: React.PropTypes.func,
		rowToEdit: React.PropTypes.object,
	},
	render: function() {
		return <div>
			{this.props.tableContent === null ? <Spinner /> : 
				this.props.tableMode === 'ENTITY_VIEW_TABLE' ? 
				<EntityViewsTable 
					tableContent={this.props.tableContent} 
					entityEditFunction={this.props.entityEditFunction} 
					entityDeleteFunction={this.props.entityDeleteFunction} 
				/> :
				<AttributeMappingTable
					setTableMode={this.props.setTableMode} 
					rowToEdit={this.props.rowToEdit}
				/>
			}
		</div> 
	}
});

export { EntityViewContent };
export default React.createFactory(EntityViewContent);