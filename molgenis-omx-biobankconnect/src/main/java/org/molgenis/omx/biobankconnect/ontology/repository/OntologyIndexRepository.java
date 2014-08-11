package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.search.SearchService;

public class OntologyIndexRepository extends AbstractOntologyRepository
{
	private final OntologyLoader loader;
	public final static String TYPE_ONTOLOGY = "indexedOntology";

	public OntologyIndexRepository(OntologyLoader loader, String name, SearchService searchService)
	{
		super(name, searchService);
		this.loader = loader;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();

		Entity entity = new MapEntity();
		entity.set(ONTOLOGY_IRI, loader.getOntologyIRI());
		entity.set(ONTOLOGY_LABEL, loader.getOntologyName());
		entity.set(ENTITY_TYPE, TYPE_ONTOLOGY);
		entities.add(entity);

		return entities.iterator();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData(entityName, MapEntity.class);
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_LABEL));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE));
		return metaData;
	}

	public long count()
	{
		return 1;
	}
}
