<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['attribute-mapping-explain.js']>

<@header css js/>
	<div class="row">
		<div class="col-md-12">
			<center><h2>Example Explain API</h2></center>
		</div>
	</div>
	<br />
	<div class="row">
		<div class="col-md-12">
		<#list mappingProject.getMappingTarget(selectedTarget).target.getAtomicAttributes().iterator() as attribute>
			<#if !attribute.isIdAtrribute()>
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">
						<div class="panel-heading"><strong>${attribute.name}</strong></div>
						<div class="panel-body">
							<div class="row">
								<div class="col-md-3">
					    			<button class="btn btn-default btn-xs pull-right explain-button" value="${attribute.name}">explain</button>
					    			<strong>Label</strong><br />${attribute.label}<br />
									<strong>Data type</strong><br />  ${attribute.dataType}
									<#if attributeTagMap[attribute.name]?? && attributeTagMap[attribute.name]?size gt 0>
									<br /><strong>Tags</strong><br />
									<#list attributeTagMap[attribute.name] as tag>
										<span class="label label-danger"> ${tag.label}</span>
									</#list>
									</#if>
								</div>
								<div id="${attribute.name}-explained-result" class="col-md-9"></div>
							</div>
						</div>
					</div><br>
				</div>
			</div>
			</#if>
		</#list>
		</div>
	</div>
	<input type="hidden" id="mappingProjectId" value="${mappingProject.identifier}"/>
	<input type="hidden" id="target" value="${selectedTarget}"/>
<@footer/>