<div id="chart-designer-modal-boxplot-container">
    <div id="chart-designer-modal-boxplot" class="modal" tabindex="-1" role="dialog"
         aria-labelledby="chart-designer-modal-boxplot-label" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span
                            aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title" id="chart-designer-modal-boxplot-label">Create Box Plot</h4>
                </div>
                <div class="modal-body">
                    <div id="chart-designer-modal-boxplot-form">
                        <form class="form-horizontal">
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="boxplot-title">Title</label>
                                <div class="col-md-9">
                                    <input type="text" class="form-control" id="boxplot-title" placeholder="title">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="boxplot-select-feature">Select
                                    feature</label>
                                <div class="col-md-9">
                                    <select id="boxplot-select-feature" class="form-control"
                                            data-placeholder="ObservableValue" name="chart-select-feature"></select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="boxplot-scale">Outliers scale</label>
                                <div class="col-md-9">
                                    <input type="number" class="form-control" min="0" max="100" step="0.1"
                                           id="boxplot-scale" placeholder="1.5">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="boxplot-select-split-feature">Split to
                                    series</label>
                                <div class="col-md-9">
                                    <select id="boxplot-select-split-feature" class="form-control"
                                            data-placeholder="ObservableValue"
                                            name="boxplot-select-split-feature"></select>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <div class="col-md-12">
                            <input id="boxplot-designer-modal-create-button" class="btn btn-default" type="button"
                                   value="Create Chart" data-dismiss="modal" aria-hidden="true">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>