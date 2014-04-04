package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.search.Hit;

class OntologyMetaResponse
{
	private final String fieldType;
	private final String name;
	private final String label;
	private final String ontologyUrl;
	private final String description;
	private List<OntologyTermMetaResponse> attributes;

	public OntologyMetaResponse(Hit hit)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		name = columnValueMap.containsKey(OntologyRepository.ONTOLOGY_URL) ? columnValueMap.get(
				OntologyRepository.ONTOLOGY_URL).toString() : null;
		ontologyUrl = columnValueMap.containsKey(OntologyRepository.ONTOLOGY_URL) ? columnValueMap.get(
				OntologyRepository.ONTOLOGY_URL).toString() : null;
		label = columnValueMap.containsKey(OntologyRepository.ONTOLOGY_LABEL) ? columnValueMap.get(
				OntologyRepository.ONTOLOGY_LABEL).toString() : null;

		fieldType = FieldTypeEnum.COMPOUND.toString();
		description = StringUtils.EMPTY;
		attributes = new ArrayList<OntologyTermMetaResponse>();
	}

	public void addAttribute(OntologyTermMetaResponse attribute)
	{
		attributes.add(attribute);
	}

	public List<OntologyTermMetaResponse> getAttributes()
	{
		return attributes;
	}

	public String getFieldType()
	{
		return fieldType;
	}

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public String getOntologyUrl()
	{
		return ontologyUrl;
	}
}