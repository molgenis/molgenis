import test from "tape";
import React from "react";
import sd from "skin-deep";
import {jobs} from "./jobs";
import RunningJobs from "react-components/jobs/RunningJobs";

test('Test if the RunningJobs component renders job progressbars correctly', assert => {
    const tree = sd.shallowRender(RunningJobs({
        jobs: jobs
    }));

    assert.equals(tree.toString(),
        '<div class="panel panel-primary"><div class="panel-heading">Running Jobs</div>' +
        '<div class="panel-body">' +
        '<div><p>TEST job job<div>Test (started by Tape)' +
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-success" role="progressbar" style="min-width:2em;width:100%;">1000/1000</div></div>' +
        '</div><div class="btn-group" role="group"><button type="button" class="btn btn-default">Show details</button></div></p></div>' +
        '<div><p>TEST job job<div>Test (started by Tape)' +
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:20%;">100/500</div></div></div>' +
        '<div class="btn-group" role="group"><button type="button" class="btn btn-default">Show details</button></div></p></div>' +
        '<div><p>TEST job job<div>Test (started by Tape)' +
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-danger" role="progressbar" style="min-width:2em;width:25%;">50/200</div></div></div>' +
        '<div class="btn-group" role="group"><button type="button" class="btn btn-default">Show details</button></div></p></div></div></div>',
        'RunningJobs component rendered progressbars for each job correctly');

    assert.end();
});