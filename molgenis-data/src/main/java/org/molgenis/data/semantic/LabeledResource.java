package org.molgenis.data.semantic;

public class LabeledResource
{
	private final String iri;
	private final String label;

	public LabeledResource(String label)
	{
		this(null, label);
	}

	public LabeledResource(String iri, String label)
	{
		super();
		this.iri = iri;
		this.label = label;
	}

	public String getIri()
	{
		return iri;
	}

	public String getLabel()
	{
		return label;
	}

}
