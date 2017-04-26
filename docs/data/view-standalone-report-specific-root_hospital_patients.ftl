<div class="control-group form-horizontal">
	<table class="table">
		<tbody>
		<tr>
			<th>Name</th>
			<td>${entity.get('lastName')}, ${entity.get('firstName')}</td>
		</tr>
		<tr>
			<th>Gender</th>
			<td>${entity.get('gender').label}</td>
		</tr>
		<#if entity.get('birthdate')??>
			<tr>
				<th>Birthdate</th>
                <td>${entity.get('birthdate').format("MMM d, yyyy")}</td>
			</tr>
		</#if>
		 <#if entity.get('birthplace')??>
			<tr>
				<th>Birthplace</th>
				<td>${entity.get('birthplace').label}</td>
			</tr>
		</#if>
		<#if entity.get('diagnosis')?has_content>
		<tr>
			<th>Diagnosis</th>
			<td>
			<#list entity.get('diagnosis') as diagnosis>
				${diagnosis.disease}<br/>
			</#list>
			</td>
		</tr>
		</#if>
		<#if entity.get('children')?has_content>
		<tr>
			<th>Children</th>
			<td>
			<#list entity.get('children') as child>
				${child.get('lastName')}, ${child.get('firstName')}<br/>
			</#list>
			</td>
		</tr>
		</#if>
		</tbody>
	</table>
	<#if entity.get('lab_results')?has_content>
	<#assign lab_results=entity.get('lab_results')>
	<h4>Lab results</h4>
	<table class="table">
		<thead>
			<tr>
				<th>Sample type</th>
				<th>Erythrocyte sedimentation rate (mm/h)</th>
				<th>Hemoglobine (mM)</th>
				<th>Hematocrit (l/l)</th>
				<th>Iron (µM)</th>
				<th>Prostate specific antigen (mg/l)</th>
				<th>Proteins</th>
				<th>Thyroid</th>
				<th>Cholesterol (mM)</th>
			</tr>
		</thead>
		<tbody>
			<#list lab_results as lab>
				<tr>
					<td>
						${lab.get("sample_type").label}
					</td>
					<td>
						<#if lab.get("ESR")??>
							${lab.get("ESR")}
						</#if>
					</td>
					<td>
						<#if lab.get("HB")??>
							${lab.get("HB")}
						</#if>
					</td>
					<td>
						<#if lab.get("hematocrit")??>
							${lab.get("hematocrit")}
						</#if>
					</td>
					<td>
						<#if lab.get("iron")??>
							${lab.get("iron")}
						</#if>
					</td>
					<td>
						<#if lab.get("PSA")??>
							${lab.get("PSA")}
						</#if>
					</td>
					<td>
						<#if lab.get("proteins_plasma")??>
							In plasma: ${lab.get("proteins_plasma")}<br/>
						</#if>
						<#if lab.get("proteins_serum")??>
							In serum: ${lab.get("proteins_serum")}<br/>
						</#if>
					</td>
					<td>
						<#if lab.get("TSH")??>
							Thyroid specific hormone mU/l: ${lab.get("TSH")}<br/>
						</#if>
						<#if lab.get("FT4")??>
							Free T4 hormones (pM): ${lab.get("FT4")}<br/>
						</#if>
					</td>
					<td>
						<#if lab.get("cholesterol")??>
							${lab.get("cholesterol")}
						</#if>
					</td>
				</tr>
			</#list>
		</tbody>
	</table>
	</#if>
</div>