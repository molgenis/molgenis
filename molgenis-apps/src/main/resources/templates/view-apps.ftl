<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>
<div class="row">
    <div class="col-md-12">
    <#if appNotAvailableMessage??>
        <script>
            alert("App not available!");
        </script>
    </#if>
        <button class="btn btn-default" id="create-app-btn">Create new App</button>
    </div>
    <hr></hr>
</div>
<div class="row">
<#list apps as app>
    <div class="col-sm-6 col-md-4">
        <div class="thumbnail">
            <img src="http://www.cheatsheet.com/wp-content/uploads/2012/04/apple_app_store.jpeg" alt="App"
                 height="100px" width="100px">
            <div class="caption">
                <h3>${app.name}</h3>
                <p>${app.description}</p>
                <ul>
                    <li>${app.url}</li>
                    <li>${app.sourcefiles}</li>
                    <li>${app.active?string("true", "false")}</li>
                </ul>
            </div>
        </div>
    </div>
</#list>
</div>
<div id="create-app-form"></div>

<script>
    $('#create-app-btn').on('click', function () {
        React.render(molgenis.ui.Form({
            entity: 'sys_App',
            modal: true,
            mode: 'create',
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('#create-app-form')[0]);
    });
</script>

<@footer/>