import React from "react";
import {UploadForm} from "./UploadForm";
import $ from "jquery";
import {JobContainer} from "./jobs/JobContainer";

var UploadContainer = React.createClass({
    displayName: 'UploadContainer',
    propTypes: {
        url: React.PropTypes.string.isRequired,
        width: React.PropTypes.oneOf(['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']),
        onSubmit: React.PropTypes.func,
        onCompletion: React.PropTypes.func,
        validExtensions: React.PropTypes.array,
        showNameFieldExtensions: React.PropTypes.array,
        maxFileSizeMB: React.PropTypes.number
    },
    getDefaultProps: function () {
        return {
            showNameFieldExtensions: ['.vcf', '.vcf.gz'],
            maxFileSizeMB: 150
        }
    },
    getInitialState: function () {
        return {
            job: null
        }
    },
    _onSubmit: function (form) {
        var data = new FormData();
        data.append('file', form.file);
        data.append('entityTypeId', form.fileName);
        data.append('action', form.action);
        data.append('notify', false);

        const submission = $.ajax({
            url: this.props.url,
            type: 'POST',
            data: data,
            cache: false,
            dataType: 'json',
            // Don't process the files
            processData: false,
            // Set content type to false as jQuery will tell the server its a
            // query string request
            contentType: false,
            success: (data) => this.setState({job: data})
        });
        if (this.props.onSubmit) {
            submission.always(this.props.onSubmit)
        }
    },
    render: function () {
        return <div>
            {this.state.job ? <JobContainer
                jobHref={this.state.job}
                onCompletion={this._onCompletion}
            /> : <UploadForm
                width={this.props.width}
                onSubmit={this._onSubmit}
                validExtensions={this.props.validExtensions}
                showNameFieldExtensions={this.props.showNameFieldExtensions}
                maxFileSizeMB={this.props.maxFileSizeMB}
            />}
        </div>
    },
    _onCompletion: function (job) {
        if (this.props.onCompletion) {
            this.props.onCompletion(job)
        }
        this.setState({job: null});
    }
});

export {UploadContainer};
export default React.createFactory(UploadContainer);