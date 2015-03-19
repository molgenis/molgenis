package org.molgenis.ontology.repository;

import static org.elasticsearch.common.collect.Iterables.transform;
import static org.molgenis.data.support.QueryImpl.IN;
import static org.molgenis.ontology.model.OntologyTermMetaData.ENTITY_NAME;
import static org.molgenis.ontology.model.OntologyTermMetaData.ONTOLOGY;

import java.util.List;

import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public class OntologyTermRepository
{
	private DataService dataService;

	public List<OntologyTerm> findOntologyTerms(List<Ontology> ontologies, String search)
	{
		Iterable<Entity> termEntities = dataService.findAll(ENTITY_NAME,
				new QueryImpl(IN(ONTOLOGY, transform(ontologies, Ontology::getId))).setPageSize(100));
		return Lists.newArrayList(Iterables.transform(termEntities, OntologyTermRepository::toOntologyTerm));
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		return OntologyTerm.create(entity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI),
				entity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
	}
}
