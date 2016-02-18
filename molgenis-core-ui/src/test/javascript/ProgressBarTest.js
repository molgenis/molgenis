import test from 'tape';
import React from 'react';
import sd from 'skin-deep'

import ProgressBar from 'react-components/ProgressBar';

test('Test if the ProgressBar component renders what we expect', assert => {
	// Render the component to test
	const tree = sd.shallowRender(ProgressBar({
		progressPct : 100, 
		progressMessage : 'testing the progress bar', 
		status : 'primary', 
		active : true
	}));
	
	// Assert the content of the component is what we expected
	assert.equal(tree.toString(), 
			'<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">testing the progress bar</div></div>',
	'Progress bar is rendered with same attributes as given in the props');
	
	assert.end();		
});
