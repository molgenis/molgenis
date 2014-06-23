<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["select2.css", "jquery-sortable.css", "menumanager.css"]>
<#assign js=["select2.min.js","jquery-sortable-min.js", "menumanager.js"]>
<@header css js/>
	<div class="row-fluid offset2 span8">
		<p>Drag and drop menu items to update menu</p>
		<div class="row-fluid" id="menu-editor-container">
			<div class="row-fluid">
				<div class="span7">
					<div id="menu-editor-tree">
						<@create_menu_list molgenis_ui.menu true/>
					</div>
				</div>
				<div class="span5">
					<legend>Create Menu</legend>
					<form name="add-menu-group-form" class="form-horizontal">
						<div class="control-group">
							<label class="control-label" for="group-name">Name</label>
							<div class="controls">
								<input type="text" name="group-name">
							</div>
						</div>
						<div class="control-group">
							<div class="controls">
								<button type="submit" class="btn">Create</button>
							</div>
						</div>
					</form>
					<legend>Create Menu Item</legend>
					<form name="add-menu-item-form" class="form-horizontal">
						<div class="control-group">
							<label class="control-label" for="menu-item">Plugin *</label>
							<div class="controls">
								<select id="menu-item-select" required>
								<#list plugins as plugin>
									<option value="${plugin.id}">${plugin.id}</option>
								</#list>
								</select>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="menu-item-name">Name</label>
							<div class="controls">
								<input type="text" name="menu-item-name">
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="menu-item-params">Query string</label>
							<div class="controls">
								<input type="text" name="menu-item-params">
							</div>
						</div>
						<div class="control-group">
							<div class="controls">
								<button type="submit" class="btn">Create</button>
							</div>
						</div>
					</form>
				</div>
			</div>
			<div class="row-fluid">
				<form name="save-menu-form" action="${context_url}/save" method="POST">
					<button type="submit" class="btn btn-primary pull-right">Save</button>
				</form>
			</div>
		</div>
	</div>
<@footer/>
<form name="edit-menu-item-form" class="form-horizontal">
	<div class="modal hide medium" id="edit-menu-item-modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">				
		      	<div class="modal-header">
		        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        	<h4 class="modal-title">Edit menu item</h4>
		     	</div>
		      	<div class="modal-body">
		      		<div class="form-horizontal">
			      		<div class="control-group">
			    			<label class="control-label" for="label">Menu item name *</label>
			    			<div class="controls">
			      				<input type="text" name="label" required>
			    			</div>
			  			</div>
		      		</div>
				</div>
		      	<div class="modal-footer">
		        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
		        	<button type="submit" class="btn btn-primary">Save</button>
		      	</div>
		    </div>
		</div>
	</div>
</form>
<#macro create_menu_list menu is_root>
	<#if is_root>
	<ol class="vertical">
	</#if>
		<li class="<#if is_root>root<#else>node highlight</#if>" data-id="${menu.id}" data-label="${menu.name}">
		<#if !is_root>
			<i class="icon-move"></i>
		</#if>
		<#if !is_root>
			<span>${menu.name}</span>
			<div class="pull-right">
				<i class="icon-edit" data-toggle="modal" data-target="#edit-menu-item-modal"></i>
				<i class="icon-trash"></i>
			</div>
		</#if>
			<ol>
	<#list menu.items as item>
		<#if item.type == "MENU">
			<@create_menu_list item false/>
		<#else>
		<li class="node" data-id="${item.id}" data-label="${item.name}">
			<i class="icon-move"></i>
			<span>${item.name}</span>
			<div class="pull-right">
				<i class="icon-edit" data-toggle="modal" data-target="#edit-menu-item-modal"></i>
				<i class="icon-trash"></i>
			</div>
		</li>
		</#if>
	</#list>
			</ol>
		</li>
<#if is_root>
	</ol>
</#if>
</#macro>