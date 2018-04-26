import {Table} from "./Table";
import React, {Component} from "react";
import $ from "jquery";
import {packageSeparator} from "rest-client";

class ScheduledJobsPlugin extends Component {
    constructor(props) {
        super(props)
        this.state = {selectedScheduledJob: null}
        this.refresh = this.refresh.bind(this)
        this.onScheduledJobSelect = this.onScheduledJobSelect.bind(this)
        this.onExecute = this.onExecute.bind(this)
        this.onScheduledJobDelete = this.onScheduledJobDelete.bind(this)
    }

    render() {
        let scheduledJob = this.state.selectedScheduledJob
        /*
         N.B. We lose access to jobExecutionType if the user collapses the type attribute.
         Table component doesn't allow us to control the collapse state.
         Therefore we hide the collapse button in scheduled-jobs.css.
         See also #6164
         */
        return <div>
            <legend>Scheduled Jobs</legend>
            <div className='scheduled-jobs-table'>
                <Table entity={'sys' + packageSeparator + 'job' + packageSeparator + 'ScheduledJob'}
                       attrs={{
                           name: null,
                           description: null,
                           cronExpression: null,
                           active: null,
                           parameters: null,
                           user: null,
                           type: {jobExecutionType: null, label: null}
                       }}
                       defaultSelectFirstRow={true}
                       selectedRow={this.state.selectedScheduledJob}
                       onRowClick={this.onScheduledJobSelect}
                       enableExecute={true}
                       onExecute={this.onExecute}
                       onRowDelete={this.onScheduledJobDelete}/>
            </div>
            {scheduledJob === null ? '' :
                <div>
                    <legend>
                        '{scheduledJob.name}' executions
                    </legend>
                    <div className='jobexecutions-table'>
                        <Table entity={scheduledJob.type.jobExecutionType.id}
                               enableAdd={false}
                               sort={{attr: {name: 'startDate'}, order: 'desc', path: []}}
                               query={{q: [{field: 'scheduledJobId', operator: 'EQUALS', value: scheduledJob.id}]}}/>
                    </div>
                </div>}
        </div>
    }

    componentDidMount() {
        setInterval(this.refresh, 20000)
    }

    refresh() {
        this.setState(this.state)
    }

    onScheduledJobSelect(scheduledJob) {
        this.setState({selectedScheduledJob: scheduledJob})
    }

    onExecute(scheduledJob) {
        $.post('/plugin/jobs/run/' + scheduledJob.id).done(() => {
            window.molgenis.createAlert([{message: 'New job scheduled'}], 'success')
            setTimeout(this.refresh, 2000)
        })
    }

    onScheduledJobDelete() {
        window.molgenis.createAlert([{message: 'Job deleted'}], 'success')
        this.setState({"selectedScheduledJob": null});
        this.refresh()
    }
}

export default React.createFactory(ScheduledJobsPlugin)