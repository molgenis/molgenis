package org.molgenis.ontology.beans;

import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class OntologyTermImpl implements OntologyTerm
{
	private final String iri;
	private final String name;
	private final Ontology ontology;
	private final Set<String> synonyms;

	public OntologyTermImpl(Entity ontologyTermEntity)
	{
		this.iri = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI);
		this.name = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME);
		this.ontology = new OntologyImpl(ontologyTermEntity.getEntity(OntologyTermMetaData.ONTOLOGY));
		this.synonyms = FluentIterable.from(ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM))
				.transform(new Function<Entity, String>()
				{
					@Override
					public String apply(Entity entity)
					{
						return entity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
					}
				}).toSet();
	}

	@Override
	public String getIRI()
	{
		return iri;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Set<String> getSynonyms()
	{
		return synonyms;
	}

	@Override
	public Ontology getOntology()
	{
		return ontology;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OntologyTermImpl other = (OntologyTermImpl) obj;
		if (iri == null)
		{
			if (other.iri != null) return false;
		}
		else if (!iri.equals(other.iri)) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}
}
