<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["select2.css", "jquery-sortable.css", "menumanager.css"]>
<#assign js=["handlebars.min.js","select2.min.js","jquery-sortable-min.js", "menumanager.js"]>
<@header css js/>
	<div class="row-fluid offset2 span8">
		<p>Drag and drop menu items to update menu, press Save to store the menu. Each menu should contain at least one item.</p>
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
						<@create_edit_menu_inputs/>
						<div class="control-group">
							<div class="controls">
								<button type="submit" class="btn">Create</button>
							</div>
						</div>
					</form>
					<legend>Create Menu Item</legend>
					<form name="add-menu-item-form" class="form-horizontal">
						<@create_edit_item_inputs false/>
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
<form name="edit-menu-form" class="form-horizontal">
	<div class="modal hide medium" id="edit-menu-modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">				
		      	<div class="modal-header">
		        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        	<h4 class="modal-title">Edit menu</h4>
		     	</div>
		      	<div class="modal-body">
		      		<div class="form-horizontal">
			      		<@create_edit_menu_inputs/>
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
<form name="edit-item-form" class="form-horizontal">
	<div class="modal hide medium" id="edit-item-modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">				
		      	<div class="modal-header">
		        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        	<h4 class="modal-title">Edit menu item</h4>
		     	</div>
		      	<div class="modal-body">
		      		<div class="form-horizontal">
			      		<@create_edit_item_inputs true/>
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
<#macro create_edit_menu_inputs>
<div class="control-group">
	<label class="control-label" for="menu-id">Id *</label>
	<div class="controls">
		<input type="text" name="menu-id" required disabled>
	</div>
</div>
<div class="control-group">
	<label class="control-label" for="menu-name">Name *</label>
	<div class="controls">
		<input type="text" name="menu-name" required>
	</div>
</div>
</#macro>
<#macro create_edit_item_inputs is_edit>
<div class="control-group">
	<label class="control-label" for="menu-item">Plugin *</label>
	<div class="controls">
		<select name="menu-item-select" required<#if is_edit> disabled</#if>>
		<#list plugins as plugin>
			<option value="${plugin.id}">${plugin.id}</option>
		</#list>
		</select>
	</div>
</div>
<div class="control-group">
	<label class="control-label" for="menu-item-name">Name *</label>
	<div class="controls">
		<input type="text" name="menu-item-name" required>
	</div>
</div>
<div class="control-group">
	<label class="control-label" for="menu-item-params">Query string</label>
	<div class="controls">
		<input type="text" name="menu-item-params">
	</div>
</div>
</#macro>
<#macro create_menu_list menu is_root>
	<#if is_root>
	<ol class="vertical root">
	</#if>
		<li class="node highlight<#if is_root> root</#if>" data-id="${menu.id}" data-label="${menu.name}">
		<#if !is_root>
			<i class="icon-move"></i>
		</#if>
			<span>${menu.name}</span>
			<div class="pull-right">
			<#if !is_root>
				<i class="icon-edit edit-menu-btn" data-toggle="modal" data-target="#edit-menu-modal"></i>
			</#if>
			<#if !is_root>
				<i class="icon-trash"></i>
			</#if>
			</div>
			<ol>
	<#list menu.items as item>
		<#if item.type == "MENU">
			<@create_menu_list item false/>
		<#else>
		<#-- extract query string from url -->
		<li class="node" data-id="${item.id}" data-label="${item.name}" <#if item.id != item.url> data-params="${item.url?substring(item.id?length + 1)?html}"</#if>>
			<i class="icon-move"></i>
			<span>${item.name}</span>
			<div class="pull-right">
				<i class="icon-edit edit-item-btn" data-toggle="modal" data-target="#edit-item-modal"></i>
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
<script id="menu-template" type="text/x-handlebars-template">
	<li class="node highlight" data-id="{{id}}" data-label="{{label}}">
		<i class="icon-move"></i>
		<span>{{label}}</span>
		<div class="pull-right">
			<i class="icon-edit edit-menu-btn" data-toggle="modal" data-target="#edit-menu-modal"></i>
			<i class="icon-trash"></i>
		</div>
		<ol><ol>
	</li>
</script>
<script id="item-template" type="text/x-handlebars-template">
	<li class="node" data-id="{{id}}" data-label="{{label}}" data-params="{{params}}">
		<i class="icon-move"></i>
		<span>{{label}}</span>
		<div class="pull-right">
			<i class="icon-edit edit-item-btn" data-toggle="modal" data-target="#edit-item-modal"></i>
			<i class="icon-trash"></i>
		</div>
	</li>
</script>