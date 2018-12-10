/**
 * The JobsContainer retrieves jobs from the server and feeds it to its
 * child component to render, typically a Jobs component.
 * Shows a spinner while fetching.
 * Shows a notification when a job that was previously running finishes.
 *
 * @module JobsContainer
 *
 * @param url
 *            String representation of the URL where the jobs can be retrieved
 * @param interval
 *            Timeout in milliseconds between retrieval of the jobs, default 1000
 *
 * @exports JobsContainer factory
 */
import React from "react";
import {Spinner} from "../Spinner";
import $ from "jquery";
import NotificationSystem from "react-notification-system";
import SetIntervalMixin from "../mixin/SetIntervalMixin";

var JobsContainer = React.createClass({
    mixins: [SetIntervalMixin],
    displayName: 'JobsContainer',
    propTypes: {
        url: React.PropTypes.string,
        interval: React.PropTypes.number
    },
    _notificationSystem: null,
    getInitialState: function () {
        return {
            jobs: null,
            selectedJobId: null
        }
    },
    componentDidMount: function () {
        this.retrieveJobs();
        this.setInterval(this.retrieveJobs, this.props.interval || 10000);
        this._notificationSystem = this.refs.notificationSystem;
    },
    render: function () {
        return <div>
            <NotificationSystem ref="notificationSystem"/>
            {this.state.jobs === null ? <Spinner/> :
                React.cloneElement(this.props.children, {
                    jobs: this.state.jobs,
                    onSelect: this._onJobSelect,
                    selectedJobId: this.state.selectedJobId
                })}
        </div>
    },
    _onJobSelect: function (selectedJobId) {
        this.setState({selectedJobId: selectedJobId})
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
        $.get(this.props.url, function (data) {
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