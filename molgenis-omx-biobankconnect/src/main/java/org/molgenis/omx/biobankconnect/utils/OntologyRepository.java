package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class OntologyRepository extends AbstractRepository implements Countable
{
	private final OntologyLoader loader;
	private final String name;
	public final static String ONTOLOGY_URL = "ontologyIRI";
	public final static String ENTITY_TYPE = "entity_type";
	public final static String ONTOLOGY_LABEL = "ontologyLabel";
	public final static String TYPE_ONTOLOGY = "indexedOntology";

	public OntologyRepository(OntologyLoader loader, String name)
	{
		super("ontology://" + name);
		this.loader = loader;
		this.name = name;
	}

	@Override
	public long count()
	{
		return 1;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();

		Entity entity = new MapEntity();
		entity.set(ONTOLOGY_URL, loader.getOntologyIRI());
		entity.set(ONTOLOGY_LABEL, loader.getOntologyName());
		entity.set(ENTITY_TYPE, TYPE_ONTOLOGY);
		entities.add(entity);

		return entities.iterator();
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData(name, MapEntity.class);
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_URL));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_LABEL));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE));

		return metaData;
	}
}
