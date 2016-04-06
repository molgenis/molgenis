import React from 'react';

import { Spinner } from '../Spinner'
import { EntityViewsTable } from './EntityViewsTable'

var EntityViewContent = React.createClass({
	displayName: 'EntityViewContent',
	propTypes: {
		tableContent: React.PropTypes.array,
		entityEditFunction: React.PropTypes.func,
		entityDeleteFunction: React.PropTypes.func
	},
	render: function() {
		return <div>
			{this.props.tableContent === null ? <Spinner /> : 
				<EntityViewsTable 
					tableContent={this.props.tableContent} 
					entityEditFunction={this.props.entityEditFunction} 
					entityDeleteFunction={this.props.entityDeleteFunction} 
				/>
			}
		</div> 
	}
});

export { EntityViewContent };
export default React.createFactory(EntityViewContent);