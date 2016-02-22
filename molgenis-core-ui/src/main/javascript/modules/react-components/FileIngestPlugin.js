import { Table } from './Table'
import React, { Component } from 'react'

class FileIngestPlugin extends Component {
	constructor(props) {
        super(props)
        this.state = {selectedFileIngest: null}
        this.onFileIngestSelect = this.onFileIngestSelect.bind(this)
    }
	
    render() {
    	let fileIngest = this.state.selectedFileIngest
		
    	return <div>
    		<legend>Automatic imports</legend>
    		<div className='ingest-table'>
    			<Table entity='FileIngest' 
    					attrs={{name: null, url: null, entityMetaData: null, cronexpression: null, active: null}} 
    					onRowClick={this.onFileIngestSelect}/>
    		</div>
    		{fileIngest === null ? '' :
    			<div>
    				<legend>
    					Import jobs for '{fileIngest.name}' 
    					<span> (Target: <a href={'/menu/main/dataexplorer?entity=' + fileIngest.entityMetaData} >{fileIngest.entityMetaData}</a>)</span>
    				</legend>
    				<div className='ingest-table'>
    					<Table entity='FileIngestJobMetaData' 
    							enableAdd={false}
    							sort={{attr: {name: 'startDate'}, order: 'desc', path: []}}
    							attrs={{status:null, startDate: null, endDate: null, progressMessage: null, file: null}}
    							query={{q: [{field: 'fileIngest', operator: 'EQUALS', value: fileIngest.id}]}} />
    				</div>
    			</div>}
    	</div>
    }
    
    onFileIngestSelect(fileIngest) {
    	this.setState({selectedFileIngest: fileIngest})
    }
 
}

export default React.createFactory(FileIngestPlugin)