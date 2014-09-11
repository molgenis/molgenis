<#macro renderEntityClassInfo entityClass showViewButton=true>
	<div class="well">
		<div class="row entity-class-header">
			<h3>${entityClass.fullName?html}</h3> 
			<i>(${entityClass.entityClassIdentifier})</i>
			
			<@hasPermission plugin='dataexplorer' entityName=entityClass.entityClassIdentifier permission='COUNT'>
				<@dataExplorerLink entityName=entityClass.entityClassIdentifier class='btn entity-btn'>Explore data</@dataExplorerLink>
			</@hasPermission>
			<#if showViewButton>
				<a href="${context_url}/${entityClass.entityClassIdentifier}" class="btn entity-btn">View</a>
			</#if>
		</div>
		<div class="row">
			<div class="col-md-2">Type:</div>
			<div class="col-md-10">${entityClass.type}</div>
		</div>
		<#if entityClass.description?has_content>
			<div class="row">
				<div class="col-md-2">Description:</div>
				<div id="entityClass-${entityClass.id?c}" class="col-md-10">${limit(entityClass.description?html, 150, 'entityClass-${entityClass.id?c}')}</div>
			</div>
		</#if>
		<#if entityClass.tags?size &gt; 0>
			<div class="row">
				<div class="col-md-2">Tags:</div>
				<div class="col-md-10">
					<#list entityClass.tags as tag>
						${tag.name}<#if tag != entityClass.tags?last>,</#if>
					</#list>
				</div>
			</div>
		</#if>
		<#if entityClass.homepage?has_content>
			<div class="row">
				<div class="col-md-2">Homepage:</div>
				<div class="col-md-10"><a href="${entityClass.homepage}" target="_blank">${entityClass.homepage}</a></div>
			</div>
		</#if>
		<#if entityClass.subEntityClasses?? && entityClass.subEntityClasses?size &gt; 0>
			<div class="row">
				<div class="col-md-2">See also:</div>
				<div class="col-md-10">
					<#list entityClass.subEntityClasses as subEntityClass>
						<div>
							<@hasPermission plugin='dataexplorer' entityName=subEntityClass.entityClassIdentifier permission='COUNT'>
								<@dataExplorerLink entityName=subEntityClass.entityClassIdentifier alternativeText=subEntityClass.fullName >${subEntityClass.fullName}</@dataExplorerLink>
							</@hasPermission>
							<@notHasPermission plugin='dataexplorer' entityName=subEntityClass.entityClassIdentifier permission='COUNT'>
								${subEntityClass.fullName} 
							</@notHasPermission>
							 (${subEntityClass.type})
						</div>
					</#list>
				</div>
			</div>
		</#if>
	</div>
</#macro>