<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['attribute-mapping.js', 'd3.min.js','vega.min.js','jstat.min.js', 'biobankconnect-graph.js', '/jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js', 'bootbox.min.js', 'jquery.ace.js']>

<@header css/>

<script src="<@resource_href "/js/ace/src-min-noconflict/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace/src-min-noconflict/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>

<div class="row">
	<div class="col-md-12">
		<h1>Mapping: <i>${entityMapping.sourceEntityMetaData.name}</i> to <i>${entityMapping.targetEntityMetaData.name?html}.${attributeMapping.targetAttributeMetaData.label?html}</i>.</h1>
		${(attributeMapping.targetAttributeMetaData.description!"")?html}
		
		<a href="${context_url}/mappingproject/${mappingProject.identifier}" class="btn btn-default btn-xs pull-left">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to project
		</a>
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
		<div class="attribute-mapping-table-container"> <#-- Start: Attribute table container -->	
			
			<div class="row">
				<div class="col-md-12">
					<legend>Attributes</legend>
					<form class="form-inline">
						
						<div class="form-group"> 
							<label>Select Attributes:</label>
						</div> 
						
						<div class="form-group pull-right">
							<div class="checkbox">
			    				<label>
			      					<input id="selected-only-checkbox" type="checkbox"> Show selected only
			    				</label>
			  				</div>
			  				
							<div class="input-group">
			      				<span class="input-group-btn">
			        				<button id="attribute-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
			      				</span>
			      				<input id="attribute-search-field" type="text" class="form-control" placeholder="Search">
			    			</div>
						</div>
						
					</form>
					<br/>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					<table id="attribute-mapping-table" class="table table-bordered scroll">
						<thead>
							<tr>
								<th>Select</th>
								<th>Attribute</th>
								<th>Algorithm value</th>
							</tr>
						</thead>
						<tbody>
							<#list entityMapping.sourceEntityMetaData.attributes as source>
								<tr>
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
					
					<nav style="text-align:center">
			  			<ul class="pagination">
			    			<li>
					      		<a href="#" aria-label="Previous">
					        		<span aria-hidden="true">&laquo;</span>
					      		</a>
					    	</li>
					    	<li><a href="#">1</a></li>
					    	<li><a href="#">2</a></li>
					    	<li><a href="#">3</a></li>
					    	<li><a href="#">4</a></li>
					    	<li><a href="#">5</a></li>
					    	<li>
					     		<a href="#" aria-label="Next">
					        		<span aria-hidden="true">&raquo;</span>
					      		</a>
					    	</li>
					  	</ul>
					</nav>		
				</div>
			</div>
			
		</div> <#-- End: Attribute table container -->
	</div> <#-- End: Attribute table column --> 
	
	<div class="col-md-6 col-lg-4"> <#-- Start: Mapping column -->
		<div class="attribute-mapping-container">  <#-- Start: Mapping container -->
			
			<div class="row">
				<div class="col-md-12">
					<legend>Mapping</legend>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					<ul class="nav nav-pills" role="tablist">
			    		<li role="presentation" class="active"><a href="#equal" aria-controls="equal" role="tab" data-toggle="tab"> = </a></li>
			    		<li role="presentation"><a href="#function" aria-controls="function" role="tab" data-toggle="tab">Function</a></li>
			    		<li role="presentation"><a href="#map" aria-controls="map" role="tab" data-toggle="tab">Map</a></li> 
			    		<li role="presentation"><a href="#script" aria-controls="script" role="tab" data-toggle="tab">Script</a></li>
			   		</ul>
			   		<br/>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-12">
					 <div class="tab-content">
			    		<div role="tabpanel" class="tab-pane fade active" id="equal"><@equal /></div>
			    		<div role="tabpanel" class="tab-pane fade" id="function"><@function /></div>
			    		<div role="tabpanel" class="tab-pane fade" id="map"><@map /></div>
			    		<div role="tabpanel" class="tab-pane fade" id="script"><@script /></div>
			    	</div>
			    	<br/>
				</div>
			</div>
			
		</div> <#-- End: Mapping container -->
	</div>  <#-- End: Mapping column -->
	
	<div class="col-md-6 col-lg-4"> <#-- Start Result column -->
		<div class="result-container"> <#-- Start: Result container -->
			
			<div class="row">
				<div class="col-md-12">
					<legend>Result</legend>
					<form class="form-inline">		
		
						<div class="form-group">
							X success, X missing, X errors
						</div>
						<div class="form-group pull-right">
							<div class="checkbox">
			    				<label>
			      					<input id="errors-only-checkbox" type="checkbox"> Errors only
			    				</label>
			  				</div>
			  				
							<div class="input-group">
			      				<span class="input-group-btn">
			        				<button id="result-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
			      				</span>
			      				<input id="result-search-field" type="text" class="form-control" placeholder="Search">
			    			</div>
						</div>
					</form>
				</div>
			</div>

			<div class="row">
				<div class="col-md-12">
					<table class="table table-bordered">
						<thead>
							<th>Source attribute X values</th>
							<th>Result values</th>
							<th>Status</th>
						</thead>
						<tbody>
							<tr>
								<td>value</td>
								<td>result value</td>
								<td>OK</td>
							</tr>
							<tr>
								<td>value</td>
								<td>result value</td>
								<td>OK</td>
							</tr>
							<tr>
								<td>value</td>
								<td>result value</td>
								<td>OK</td>
							</tr>
						</tbody>
					</table>
					
					<nav style="text-align:center">
		  			<ul class="pagination">
		    			<li>
				      		<a href="#" aria-label="Previous">
				        		<span aria-hidden="true">&laquo;</span>
				      		</a>
				    	</li>
				    	<li><a href="#">1</a></li>
				    	<li><a href="#">2</a></li>
				    	<li><a href="#">3</a></li>
				    	<li><a href="#">4</a></li>
				    	<li><a href="#">5</a></li>
				    	<li>
				     		<a href="#" aria-label="Next">
				        		<span aria-hidden="true">&raquo;</span>
				      		</a>
				    	</li>
				  	</ul>
				</nav>		
			</div>
			
		</div> <#-- End: Result container -->
	</div> <#-- End: Result column -->
	
</div> <#-- End: Master row -->


<div class="row">
	<div class="col-md-12"> <#-- Start: Save column -->
		<div class="save-and-test-btn-container"> <#-- Start: Save container -->
			<hr></hr>
			<form>
			</form>
		</div> <#-- End: Save container -->
	</div> <#-- End: Save column -->
</div>


<#-- equal tab -->
<#macro equal> 
	<div class="row">
		<div class="col-md-12">
			<h4>Simple direct one on one mapping</h4>
		</div>
	</div>
</#macro>

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
			<script>
				$("#advanced-mapping-table").load("advancedmappingeditor #advanced-mapping-table", {
					mappingProjectId : "${mappingProject.identifier}",
					target : "${entityMapping.targetEntityMetaData.name?html}",
					source : "${entityMapping.name?html}",
					targetAttribute : "${attributeMapping.targetAttributeMetaData.name?html}",
					sourceAttribute : "LifeLines_GENDER"
				});
			</script>
		</div>
	</div>
</#macro>

<#-- algorithm editor tab -->
<#macro script>
	<div class="row">
		<div class="col-md-12">
			<h4>Algorithm</h4>
			<form>
				<div class="form-group">
					<input type="text"></input>
				</div>
			</form>
		</div>
	</div>
</#macro>

<@footer/>