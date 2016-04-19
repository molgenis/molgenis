/**
 * The JobContainer retrieves progress for a single Job execution and renders it
 * using the Job component.
 *
 * @module JobContainer
 *
 * @exports JobContainer factory
 */
import React from 'react';
import RestClient from "rest-client/RestClientV2";
import { Spinner } from '../Spinner'
import $ from 'jquery';

import { Job } from './Job';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin';
import SetIntervalMixin from '../mixin/SetIntervalMixin';

var JobContainer = React.createClass({
    mixins: [DeepPureRenderMixin, SetIntervalMixin],
    displayName: 'JobContainer',
    propTypes: {
        jobHref: React.PropTypes.string
    },
    getInitialState: function() {
        return {
            job : null
        }
    },
    componentDidMount: function() {
        this.retrieveJob();
        this.setInterval(this.retrieveJob, 1000);
    },
    render: function() {
        if(this.state.job) {
            return <Job job={this.state.job}/>
        } else {
            return <Spinner />;
        }
    },
    retrieveJob: function() {
        var self = this;
        $.get(this.props.jobHref, function(job) {
            self.setState({job: job})
        });
    }
});

export default React.createFactory(JobContainer);