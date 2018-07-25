<form method="post" id="wizardForm" name="wizardForm" action="" role="form">
<#if wizard.mustChangeEntityName>
    <div class="row">
        <div class="col-md-4">
            <div class="form-group">
                <label class="control-label" for="name">Entity name *</label>
                <i>(Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.)</i>
                <input type="text" class="form-control" name="name" required placeholder="Enter entity name"
                       value="${wizard.file.name
                       ?replace("\\.vcf\\.gz|\\.vcf",'','ri') <#-- remove extention -->
                       ?js_string[0..*21] <#-- maximum length is 30 chars, but we need to take into account that the samples are postfixed "_SAMPLES" -->
                       ?replace("\\-|\\.|\\*|\\$|\\&|\\%|\\^|\\(|\\)|\\#|\\!|\\@|\\?",'','r')<#-- remove illegal chars -->
                       ?replace("^[0-9]",'','r') <#-- we don't allow entitynames starting with a number -->
                       }" maxlength="22">
            </div>
        </div>
    </div>
</#if>
<#-- hide the metadata options panel in case only one option is available -->
    <div class="row<#if wizard.supportedMetadataActions?size lte 1> hidden</#if>">
        <div class="col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading"><h4 class="panel-title">Metadata options</h4></div>
                <div class="panel-body">
                    <#list wizard.supportedMetadataActions as action>
                        <#if action == 'UPSERT'>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="metadata-option" value="upsert"<#if action?index == 0>
                                           checked</#if>>
                                    <strong>Create new metadata / update existing metadata</strong>
                                </label>
                            </div>
                            <span>Importer adds new metadata or updates existing metadata<span>
                        <#elseif action == 'ADD'>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="metadata-option" value="add"<#if action?index == 0>
                                           checked</#if>>
                                    <strong>Create new metadata</strong>
                                </label>
                            </div>
                            <span>Importer adds new metadata or fails if metadata exists</span>
                        <#elseif action == 'UPDATE'>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="metadata-option" value="update"<#if action?index == 0>
                                           checked</#if>>
                                    <strong>Update existing metadata</strong>
                                </label>
                            </div>
                            <span>Importer updates existing metadata or fails if entity does not exist</span>
                        <#elseif action == 'IGNORE'>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="metadata-option" value="ignore"<#if action?index == 0>
                                           checked</#if>>
                                    <strong>Ignore metadata</strong>
                                </label>
                            </div>
                            <span>Importer ignores metadata</span>
                        </#if>
                    </#list>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading"><h4 class="panel-title">Data options</h4></div>
                <div class="panel-body">
                <#list wizard.supportedDataActions as action>
                    <#if action == 'ADD_UPDATE_EXISTING'>
                        <div class="radio">
                            <label>
                                <input type="radio" name="data-option" value="add_update"><strong>Add entities /
                                update existing</strong>
                            </label>
                        </div>
                    <span>Importer adds new entities or updates existing entities<span>
                    </#if>
                    <#if action == 'ADD'>
                        <div class="radio">
								<label>
									<input type="radio" name="data-option" value="add"
                                           checked><strong>Add entities</strong>
								</label>
							</div>
                        <span>Importer adds new entities or fails if entity exists</span>
                    </#if>
                    <#if action == 'UPDATE'>
                        <div class="radio">
								<label>
									<input type="radio" name="data-option"
                                           value="update"><strong>Update entities</strong>
								</label>
							</div>
                        <span>Importer updates existing entities or fails if entity does not exist</span>
                    </#if>
                </#list>
                </div>
            </div>
        </div>
    </div>
</form>