<form method="post" name="importWizardForm" enctype="multipart/form-data" action="">
		
<div  style="padding-top: 25px">
<div>
	Upload <a href="generated-doc/fileformat.html" target="_blank">Observ-OMX</a> data file<br /><br />
	<input type="file" name="upload" style="height:25px" >
</div>
<div style="padding-top: 25px">
<div class="accordion" id="accordion">
<div class="accordion-group">
<div class="accordion-heading">
<a class="accordion-toggle" data-toggle="collapse" href="#collapse1">Advanced options</a>
</div>
<div id="collapse1" class="accordion-body collapse">
<div class="accordion-inner">
<table style="width: 75%;">
	<tr>
		<td>
			<label>
				<input type="radio" style="margin:8px" name="entity_option" value="add_update" checked>
				Add entities / update existing
			</label>
		</td>
		<td>Importer adds new entities or updates existing entities</td>
	</tr>
	<tr>
		<td style="width: 30%;">
			<label>
				<input type="radio" style="margin:8px" name="entity_option" value="add">
				Add entities
			</label>
		</td>
		<td>Importer adds new entities or fails if entity exists</td>
	</tr>
	<tr>
		<td>
			<label>
				<input type="radio" style="margin:8px" name="entity_option" value="add_ignore">
				Add entities / ignore existing
			</label>
		</td>
		<td>Importer adds new entities or skips if entity exists</td>
	</tr>
	<tr>
		<td>
			<label>
				<input type="radio" style="margin:8px" name="entity_option" value="update">
				Update Entities
			</label>
		</td>
		<td>Importer updates existing entities or fails if entity does not exist</td>
	</tr>
	<tr>
		<td>
			<label>
				<input type="radio"style="margin:8px"  name="entity_option" value="update_ignore">
				Update Entities / ignore not existing
			</label>
		</td>
		<td>Importer updates existing entities or skips if entity does not exist</td>
	</tr>
</table>
</div>
</div>
</div>
</div>
</div>
</form>

<script>
$('#collapse1').collapse("hide");
</script>