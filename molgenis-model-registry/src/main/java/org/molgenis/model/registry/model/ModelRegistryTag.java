package org.molgenis.model.registry.model;

/**
 * @author sido
 */
public class ModelRegistryTag
{

	private String label;
	private String relation;
	private String iri;

	public ModelRegistryTag(String label, String iri, String relation)
	{
		super();
		this.label = label;
		this.iri = iri;
		this.relation = relation;
	}

	@SuppressWarnings("unused")
	public String getLabel()
	{
		return label;
	}

	@SuppressWarnings("unused")
	public String getIri()
	{
		return iri;
	}

	@SuppressWarnings("unused")
	public String getRelation()
	{
		return relation;
	}

}
