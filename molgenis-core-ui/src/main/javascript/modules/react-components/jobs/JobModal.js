/**
 * JobModal shows job details
 *
 * @module Job
 *
 * @param job
 *            A key value object containing all the details of a job including
 *            its status and progress message plus details specific to this type
 *            of job.
 *
 * @exports JobModal class
 */

import React from "react";
import { ProgressBar } from '../ProgressBar';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin';
import {JobDetails} from './JobDetails'

var JobModal = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'JobModal',
    propTypes: {
        job: React.PropTypes.object.isRequired,
        onClose: React.PropTypes.func.isRequired
    },
    componentDidMount(){
        $(this.getDOMNode()).modal('show');
        $(this.getDOMNode()).on('hidden.bs.modal', this.props.onClose);
    },
    render: function () {
        const {job} = this.props;
        return <div className="modal fade" tabIndex="-1" role="dialog">
                <div className="modal-dialog  modal-lg">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h4 className="modal-title">Job details</h4>
                        </div>
                        <div className="modal-body">
                            <JobDetails job={job}/>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-primary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
    }
});

export { JobModal };
export default React.createFactory(JobModal);