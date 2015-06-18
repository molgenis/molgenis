<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=[
	'attribute-mapping.js', 
	'd3.min.js',
	'vega.min.js',
	'jstat.min.js',
	'biobankconnect-graph.js',
	'/jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js',
	'bootbox.min.js',
	'jquery.ace.js'
]>

<@header css js/>

<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<div class="row">
	<div class="col-md-12">
		<h1>Mapping: <i>${entityMapping.sourceEntityMetaData.name?html}</i> to <i>${entityMapping.targetEntityMetaData.name?html}.${attributeMapping.targetAttributeMetaData.label?html}</i>.</h1>
		${(attributeMapping.targetAttributeMetaData.description!"")?html}
		
		<a href="${context_url}/mappingproject/${mappingProject.identifier}" class="btn btn-default btn-xs pull-left">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to project
		</a>
		
		<#-- Hidden fields containing information needed for ajax requests -->
		<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier?html}"/>
		<input type="hidden" name="target" value="${entityMapping.targetEntityMetaData.name?html}"/>
		<input type="hidden" name="source" value="${entityMapping.sourceEntityMetaData.name?html}"/>
		<input type="hidden" name="targetAttribute" value="${attributeMapping.targetAttributeMetaData.name?html}"/>
		<input type="hidden" name="targetAttributeType" value="${attributeMapping.targetAttributeMetaData.dataType}"/>
		
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<br/>
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
						<button id="test-mapping-btn" type="btn" class="btn btn-success btn-sm pull-right">Test selection</button>
					</legend>
					<p>
						Select attribute(s) to map to ${attributeMapping.targetAttributeMetaData.name?html}. 
						By checking one of the attributes below, an algorithm will be generated and the result of your selection will be shown.
					</p>
					<form class="form-inline">
						<div class="form-group">
							<div class="checkbox">
			    				<label>
			      					<input id="selected-only-checkbox" type="checkbox"> Show selected only
			    				</label>
			  				</div>
			  			</div>	
			  			<div class="form-group pull-right">
							<div class="input-group">
			      				<span class="input-group-btn">
			        				<button id="attribute-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
			      				</span>
			      				<input id="attribute-search-field" type="text" class="form-control" placeholder="Search">
			    			</div>
			    			<br></br>
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
								<tr class="${source.name}">
									<td>
										<div class="checkbox">
											<label>
												<input class="${source.name}" type="checkbox">
											</label>
										</div>
									</td>
									<td>
										<b>${source.label?html}</b> (${source.dataType})
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
						<button id="reset-algorithm-changes-btn" type="btn" class="btn btn-warning btn-sm pull-right">Reset</button>
					</legend>
					<p>
						Use one of the methods below to map the values of the selected attribute(s) to the target attribute. 
						The script editor offers large control over your algorithm, but javascript knowledge is needed.
						<#if attributeMapping.targetAttributeMetaData.dataType == "xref" || attributeMapping.targetAttributeMetaData.dataType == "categorical" ||
				    		attributeMapping.targetAttributeMetaData.dataType == "mref" || attributeMapping.targetAttributeMetaData.dataType == "string">
				    		The Map tab allows you to map the various categorical values or strings to the categorical values of the target attribute.
				    	</#if>
				    	
				    	<#if attributeMapping.targetAttributeMetaData.dataType == "decimal">
				    		The Function tab allows you to perform basic mathematical methods to your source attribute values.
				    	</#if>
					</p>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					<ul class="nav nav-tabs" role="tablist">
			    		<li role="presentation" class="active"><a href="#script" aria-controls="script" role="tab" data-toggle="tab">Script</a></li>
			    		
			    		<#if attributeMapping.targetAttributeMetaData.dataType == "decimal">
			    			<li role="presentation"><a href="#function" aria-controls="function" role="tab" data-toggle="tab">Function</a></li>
		    			</#if>
			    		
			    		<#if attributeMapping.targetAttributeMetaData.dataType == "xref" || attributeMapping.targetAttributeMetaData.dataType == "categorical" ||
			    		attributeMapping.targetAttributeMetaData.dataType == "mref" || attributeMapping.targetAttributeMetaData.dataType == "string">
			    			<li role="presentation"><a href="#map" aria-controls="map" role="tab" data-toggle="tab">Map</a></li>
		    			</#if> 
			   		</ul>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					 <div class="tab-content">
			    		<div role="tabpanel" class="tab-pane active" id="script"><@script /></div>
			    		<div role="tabpanel" class="tab-pane" id="function"><@function /></div>
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
						<button id="save-mapping-btn" type="btn" class="btn btn-primary btn-sm pull-right">Save</button>
					</legend>
					<p>
						The most right column contains the results of applying the algorithm 
						over the values of the selected source attributes.
					</p>
					<div id="result-table-container"></div>
				</div>
			</div>
			 
		</div> <#-- End: Result container -->
	</div> <#-- End: Result column -->	

</div> <#-- End: Master row -->

<#-- function tab -->
<#macro function>
	<div class="row">
		<div class="col-md-12">
			<h4>Apply a mathematical function to the values of the selected attribute</h4>
			<form class="form-inline">
				
				<div id="function-selected-attribute-field" class="form-group">
			    	Attribute: 
		    	</div>
		    	
		    	<div class="form-group">
		    		<div class="input-group">
		    			<div class="input-group-btn">
					    	<select class="form-control" id="function-operator-field">
					    		<option value="" selected disabled>Please select</option>
					    		<option value="divide">Divide</option>
					    		<option value="multiply">Multiply</option>
					    		<option value="min">Minus</option>
					    		<option value="sum">Plus</option>
					    	</select>
				    	</div>
			    		<input type="text" class="form-control" id="function-value-field" placeholder="function value..."></input>
		    		</div>
			  	</div>
			  	
			</form>
			<br/>
		</div>
	</div>
</#macro>

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
				<p>Use the script editor to determine how selected attributes.</p>
				<textarea id="ace-editor-text-area" name="algorithm" rows="15" <#if !hasWritePermission>data-readonly="true"</#if> 
					style="width:100%;">${(attributeMapping.algorithm!"")?html}</textarea>
			</div>
		</div>
	</div>
</#macro>

<@footer/>