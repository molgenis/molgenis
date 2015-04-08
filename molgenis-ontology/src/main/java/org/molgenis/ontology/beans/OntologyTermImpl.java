package org.molgenis.ontology.beans;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class OntologyTermImpl implements OntologyTerm
{
	private final String iri;
	private final String label;
	private final String description;
	private final String termAccession;
	private final Ontology ontology;
	private final Set<String> synonyms;

	public OntologyTermImpl(String iri, String label, String description, String termAccession, Ontology ontology)
	{
		this.iri = iri;
		this.label = label;
		this.description = description;
		this.termAccession = termAccession;
		this.ontology = ontology;
		this.synonyms = new HashSet<String>();
	}

	public OntologyTermImpl(Entity ontologyTermEntity)
	{
		this.iri = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI);
		this.label = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME);
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
		// TODO : FIXME
		this.termAccession = StringUtils.EMPTY;
		this.description = StringUtils.EMPTY;
	}

	@Override
	public String getIRI()
	{
		return iri;
	}

	@Override
	public String getLabel()
	{
		return label;
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
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getTermAccession()
	{
		return termAccession;
	}

	@Override
	public String toString()
	{
		return label;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		if (label == null)
		{
			if (other.label != null) return false;
		}
		else if (!label.equals(other.label)) return false;
		return true;
	}

}
