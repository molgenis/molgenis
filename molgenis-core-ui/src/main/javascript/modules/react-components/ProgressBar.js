import React from 'react';
import DeepPureRenderMixin from './mixin/DeepPureRenderMixin'; 

var div = React.DOM.div;

var ProgressBarClass = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'ProgressBar',
	propTypes: {
		progressPct: React.PropTypes.number.isRequired,
		progressMessage: React.PropTypes.string.isRequired,
		status: React.PropTypes.oneOf(['info', 'danger', 'success', 'warning', 'primary']).isRequired,
		active: React.PropTypes.bool
	},
	render: function() {
		return <div className="progress">
			<div className="progress-bar progress-bar-{this.props.status} {this.props.active ? 'active' : ''}" 
				role="progressbar" 
					style={{minWidth: '2em', 'width': this.props.progressPct+ '%'}}>
			    		{this.props.progressMessage}
			</div>
		</div>
	}
});

export { ProgressBarClass };

export default React.createFactory(ProgressBarClass);