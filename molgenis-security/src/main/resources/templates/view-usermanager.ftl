<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["usermanager.css"]>
<#assign js=["usermanager.js"]>

<@header css js/>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <legend>User management</legend>
            <button id="create-user-btn" class="btn btn-success btn-xs" type="button" title="Add user">
                <span>
                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                    <span class="sr-only">add user</span>
                </span>
            </button>

            <table class="table">
                <thead>
                <tr>
                    <th>Edit</th>
                    <th>Active</th>
                    <th>Username</th>
                    <th>Full name</th>
                    <th>Member of groups</th>
                </tr>
                </thead>

                <tbody>
                <#if users?has_content>
                    <#list users as user>
                    <tr id="userRow${user.user.id?html}">
                        <td>
                            <button class="btn btn-default btn-xs edit-user-btn" type="button" title="Edit user"
                                    data-id="${user.user.id?html}">
                                <span>
                                    <span class="glyphicon glyphicon-edit" aria-hidden="true"></span>
                                    <span class="sr-only">edit user ${user.user.formattedName!?html}</span>
                                </span>
                            </button>
                        <td><input type="checkbox" class="activate-user-checkbox" data-id="${user.user.id?html}"
                                   <#if user.user.active>checked</#if> <#if user.user.superuser>disabled</#if>>
                        </td>
                        <td>${user.user.username!?html}</td>
                        <td>${user.user.formattedName!?html}</td>
                        <td><#list user.currentGroups as group>
                            ${group.label?html}
                        </#list>
                    </tr>
                    </#list>
                </#if>
                </tbody>
            </table>
        </div>

    </div>
</div>
<@footer/>