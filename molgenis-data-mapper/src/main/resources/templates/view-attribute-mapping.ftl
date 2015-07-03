<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=[
	'attribute-mapping.js', 
	'd3.min.js',
	'vega.min.js',
	'jstat.min.js',
	'biobankconnect-graph.js',
	'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js',
	'bootbox.min.js',
	'jquery.ace.js',
	'jquery.highlight.js'
]>

<@header css js/>

<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<div class="row">
	<div class="col-md-12">
		<a href="${context_url?html}/mappingproject/${mappingProject.identifier?html}" class="btn btn-default btn-xs pull-left">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to project
		</a>
		
		<#-- Hidden fields containing information needed for ajax requests -->
		<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier?html}"/>
		<input type="hidden" name="target" value="${entityMapping.targetEntityMetaData.name?html}"/>
		<input type="hidden" name="source" value="${entityMapping.sourceEntityMetaData.name?html}"/>
		<input type="hidden" name="targetAttribute" value="${attributeMapping.targetAttributeMetaData.name?html}"/>
		<input type="hidden" name="targetAttributeType" value="${attributeMapping.targetAttributeMetaData.dataType?html}"/>
		
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<hr></hr>
		<p>
			${attributeMapping.targetAttributeMetaData.name?html} (${attributeMapping.targetAttributeMetaData.dataType}) : ${(attributeMapping.targetAttributeMetaData.description!"")?html}
		</p>
	</div>
</div>

<div class="row"> <#-- Start: Master row -->
	
	<div class="col-md-6 col-lg-4"> <#-- Start: Attribute table column -->
		<div id="attribute-mapping-table-container"> <#-- Start: Attribute table container -->	
			
			<div class="row">
				<div class="col-md-12">
					<legend>
						Attributes
						<i class="glyphicon glyphicon-question-sign" rel="tooltip" title="Select attribute(s) to map to 
						${attributeMapping.targetAttributeMetaData.name?html}. By checking one of the attributes below, 
						an algorithm will be generated and the result of your selection will be shown."></i>
					</legend>
					
					<form>
			  			<div class="form-group">
							<input id="attribute-search-field" type="text" class="form-control" placeholder="Search...">
						</div>
					</form>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					<table id="attribute-mapping-table" class="table table-bordered scroll">
						<thead>
							<th>Select</th>
							<th>Attribute</th>
							<th>Algorithm value</th>
						</thead>
						<tbody>
							<#list entityMapping.sourceEntityMetaData.attributes as source>
								<tr data-attribute-name="${source.name?html}" data-attribute-label="${source.label?html}">
									<td>
										<div class="checkbox">
											<label>
												<input data-attribute-name="${source.name?html}" type="checkbox">
											</label>
										</div>
									</td>
									<td class="source-attribute-information">
										<b>${source.label?html}</b> (${source.dataType?html})
										<#if source.nillable> <span class="label label-warning">nillable</span></#if>
										<#if source.unique> <span class="label label-default">unique</span></#if>
										<#if source.description??><br />${source.description?html}</#if>
									</td>
									<td>
										${source.name?html}
									</td>
								</tr>
							</#list>
						</tbody>
					</table>
				</div>
			</div>
			
		</div> <#-- End: Attribute table container -->
	</div> <#-- End: Attribute table column --> 
	
	<div class="col-md-6 col-lg-4"> <#-- Start: Mapping column -->
		<div id="attribute-mapping-container">  <#-- Start: Mapping container -->
			
			<div class="row">
				<div class="col-md-12">
					<legend>
						Mapping
						<i class="glyphicon glyphicon-question-sign" rel="tooltip" title="Use one of the methods below to map the values of the 
						selected attribute(s) to the target attribute. The script editor offers large control over your algorithm, but javascript knowledge is needed.
						<#if attributeMapping.targetAttributeMetaData.dataType == "xref" || attributeMapping.targetAttributeMetaData.dataType == "categorical" ||
				    		attributeMapping.targetAttributeMetaData.dataType == "mref" || attributeMapping.targetAttributeMetaData.dataType == "string">
				    		The Map tab allows you to map the various categorical values or strings to the categorical values of the target attribute.
				    	</#if>"></i>
					</legend>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					<ul class="nav nav-tabs" role="tablist">
			    		<li role="presentation" class="active"><a href="#script" aria-controls="script" role="tab" data-toggle="tab">Script</a></li>
			    		
			    		<#if attributeMapping.targetAttributeMetaData.dataType == "xref" || attributeMapping.targetAttributeMetaData.dataType == "categorical" ||
			    		attributeMapping.targetAttributeMetaData.dataType == "mref">
			    			<li role="presentation"><a href="#map" aria-controls="map" role="tab" data-toggle="tab">Map</a></li>
		    			</#if> 
			   		</ul>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					 <div class="tab-content">
			    		<div role="tabpanel" class="tab-pane active" id="script"><@script /></div>
			    		<div role="tabpanel" class="tab-pane" id="map"><@map /></div>
			    	</div>
			    	<br/>
				</div>
			</div>
			
		</div> <#-- End: Mapping container -->
	</div>  <#-- End: Mapping column -->

	<div class="col-md-6 col-lg-4"> <#-- Start Result column -->
		<div id="result-container"> <#-- Start: Result container -->
			
			<div class="row">
				<div class="col-md-12">
					<legend>
						Result
						<i class="glyphicon glyphicon-question-sign" rel="tooltip" title="The most right column contains the results 
						of applying the algorithm over the values of the selected source attributes."></i>	
					</legend>
					<p>
						
					</p>
					<h4>Validation</h4>
					<p>Algorithm validation starts automatically when the algorithm is updated.</p> 
                    <div id="mapping-validation-container"></div>
					<h4>Preview</h4>
					<div id="result-table-container"></div>
				</div>
			</div>
			 
		</div> <#-- End: Result container -->
	</div> <#-- End: Result column -->	

	<div class="col-md-12 col-lg-12">
		<hr></hr>
		<button id="save-mapping-btn" type="btn" class="btn btn-primary btn-lg pull-right">Save</button>	
	</div>

</div> <#-- End: Master row -->


<#-- map tab -->
<#macro map>
	<div class="row">
		<div class="col-md-12">
			<div id="advanced-mapping-table"></div>
		</div>
	</div>
</#macro>

<#-- algorithm editor tab -->
<#macro script>
	<div class="row">
		<div class="col-md-12">
			<div class="ace-editor-container">
				<h4>Algorithm</h4>
				<p>
					Use the script editor to determine how the values of selected attributes are processed. 
					See the <a id="js-function-modal-btn" href="#">list of available functions</a> for more information. 
				</p>
				<#-- For future calculator layout around script editor
					<form>
						<button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span></button>
						<button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-minus"></span></button>
						<button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-asterisk"></span></button>
						<button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-option-vertical"></span></button>
					</form>
					<br></br>
				-->
				<textarea id="ace-editor-text-area" name="algorithm" rows="15" <#if !hasWritePermission>data-readonly="true"</#if> 
					style="width:100%;">${(attributeMapping.algorithm!"")?html}</textarea>
			</div>
		</div>
	</div>
</#macro>


<div id="js-function-modal" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title">Javascript function examples</h4>
			</div>
			
			<div class="modal-body">
				<div class="row">
					<div class="col-md-2"
						<strong>.map()</strong>
					</div>
					<div class="col-md-10"
						<p>can be used to map multiple values to eachother. Example: <b>$('GENDER').map({"0":"0","1":"1",}).value()</b> </p>
					</div>
				</div>
				
				<div class="row">
					<div class="col-md-2"
						<strong>.date()</strong>
					</div>
					<div class="col-md-10"
						<p>Can be used to calculate the date. Example: <b>$('DATE').date().value()</b> </p>
					</div>
				</div>	
      		</div>
		</div>
	</div>
</div>
<div class="modal" id="validation-error-messages-modal" tabindex="-1" role="dialog" aria-labelledby="validation-error-messages-label" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">             
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="validation-error-messages-label">Validation Errors</h4>
            </div>
            <div class="modal-body">
                <table class="table table-bordered validation-error-messages-table">
                    <thead>
                        <th>Source Entity</th> 
                        <th>Error message</th>
                    </thead>
                    <tbody id="validation-error-messages-table-body">
                    </tbody>
                </table>                        
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<@footer/>