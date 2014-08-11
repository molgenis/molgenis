package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.search.SearchService;

public class OntologyIndexRepository extends AbstractOntologyRepository
{
	private final OntologyLoader ontologyLoader;
	public final static String TYPE_ONTOLOGY = "indexedOntology";

	public OntologyIndexRepository(OntologyLoader loader, String name, SearchService searchService)
	{
		super(name, searchService);
		if (loader == null) throw new IllegalArgumentException("OntologyLoader is null!");
		ontologyLoader = loader;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();

		Entity entity = new MapEntity();
		entity.set(ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
		entity.set(ONTOLOGY_NAME, ontologyLoader.getOntologyName());
		entity.set(ENTITY_TYPE, TYPE_ONTOLOGY);
		entities.add(entity);

		return entities.iterator();
	}

	public long count()
	{
		return 1;
	}
}
