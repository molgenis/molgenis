package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;

public class OntologyTermMetaResponse
{
	private final String fieldType;
	private final String ontologyUrl;
	private final String nodePath;
	private final String definition;
	private final String name;
	private final String label;
	private Set<String> synonyms;
	private List<OntologyTermMetaResponse> attributes;

	public OntologyTermMetaResponse(Hit hit)
	{
		Map<String, Object> columnValueMap = hit.getColumnValueMap();

		name = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_TERM_IRI) ? columnValueMap.get(
				OntologyTermRepository.ONTOLOGY_TERM_IRI).toString() : null;
		nodePath = columnValueMap.containsKey(OntologyTermRepository.NODE_PATH) ? columnValueMap.get(
				OntologyTermRepository.NODE_PATH).toString() : null;
		ontologyUrl = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_IRI) ? columnValueMap.get(
				OntologyTermRepository.ONTOLOGY_IRI).toString() : null;
		definition = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_TERM_DEFINITION) ? columnValueMap.get(
				OntologyTermRepository.ONTOLOGY_TERM_DEFINITION).toString() : null;

		String ontologyTermLabel = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_TERM) ? columnValueMap
				.get(OntologyTermRepository.ONTOLOGY_TERM).toString() : null;

		if (ontologyTermLabel == null || ontologyTermLabel.isEmpty())
		{
			String fragments[] = name.split("[*/]");
			ontologyTermLabel = fragments[fragments.length - 1];
		}

		label = ontologyTermLabel;

		if (columnValueMap.containsKey(OntologyTermRepository.LAST))
		{
			if (Boolean.parseBoolean(columnValueMap.get(OntologyTermRepository.LAST).toString()))
			{
				fieldType = FieldTypeEnum.STRING.toString();
			}
			else
			{
				fieldType = FieldTypeEnum.COMPOUND.toString();
			}
		}
		else fieldType = null;
		attributes = new ArrayList<OntologyTermMetaResponse>();
		synonyms = new HashSet<String>();
	}

	public void addSynonyms(String synonym)
	{
		if (!synonym.equalsIgnoreCase(label) && !synonym.isEmpty())
		{
			synonyms.add(synonym);
		}
	}

	public void addAttribute(OntologyTermMetaResponse attribute)
	{
		attributes.add(attribute);
	}

	public List<OntologyTermMetaResponse> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(List<OntologyTermMetaResponse> attributes)
	{
		this.attributes = attributes;
	}

	public String getFieldType()
	{
		return fieldType;
	}

	public String getOntologyUrl()
	{
		return ontologyUrl;
	}

	public String getNodePath()
	{
		return nodePath;
	}

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	public Set<String> getSynonyms()
	{
		return synonyms;
	}

	public String getDefinition()
	{
		return definition;
	}
}