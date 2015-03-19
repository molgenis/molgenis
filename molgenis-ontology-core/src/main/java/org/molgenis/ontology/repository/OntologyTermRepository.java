package org.molgenis.ontology.repository;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public class OntologyTermRepository
{
	private DataService dataService;

	public List<OntologyTerm> findOntologyTerm(List<Ontology> ontologies, String search)
	{
		return null;
		// return Lists.newArrayList(dataService.findAll(ENTITY_NAME,
		// IN(ONTOLOGY, ontologies.stream().map(Ontology::getId).map(this::toOntologyTerm).collect(toList())));
	}

	private OntologyTerm toOntologyTerm(Entity entity)
	{
		return null;
	}
}
