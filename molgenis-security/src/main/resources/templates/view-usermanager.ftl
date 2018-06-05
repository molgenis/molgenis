<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["usermanager.js"]>

<@header css js/>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <legend>
                User management
                <a id="create-user-btn" href="#" style="margin:30px 10px">
                    <img src="/img/new.png">
                </a>
            </legend>

            <table class="table">
                <thead>
                <tr>
                    <th>Edit</th>
                    <th>Active</th>
                    <th>Username</th>
                    <th>Full name</th>
                </tr>
                </thead>

                <tbody>
                        <#if users?has_content>
                            <#list users as user>
                            <tr id="userRow${user.id?html}">
                                <td><a href="#" class="edit-user-btn" data-id="${user.id?html}"><img
                                        src="/img/editview.gif"></a></td>
                                <td><input type="checkbox" class="activate-user-checkbox" data-id="${user.id?html}"
                                           <#if user.isActive()>checked</#if> <#if user.isSuperuser()>disabled</#if>>
                                </td>
                                <td>${user.getUsername()!?html}</td>
                                <td>${user.getFullName()!?html}</td>
                            </tr>
                            </#list>
                        </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
<@footer/>