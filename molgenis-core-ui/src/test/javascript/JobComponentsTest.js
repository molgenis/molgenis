import test from 'tape';
import React from 'react';
import sd from 'skin-deep';

import JobContainer from 'react-components/jobs/JobContainer';
import Jobs from 'react-components/jobs/Jobs';
import RunningJobs from 'react-components/jobs/RunningJobs';
import JobTable from 'react-components/jobs/JobTable';
import Job from 'react-components/jobs/Job';

const job_success = {
	identifier: "test_1",
	status: "SUCCESS",
	type: "TEST job",
	entityName: "TapeTest",
	progessMax: 1000,
	progressInt: 1000,
	progressMessage: "Test (started by Tape)",
	submissionDate: "2100-01-01T00:00:00+0100",
	startDate: "2100-01-01T00:15:00+0100",
	endDate: "2100-01-01T00:30:00+0100",
};

const job_running = {
	identifier: "test_2",
	status: "RUNNING",
	type: "TEST job",
	entityName: "TapeTest",
	progessMax: 500,
	progressInt: 100,
	progressMessage: "Test (started by Tape)",
	submissionDate: "2200-01-01T00:00:00+0100",
	startDate: "2200-01-01T00:15:00+0100",
	endDate: "2200-01-01T00:30:00+0100",
}

const job_failed = {
	identifier: "test_3",
	status: "FAILED",
	type: "TEST job",
	entityName: "TapeTest",
	progessMax: 200,
	progressInt: 50,
	progressMessage: "Test (started by Tape)",
	submissionDate: "2300-01-01T00:00:00+0100",
	startDate: "2300-01-01T00:15:00+0100",
	endDate: "2300-01-01T00:30:00+0100",
}

const jobs = [job_success, job_running, job_failed];

// TODO Create a test for the JobContainer. Requires proxyquire and sinon due to rest-client requests

test('Test if the Jobs component renders running, success and failed jobs correctly', assert => {
	const tree = sd.shallowRender(Jobs({
		jobs: jobs
	}));
	
	assert.equals(tree.toString(), 
	'<div class="row"><div class="col-md-5"><div class="panel panel-primary"><div class="panel-heading">Running Jobs</div><div class="panel-body"><div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div></div></div></div><div class="col-md-7"><div class="panel panel-primary"><div class="panel-heading">Finished Jobs</div><div class="panel-body"><table class="table table-striped"><thead><th>Status</th><th>Runtime</th><th>Type</th><th>Message</th><th>Started</th><th>Finished</th><th>Link to entity</th></thead><tbody><tr><td>SUCCESS</td><td>15 minutes</td><td>TEST job</td><td>Test (started by Tape)</td><td>Fri, Jan 1, 2100 12:15 AM</td><td>in 84 years</td><td>URL to TapeTest</td></tr><tr><td>FAILED</td><td>15 minutes</td><td>TEST job</td><td>Test (started by Tape)</td><td>Mon, Jan 1, 2300 12:15 AM</td><td>in 284 years</td><td>URL to TapeTest</td></tr></tbody></table></div></div></div></div>', 
	'Jobs component rendered three types of jobs correctly');
	
	assert.end();
});

test('Test if the RunningJobs component renders job progressbars', assert => {
	const tree = sd.shallowRender(RunningJobs({
		jobs: jobs
	}));
	
	assert.equals(tree.toString(), 
	'<div class="panel panel-primary"><div class="panel-heading">Running Jobs</div><div class="panel-body"><div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-success" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div><div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div><div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-danger" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div></div></div>', 
	'RunningJobs component rendered progressbars for each job correctly');
	
	assert.end();
});

test('Test if the JobTable renders a table with job information', assert => {
	const tree = sd.shallowRender(JobTable({
		jobs: jobs
	}));
	
	assert.equals(tree.toString(), 
	'<div class="panel panel-primary"><div class="panel-heading">Finished Jobs</div><div class="panel-body"><table class="table table-striped"><thead><th>Status</th><th>Runtime</th><th>Type</th><th>Message</th><th>Started</th><th>Finished</th><th>Link to entity</th></thead><tbody><tr><td>SUCCESS</td><td>15 minutes</td><td>TEST job</td><td>Test (started by Tape)</td><td>Fri, Jan 1, 2100 12:15 AM</td><td>in 84 years</td><td>URL to TapeTest</td></tr><tr><td>RUNNING</td><td>15 minutes</td><td>TEST job</td><td>Test (started by Tape)</td><td>Wed, Jan 1, 2200 12:15 AM</td><td>in 184 years</td><td>URL to TapeTest</td></tr><tr><td>FAILED</td><td>15 minutes</td><td>TEST job</td><td>Test (started by Tape)</td><td>Mon, Jan 1, 2300 12:15 AM</td><td>in 284 years</td><td>URL to TapeTest</td></tr></tbody></table></div></div>', 
	'JobTable component rendered a table with job information correctly');
	
	assert.end();
});

test('Test if the Job component renders a succeeded job correctly', assert => {
	const tree = sd.shallowRender(Job({
		job: job_success
	}));
	
	assert.equals(tree.toString(), 
	'<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-success" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div>', 
	'Job component rendered a succeeded job correctly');
	
	assert.end();
});

test('Test if the Job component renders a running job correctly', assert => {
	const tree = sd.shallowRender(Job({
		job: job_running
	}));
	
	assert.equals(tree.toString(), 
	'<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div>', 
	'Job component rendered a running job correctly');
	
	assert.end();
});

test('Test if the Job component renders a failed job correctly', assert => {
	const tree = sd.shallowRender(Job({
		job: job_failed
	}));
	
	assert.equals(tree.toString(), 
	'<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-danger" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div></p></div>', 
	'Job component rendered a failed job correctly');
	
	assert.end();
});