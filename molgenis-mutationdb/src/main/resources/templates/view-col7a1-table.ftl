<table class="molgenis-table-striped table-hover table-condensed molgenis-table">
	<thead>
		<tr>
			<#if headers?? == true>
				<#list headers as header>
					<th>${header?html}</th>
				</#list>
			</#if>
		</tr>
	</thead>
	<tbody>
		<#if rows?? == true>
			<#list rows as row>
				<tr>
					<#if (row.cells)?? == true>
						<#list row.cells as cell>
							<td>
								<#if (cell.values)?? == true>
									<#list cell.values as value>
										<div>${value.value?html}</br></div>
									</#list>
								</#if>
							</td>
						</#list>
					</#if>
				</tr>
			</#list>
		</#if>
	</tbody>
</table>
