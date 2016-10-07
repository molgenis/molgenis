package org.molgenis.ontology.core.repository;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.model.Ontology;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.stream.Stream;

import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_IRI;

/**
 * Maps OntologyMetaData {@link Entity} <-> {@link OntologyEntity}
 */
public class OntologyRepository
{
	@Autowired
	private DataService dataService;

	/**
	 * Retrieves all {@link OntologyEntity}s.
	 */
	public Stream<Ontology> getOntologies()
	{
		Stream<OntologyEntity> findAll = dataService.findAll(ONTOLOGY, OntologyEntity.class);
		return findAll.map(OntologyRepository::toOntology);
	}

	/**
	 * Retrieves an ontology with a specific IRI.
	 *
	 * @param IRI the IRI of the ontology
	 * @return
	 */
	public Ontology getOntology(String IRI)
	{
		OntologyEntity findOne = dataService
				.findOne(ONTOLOGY, new QueryImpl<OntologyEntity>().eq(ONTOLOGY_IRI, IRI), OntologyEntity.class);
		return Objects.nonNull(findOne) ? toOntology(findOne) : null;
	}

	public static Ontology toOntology(OntologyEntity ontologyEntity)
	{
		return Ontology.create(ontologyEntity.getId(), ontologyEntity.getIRI(), ontologyEntity.getOntologyName());
	}
}
