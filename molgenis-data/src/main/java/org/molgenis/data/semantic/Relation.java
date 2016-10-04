package org.molgenis.data.semantic;

public enum Relation
{

	instanceOf("http://molgenis.org/biobankconnect/instanceOf"), link(
		"http://molgenis.org/biobankconnect/link"), homepage("http://xmlns.com/foaf/0.1/homepage"), isDefinedBy(
		"http://www.w3.org/2000/01/rdf-schema#isDefinedBy"), seeAlso(
		"http://www.w3.org/2000/01/rdf-schema#seeAlso"), hasLowerValue(
		"http://molgenis.org/uml/hasLowerValue"), hasUpperValue(
		"http://molgenis.org/uml/hasUpperValue"), isRealizationOf(
		"http://molgenis.org/uml/isRealizationOf"), isGeneralizationOf(
		"http://molgenis.org/uml/isGeneralizationOf"), hasSourceId(
		"http://molgenis.org/uml/hasSourceId"), hasSourceName(
		"http://molgenis.org/uml/hasSourceName"), isAssociatedWith("http://molgenis.org#isAssociatedWith");

	private String iri;

	Relation(String iri)
	{
		this.iri = iri;
	}

	public String getIRI()
	{
		return iri;
	}

	public String getLabel()
	{
		return toString();
	}

	public static Relation forIRI(String string)
	{
		for (Relation relation : values())
		{
			if (relation.getIRI().equals(string))
			{
				return relation;
			}
		}
		return null;
	}
}
