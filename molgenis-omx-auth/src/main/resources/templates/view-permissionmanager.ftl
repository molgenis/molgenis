<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["permissionmanager.css"]>
<#assign js=["permissionmanager.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span2"></div>
		<div class="span8">
			<div class="row-fluid">
				<div class="tabbable tabs-left">
					<ul class="nav nav-tabs">
						<li class="active"><a href="#plugin-permission-manager" data-toggle="tab">Plugin Permissions</a></li>
						<li><a href="#entity-class-permission-manager" data-toggle="tab">Entity Class Permissions</a></li>  
						<li><a href="#entity-permission-manager" data-toggle="tab">Entity Permissions</a></li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane active" id="plugin-permission-manager">
					<#include "/view-permissionmanager-plugin.ftl">
						</div>
						<div class="tab-pane" id="entity-class-permission-manager">
					<#include "/view-permissionmanager-entity-class.ftl">
						</div>
						<div class="tab-pane" id="entity-permission-manager">
					<#include "/view-permissionmanager-entity.ftl">
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="span2"></div>
	</div>
<@footer/>