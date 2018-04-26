package org.molgenis.ontology.roc;

public class OntologyWord
{
	private final String ontologyIri;
	private final String word;

	public OntologyWord(String ontologyIri, String word)
	{
		this.ontologyIri = ontologyIri;
		this.word = word;
	}

	public String getOntologyIri()
	{
		return ontologyIri;
	}

	public String getWord()
	{
		return word;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ontologyIri == null) ? 0 : ontologyIri.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OntologyWord other = (OntologyWord) obj;
		if (ontologyIri == null)
		{
			if (other.ontologyIri != null) return false;
		}
		else if (!ontologyIri.equals(other.ontologyIri)) return false;
		if (word == null)
		{
			if (other.word != null) return false;
		}
		else if (!word.equals(other.word)) return false;
		return true;
	}
}
