import React from "react";
import {ProgressBarClass} from './ProgressBar';
import DeepPureRenderMixin from './mixin/DeepPureRenderMixin'; 

var Job = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'Job',
	propTypes: {
		job: React.PropTypes.object
	},
	render: function() {
		let pbProps = {
				progressMessage : 'Jobby there',
				status : 'primary',
				active : true
			};
		return <ProgressBarClass {...pbProps} progressPct={80} />
	}
});
	
export default React.createFactory(Job);