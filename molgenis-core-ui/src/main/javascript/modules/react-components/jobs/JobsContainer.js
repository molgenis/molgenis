/**
 * The JobsContainer retrieves jobs from the server and renders them using the
 * Jobs component. Shows a spinner while fetching.
 *
 * @module JobsContainer
 *
 * @param username
 *            The username of the one viewing the jobs. Jobs can be filtered
 *            based on this username
 *
 * @exports JobsContainer factory
 */
import React from 'react';
import RestClient from "rest-client/RestClientV2";
import { Spinner } from '../Spinner'
import $ from 'jquery';
import NotificationSystem from 'react-notification-system';
import { Jobs } from './Jobs';

import SetIntervalMixin from '../mixin/SetIntervalMixin';

var JobsContainer = React.createClass({
    mixins: [SetIntervalMixin],
    displayName: 'JobsContainer',
    propTypes: {
        username: React.PropTypes.string
    },
    _notificationSystem: null,
    getInitialState: function () {
        return {
            jobs: null
        }
    },
    componentDidMount: function () {
        this.retrieveJobs();
        this.setInterval(this.retrieveJobs, 1000);
        this._notificationSystem = this.refs.notificationSystem;
    },
    render: function () {
        return <div>
            <NotificationSystem ref="notificationSystem"/>
            {this.state.jobs === null ? <Spinner/> :
                <Jobs jobs={this.state.jobs}/>}
        </div>
    },
    _getNewlyFinishedJobs: function (jobs) {
        if (!this.state.jobs) {
            return [];
        }
        const runningJobIDs = this.state.jobs.filter((job) => job.status === 'RUNNING').map((job) => job.identifier);
        return jobs.filter((newJob) => newJob.status !== 'RUNNING' && runningJobIDs.indexOf(newJob.identifier) >= 0);
    },
    retrieveJobs: function () {
        var self = this;
        $.get('/plugin/jobs/latest', function (data) {
            self._getNewlyFinishedJobs(data).forEach((job) => {
                const notification = {
                    title: 'Job finished',
                    message: job.progressMessage,
                    level: job.status === 'SUCCESS' ? 'success' : 'warning',
                    position: 'tr',
                    action: job.resultUrl && {
                        label: 'Go to result',
                        callback: () => {
                            window.location = job.resultUrl
                        }
                    }
                };
                self._notificationSystem.addNotification(notification);
            })
            self.setState({jobs: data})
        });
    }
});

export default React.createFactory(JobsContainer);