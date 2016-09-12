/**
 * JobDetails shows job details
 *
 * @module Job
 *
 * @param job
 *            A key value object containing all the details of a job including
 *            its status and progress message plus details specific to this type
 *            of job.
 *
 * @exports JobDetails class
 */

import React from "react";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";
import {Ace} from "../wrapper/Ace";

var JobDetails = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'JobDetails',
    propTypes: {
        job: React.PropTypes.object.isRequired,
    },
    render: function () {
        const {job} = this.props;
        return <div>
            <form className="form-horizontal">
                {Object.keys(job).map(key =>
                    <div className="form-group">
                        <label className="col-sm-2 control-label">{key}</label>
                        <div className="col-sm-10">
                            {key === 'log' ? <Ace readOnly
                                                  value={job.log}
                                                  mode='text'
                                                  tail/> :
                                <p className="form-control-static">{
                                    typeof job[key] === 'object' ?
                                        job[key]['__labelValue'] :
                                        job[key]}</p>}
                        </div>
                    </div>)}
            </form>
        </div>
    }
});

export {JobDetails};
export default React.createFactory(JobDetails);