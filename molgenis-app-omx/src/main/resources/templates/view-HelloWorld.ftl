<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header />
<form method="POST" action="${context_url}/2">
<input type="text" name="voornaam" class="required">
<button type="submit">submit</button>
</form>

<div>Hello ${naam}</div>
<@footer />
