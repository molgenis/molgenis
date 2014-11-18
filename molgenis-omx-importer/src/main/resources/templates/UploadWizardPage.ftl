<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="" role="form">
    <div style="padding-top: 25px">
    <div>
    	Upload <a href="/html/fileformat.html" target="_blank">Observ-OMX</a> data file<br /><br />
    	<input type="file" name="upload" style="height:25px" >
    </div>
    <div class="row">
        <div class="col-md-8">
            <div class="panel">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-target="#upload-options-collapse" href="#upload-options-collapse">Advanced options</a>
                    </h4>
                </div>
                <div id="upload-options-collapse" class="panel-collapse collapse">
                    <div class="panel-body">
                        <table style="width: 75%;">
                            <tr>
                                <td>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="entity_option" value="add_update">Add entities / update existing
                                        </label>
                                    </div>
                                </td>
                                <td>Importer adds new entities or updates existing entities</td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="entity_option" value="add" checked>Add entities
                                        </label>
                                    </div>
                                </td>
                                <td>Importer adds new entities or fails if entity exists</td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="entity_option" value="add_ignore">Add entities / ignore existing
                                        </label>
                                    </div>
                                </td>
                                <td>Importer adds new entities or skips if entity exists</td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="entity_option" value="update">Update Entities
                                        </label>
                                    </div>
                                </td>
                                <td>Importer updates existing entities or fails if entity does not exist</td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="entity_option" value="update_ignore">Update Entities / ignore not existing
                                        </label>
                                    </div>
                                </td>
                                <td>Importer updates existing entities or skips if entity does not exist</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>