<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-sortable.css", "menumanager.css"]>
<#assign js=["jquery-sortable-min.js", "menumanager.js"]>
<@header css js/>

<div class="row" id="menu-editor-container">
    <div class="col-md-5">
        <p>
            Drag and drop menu items to update menu, press Save to store the menu.
            Each menu should contain at least one item.
        </p>

        <div id="menu-editor-tree">
        <@create_menu_list molgenis_ui.menu true/>
        </div>
    </div>

    <div class="col-md-6">
        <div class="row">
            <div class="col-md-6">
                <legend>Create Menu</legend>
                <form name="add-menu-group-form" class="form-horizontal" role="form">
                <@create_edit_menu_inputs/>
                    <div class="form-group">
                        <div class="col-md-9 col-md-offset-3">
                            <button type="submit" class="btn btn-default pull-right">Create</button>
                        </div>
                    </div>
                </form>
            </div>
            <div class="col-md-6">
                <legend>Create Menu Item</legend>
                <form name="add-menu-item-form" class="form-horizontal" role="form">
                <@create_edit_item_inputs false/>
                    <div class="form-group">
                        <div class="col-md-9 col-md-offset-3">
                            <button type="submit" class="btn btn-default pull-right">Create</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <hr></hr>
                <form name="save-menu-form" action="${context_url?html}/save" method="POST">
                    <button type="submit" class="btn btn-info pull-right">Save the new menu layout</button>
                </form>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <br></br>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <legend>Upload logo</legend>
                <form name="upload-new-logo" class="form-horizontal" role="form"
                      action="${context_url?html}/upload-logo" method="POST" enctype="multipart/form-data">
                <@upload_new_logo />
                    <div class="form-group">
                        <div class="col-md-9 col-md-offset-3">
                            <input type="submit" value="Upload logo" class="btn btn-primary pull-right"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <hr></hr>
    </div>
</div>


<@footer/>

<form name="edit-menu-form" class="form-horizontal" role="form">
    <div class="modal" id="edit-menu-modal" tabindex="-1" role="dialog" aria-labelledby="edit-menu-modal-label"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="edit-menu-modal-label">Edit menu</h4>
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
    <div class="modal" id="edit-item-modal" tabindex="-1" role="dialog" aria-labelledby="edit-item-modal-label"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="edit-item-modal-label">Edit menu item</h4>
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
<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-id">Id *</label>
    </div>

    <div class="col-md-9">
        <input type="text" class="form-control" name="menu-id" required disabled>
    </div>
</div>

<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-name">Name *</label>
    </div>
    <div class="col-md-9">
        <input type="text" class="form-control" name="menu-name" required>
    </div>
</div>
</#macro>

<#macro create_edit_item_inputs is_edit>
<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-item">Plugin *</label>
    </div>

    <div class="col-md-9">
        <select class="form-control" name="menu-item-select" required<#if is_edit> disabled</#if>>
            <#list plugins as plugin>
                <option value="${plugin.id?html}">${plugin.id?html}</option>
            </#list>
        </select>
    </div>
</div>

<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-item-name">Name *</label>
    </div>

    <div class="col-md-9">
        <input type="text" class="form-control" name="menu-item-name" required>
    </div>
</div>

<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-item-params">Query string</label>
    </div>

    <div class="col-md-9">
        <input type="text" class="form-control" name="menu-item-params">
    </div>
</div>
</#macro>

<#macro create_menu_list menu is_root>
    <#if is_root>
    <ol class="vertical root">
    </#if>
    <li class="node highlight<#if is_root> root</#if>" data-id="${menu.id?html}" data-label="${menu.name?html}">
        <#if !is_root>
            <span class="glyphicon glyphicon-move"></span>
        </#if>
        <span>${menu.name?html}</span>
        <div class="pull-right">
            <#if !is_root>
                <span class="glyphicon glyphicon-edit edit-menu-btn" data-toggle="modal"
                      data-target="#edit-menu-modal"></span>
            </#if>
            <#if !is_root>
                <span class="glyphicon glyphicon-trash"></span>
            </#if>
        </div>
        <ol>
            <#list menu.items as item>
                <#if item.type == "MENU">
                    <@create_menu_list item false/>
                <#else>
                <#-- extract query string from url -->
                    <li class="node" data-id="${item.id?html}" data-label="${item.name?html}" <#if item.id != item.url>
                        data-params="${item.url?substring(item.id?length + 1)?html}"</#if>>
                        <span class="glyphicon glyphicon-move"></span>
                        <span>${item.name?html}</span>
                        <div class="pull-right">
                            <span class="glyphicon glyphicon-edit edit-item-btn" data-toggle="modal"
                                  data-target="#edit-item-modal"></span>
                            <span class="glyphicon glyphicon-trash"></span>
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

<#macro upload_new_logo>
<div class="form-group">
    <div class="col-md-3">
        <label class="control-label pull-right" for="menu-id">Choose file</label>
    </div>

    <div class="col-md-9">
        <input type="file" name="logo" data-filename-placement="inside" title="Select a file...">
    </div>
</div>
</#macro>

<script id="menu-template" type="text/x-handlebars-template">
    <li class="node highlight" data-id="{{id}}" data-label="{{label}}">
        <span class="glyphicon glyphicon-move"></span>
        <span>{{label}}</span>
        <div class="pull-right">
            <span class="glyphicon glyphicon-edit edit-menu-btn" data-toggle="modal"
                  data-target="#edit-menu-modal"></span>
            <span class="glyphicon glyphicon-trash"></span>
        </div>
        <ol>
            <ol>
                </li>
</script>

<script id="item-template" type="text/x-handlebars-template">
    <li class="node" data-id="{{id}}" data-label="{{label}}" data-params="{{params}}">
        <span class="glyphicon glyphicon-move"></span>
        <span>{{label}}</span>
        <div class="pull-right">
            <span class="glyphicon glyphicon-edit edit-item-btn" data-toggle="modal"
                  data-target="#edit-item-modal"></span>
            <span class="glyphicon glyphicon-trash"></span>
        </div>
    </li>
</script>