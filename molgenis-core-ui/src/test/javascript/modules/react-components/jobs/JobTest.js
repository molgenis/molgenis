import test from 'tape';
import React from 'react';
import sd from 'skin-deep';

import {job_success, job_failed, job_running} from './jobs'

import Job from 'react-components/jobs/Job';

test('Test if the Job component renders a succeeded job correctly', assert => {
    const tree = sd.shallowRender(Job({
        job: job_success
    }));

    assert.equals(tree.toString(),
        '<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-success" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div><div class="btn-group" role="group"></div></p></div>',
        'Job component rendered a succeeded job correctly');

    assert.end();
});

test('Test if the Job component renders a running job correctly', assert => {
    const tree = sd.shallowRender(Job({
        job: job_running
    }));

    assert.equals(tree.toString(),
        '<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div><div class="btn-group" role="group"></div></p></div>',
        'Job component rendered a running job correctly');

    assert.end();
});

test('Test if the Job component renders a failed job correctly', assert => {
    const tree = sd.shallowRender(Job({
        job: job_failed
    }));

    assert.equals(tree.toString(),
        '<div><p>TEST job job<div class="progress background-lightgrey"><div class="progress-bar progress-bar-danger" role="progressbar" style="min-width:2em;width:100%;">Test (started by Tape)</div></div><div class="btn-group" role="group"></div></p></div>',
        'Job component rendered a failed job correctly');

    assert.end();
});