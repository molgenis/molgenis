<div id="chart-designer-modal-scatterplot-container">
    <div id="chart-designer-modal-scatterplot" class="modal" tabindex="-1"
         aria-labelledby="chart-designer-modal-scatterplot-label" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span
                            aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title" id="chart-designer-modal-scatterplot-label">Create Scatter Plot</h4>
                </div>
                <div class="modal-body">
                    <div id="chart-designer-modal-scatterplot-form">
                        <form class="form-horizontal">
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="scatterplot-title">Title</label>
                                <div class="col-md-9">
                                    <input type="text" class="form-control" id="scatterplot-title" placeholder="title">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="scatterplot-select-yaxis-feature">Y
                                    axis</label>
                                <div class="col-md-9">
                                    <select id="scatterplot-select-yaxis-feature" class="form-control"
                                            data-placeholder="ObservableValue"
                                            name="scatterplot-select-yaxis-feature"></select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="scatterplot-select-xaxis-feature">X
                                    axis</label>
                                <div class="col-md-9">
                                    <select id="scatterplot-select-xaxis-feature" class="form-control"
                                            data-placeholder="ObservableValue"
                                            name="chart-select-xaxis-feature"></select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-3 control-label" for="scatterplot-select-split-feature">Split to
                                    series</label>
                                <div class="col-md-9">
                                    <select id="scatterplot-select-split-feature" class="form-control"
                                            data-placeholder="ObservableValue"
                                            name="scatterplot-select-split-feature"></select>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <div class="col-md-12">
                            <input id="scatterplot-designer-modal-create-button" class="btn btn-default pull right"
                                   type="button" value="Create Chart" data-dismiss="modal" aria-hidden="true">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>