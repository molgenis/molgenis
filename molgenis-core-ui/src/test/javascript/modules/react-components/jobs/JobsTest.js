import test from "tape";
import React from "react";
import {jobs} from "./jobs";
import Jobs from "react-components/jobs/Jobs";
import sd from "skin-deep";

test('Test if the Jobs component renders running, success and failed jobs correctly', assert => {
    const tree = sd.shallowRender(Jobs({
        jobs: jobs
    }));

    assert.equals(tree.toString(),
        '<div><div class="row"><div class="col-md-12">' +
        '<div class="panel panel-primary">' +
        '<div class="panel-heading">Running Jobs</div>' +
        '<div class="panel-body"><div>' +
        '<p>TEST job job<div>Test (started by Tape)' +
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:20%;">100/500</div></div></div>' +
        '<div class="btn-group" role="group">' +
        '<button type="button" class="btn btn-default">Show details</button>' +
        '</div>' +
        '</p></div></div></div></div></div>' +
        '<div class="row"><div class="col-md-12"><div class="panel panel-primary">' +
        '<div class="panel-heading">Finished Jobs</div>' +
        '<div class="panel-body">' +
        '<table class="table table-striped">' +
        '<thead><th></th><th>Status</th><th>When</th><th>Duration</th><th>Type</th><th>Message</th><th>Result</th></thead>' +
        '<tbody>' +
        '<tr><td><button class="btn btn-xs btn-info"><span class="glyphicon glyphicon-search" aria-hidden="true"></span></button></td>' +
        '<td>SUCCESS</td><td>Jan 1, 2100, 12:15 - 12:30 AM (in 84 years)</td><td>15m, 0s</td><td>TEST job</td><td>Test (started by Tape)</td><td></td></tr>' +
        '<tr><td><button class="btn btn-xs btn-info"><span class="glyphicon glyphicon-search" aria-hidden="true"></span></button></td>' +
        '<td>FAILED</td><td>Jan 1, 2300, 12:15 - 12:30 AM (in 284 years)</td><td>15m, 0s</td><td>TEST job</td><td>Test (started by Tape)</td><td></td></tr>' +
        '</tbody></table></div></div></div></div></div>',
        'Jobs component rendered three types of jobs correctly');

    assert.end();
});