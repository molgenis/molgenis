import React from "react";
import {Button} from "./Button";
import {Input} from "./Input";
import {RadioGroup} from "./RadioGroup";

var UploadForm = React.createClass({
	displayName: 'UploadForm',
	propTypes: {
		width: React.PropTypes.oneOf(['1','2','3','4','5','6','7','8','9','10','11','12']),
		showAction: React.PropTypes.bool,
		onSubmit: React.PropTypes.func.isRequired,
		validExtensions: React.PropTypes.array
	},
	getInitialState: function() {
		return {
			file: null,
			showNameField: false,
			fileName: null,
			action: 'ADD'
		}
	},
	getDefaultProps: function() {
		return {
			width: '6',
			showAction: false
		};
	},
	render: function() {
		var gridWidth = this.props.width ? 'col-md-' + this.props.width : 'col-md-12';
		var actions = [{value: 'ADD', label: 'ADD'}, {value:'ADD_UPDATE_EXISTING',label:'ADD/UPDATE'}, {value:'UPDATE',label:'UPDATE'}];
		return <form>
			<div className={gridWidth}>
				<div className='form-group'>
					<input type="file" onChange={this._setFile} />
					{this.state.warning && <span id="helpBlock" class="help-block">{this.state.warning}</span>}
				</div>
					
				{this.state.showNameField &&	
				<div className='form-group'>	
					<label htmlFor='file-name-input-field'>Name</label>
					<Input id='file-name-input-field' type='string' onValueChange={this._setFileName} required={true} value={this.state.fileName}/>
				</div>
				}
				{this.props.showAction && <div className='form-group'>
					<label htmlFor='action-field'>Action</label>
					<RadioGroup name='action-field' layout='vertical' options={actions} type='radio' onValueChange={this._setAction} required={false} value={this.state.action} />
				</div>}

				{this.state.file && <div className='form-group'>
					<Button id='upload-file-btn' type='submit' style='default' size='medium' onClick={this._onSubmit} text='Upload'
							disabled={this.state.showNameField && !this.state.fileName}/>
				</div>}
			</div>
		</form>
	},
	_setFile: function(event) {
		const file = event.target.files[0];
		var fileName = file.name.toLowerCase();
		var showNameField = fileName.endsWith('.vcf') || fileName.endsWith('.vcf.gz');
		if( this.props.validExtensions && !this.props.validExtensions.find((extension) => fileName.endsWith(extension))){
			this.setState({warning: 'Invalid file name, extension must be '+this.props.validExtensions})
		} else {
			if (showNameField) {
				// Remove extension
				fileName = fileName.replace(/\.vcf|\.vcf\.gz/, '');
				// Maximum length is 30 chars, but we need to take into account that the samples are post fixed "_SAMPLES"
				fileName = fileName.substring(0, 21);
				// Remove illegal chars
				fileName = fileName.replace(/\-|\.|\*|\$|\&|\%|\^|\(|\)|\#|\!|\@|\?/g, '_');
				// Don't allow entitynames starting with a number
				fileName = fileName.replace(/^[0-9]/g, '_');
				this.setState({fileName});
			}
			this.setState({file, showNameField, warning: undefined});
		}
	},
	_setFileName: function(fileName) {
		this.setState({fileName: fileName.value});
	},
	_setAction: function(action) {
		this.setState({action : action.value});
	},
	_onSubmit: function(event) {
		event.preventDefault();
		this.props.onSubmit(this.state);
	}
});

export { UploadForm };
export default React.createFactory(UploadForm);