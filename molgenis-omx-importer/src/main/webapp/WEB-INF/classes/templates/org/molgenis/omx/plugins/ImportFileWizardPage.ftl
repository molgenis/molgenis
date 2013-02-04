<table style="width: 75%;">
	<tr>
		<td style="width: 30%;">
			<label>
				<input type="radio" style="margin:8px" name="entity_option" value="add" checked>
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
				<input type="radio" style="margin:8px" name="entity_option" value="add_update">
				Add entities / update existing
			</label>
		</td>
		<td>Importer adds new entities or updates existing entities</td>
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