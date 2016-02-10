import test from 'tape';
import React from 'react';
import sd from 'skin-deep'

import ProgressBar from 'react-components/ProgressBar';

test('Testing ProgressBar component', (t) => {
	t.test('Rendering a progress bar', (t) => {
		const progressBar = {
			progressPct : 100, 
			progressMessage : 'testing the progress bar', 
			status : 'primary', 
			active : true
		};
		
		const result = sd.shallowRender(ProgressBar(progressBar));
		t.test('Assert the progress bar equals what we expect', (t) => {
			 t.plan(1);
			 t.equal(result.toString(), 
			 '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">testing the progress bar</div></div>',
			 'Progress bar is rendered with same attributes as given in the props');
		});	
	});
});