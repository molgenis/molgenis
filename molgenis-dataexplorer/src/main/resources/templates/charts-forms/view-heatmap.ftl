<div id="chart-designer-modal-heatmap-container">
    <div id="chart-designer-modal-heatmap" class="modal hide" tabindex="-1" role="dialog">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h3>Create Heat map</h3>
        </div>
        <div class="modal-body">
            <div id="chart-designer-modal-heatmap-form" class="col-md-12">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="heatmap-title">Title</label>
                        <div class="col-md-9">
                            <input type="text" id="heatmap-title" placeholder="title">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="heatmap-select-xaxis-feature">X axis</label>
                        <div class="col-md-9">
                            <select id="heatmap-select-xaxis-feature" data-placeholder="ObservableValue"
                                    name="heatmap-select-xaxis-feature"></select>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <div class="form-group">
                <div class="col-md-9">
                    <input id="heatmap-designer-modal-create-button" class="btn btn-default" type="button"
                           value="Create Chart" data-dismiss="modal" aria-hidden="true">
                </div>
            </div>
        </div>
    </div>
</div>