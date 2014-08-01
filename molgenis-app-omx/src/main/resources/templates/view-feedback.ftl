<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div id="contact-molgenis">
	<div class="page-header">
		<h3>We&rsquo;re here to help with any questions or comments.<br/>
		<small>If you just want to say hi, that&rsquo;s cool too.</small>
	</div>
	<p class="muted"></p>
	<div class="container">
		<form accept-charset="UTF-8" action="/feedback" method="post">
			<fieldset>
				<p>
					Feel free to email us at <a href="mailto:molgenis@molgenis.org">molgenis@molgenis.org</a>
				</p>
				<label class="control-label" for="form_name">Name</label>
				<input
					class="input-xlarge" id="form_name" name="form[name]" size="30"
					type="text" />
				<label class="control-label" for="form_email">Email</label>
				<input class="input-xlarge" id="form_email" name="form[email]"
					size="30" type="email" />
				<label class="control-label"
					for="form_subject">Subject</label>
				<input class="input-xxlarge"
					id="form_subject" maxlength="72" name="form[subject]" size="72"
					type="text" />
				<label class="control-label" for="form_body">Body</label>
				<textarea class="input-xxlarge" id="form_comments"
					name="form[comments]"
					placeholder="If you use 140 characters or fewer, we&rsquo;ll give you a gold star."
					required="true" rows="10"></textarea>
			</fieldset>
			<button type="submit" class="btn btn-success">Send</button>
		</form>
	</div>
</div>
<@footer/>
