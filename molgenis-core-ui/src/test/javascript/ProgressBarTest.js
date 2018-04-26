import test from "tape";
import React from "react";
import sd from "skin-deep";
import ProgressBar from "react-components/ProgressBar";

test('Test if the ProgressBar component renders a running, failed and success progress bar correctly', assert => {
    // Render the component to test
    const result1 = sd.shallowRender(ProgressBar({
        progressPct: 60,
        progressMessage: 'testing the progress bar running',
        status: 'primary',
        active: true
    }));
    const result2 = sd.shallowRender(ProgressBar({
        progressPct: 20,
        progressMessage: 'testing the progress bar failed',
        status: 'warning',
        active: false
    }));
    const result3 = sd.shallowRender(ProgressBar({
        progressPct: 100,
        progressMessage: 'testing the progress bar suceeded',
        status: 'success',
        active: false
    }));

    // Assert the content of the component is what we expected
    assert.equal(result1.toString(),
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-primary progress-bar-striped active" role="progressbar" style="min-width:2em;width:60%;">testing the progress bar running</div></div>',
        'Progress bar is rendered as an active progressbar');

    assert.equal(result2.toString(),
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-warning" role="progressbar" style="min-width:2em;width:20%;">testing the progress bar failed</div></div>',
        'Progress bar is rendered as a failed progressbar');

    assert.equal(result3.toString(),
        '<div class="progress background-lightgrey"><div class="progress-bar progress-bar-success" role="progressbar" style="min-width:2em;width:100%;">testing the progress bar suceeded</div></div>',
        'Progress bar is rendered as a success progressbar');

    assert.end();
});