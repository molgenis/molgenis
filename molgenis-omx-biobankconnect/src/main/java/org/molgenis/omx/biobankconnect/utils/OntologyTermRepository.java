package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermRepository extends AbstractRepository implements Countable
{
	private final OntologyLoader ontologyLoader;
	private final String ontologyIRI;
	private final String ontologyName;
	private final String name;
	private final static String ID = "id";
	public final static String NODE_PATH = "nodePath";
	public final static String PARENT_NODE_PATH = "parentNodePath";
	public final static String PARENT_ONTOLOGY_TERM_URL = "parentOntologyTermIRI";
	public final static String ROOT = "root";
	public final static String LAST = "isLast";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String ONTOLOGY_TERM = "ontologyTerm";
	public final static String ONTOLOGY_TERM_DEFINITION = "definition";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String SYNONYMS = "ontologyTermSynonym";
	public final static String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	public final static String ONTOLOGY_LABEL = "ontologyLabel";
	public final static String ENTITY_TYPE = "entity_type";
	public final static String TYPE_ONTOLOGYTERM = "ontologyTerm";
	public final static String CHIDLREN = "children";

	public OntologyTermRepository(OntologyLoader loader, String name)
	{
		super("ontologyterm://" + name);
		this.ontologyLoader = loader;
		if (this.ontologyLoader != null)
		{
			this.ontologyName = this.ontologyLoader.getOntologyName();
			this.ontologyIRI = this.ontologyLoader.getOntologyIRI();
		}
		else
		{
			this.ontologyName = name;
			this.ontologyIRI = name;
		}
		this.name = name;
	}

	@Override
	public long count()
	{
		List<Entity> entities = new ArrayList<Entity>();
		createOntologyTable(entities, ontologyLoader);

		return entities.size();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();
		createOntologyTable(entities, ontologyLoader);

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
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ID));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(NODE_PATH));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_NODE_PATH));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_ONTOLOGY_TERM_URL));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ROOT));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(LAST));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_DEFINITION));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_LABEL));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(SYNONYMS));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ALTERNATIVE_DEFINITION));

		return metaData;
	}

	private void createOntologyTable(List<Entity> entities, OntologyLoader model)
	{
		int count = 0;
		for (OWLClass subClass : model.getTopClasses())
		{
			recursiveAddEntity("0[0]." + count, null, subClass, entities, true);
			count++;
		}
	}

	private void recursiveAddEntity(String parentTermPath, String parentTermUrl, OWLClass cls, List<Entity> entities,
			boolean root)
	{
		String label = ontologyLoader.getLabel(cls).replaceAll("[^a-zA-Z0-9 ]", " ");
		String definition = ontologyLoader.getDefinition(cls);
		Set<OWLClass> listOfChildren = ontologyLoader.getChildClass(cls);
		Set<String> synonyms = new HashSet<String>();
		synonyms.add(label);
		synonyms.addAll(ontologyLoader.getSynonyms(cls));
		StringBuilder alternativeDefinitions = new StringBuilder();
		for (Set<OWLClass> alternativeDefinition : ontologyLoader.getAssociatedClasses(cls))
		{
			StringBuilder newDefinition = new StringBuilder();
			for (OWLClass associatedClass : alternativeDefinition)
			{
				if (newDefinition.length() != 0) newDefinition.append(',');
				newDefinition.append(associatedClass.getIRI().toString());
			}
			if (alternativeDefinitions.length() != 0 && newDefinition.length() != 0) alternativeDefinitions
					.append("&&&");
			alternativeDefinitions.append(newDefinition);
		}

		for (String synonym : synonyms)
		{
			Entity entity = new MapEntity();
			entity.set(ID, ontologyLoader.getId(cls));
			entity.set(NODE_PATH, parentTermPath + "[" + (parentTermPath.split("\\.").length - 1) + "]");
			entity.set(PARENT_NODE_PATH, parentTermPath.replaceAll("\\.[0-9]+$", ""));
			entity.set(PARENT_ONTOLOGY_TERM_URL, parentTermUrl);
			entity.set(ROOT, root);
			entity.set(LAST, listOfChildren.size() == 0);
			entity.set(ONTOLOGY_IRI, ontologyIRI);
			entity.set(ONTOLOGY_NAME, ontologyName);
			entity.set(ONTOLOGY_TERM, label);
			entity.set(ONTOLOGY_TERM_DEFINITION, definition);
			entity.set(ONTOLOGY_TERM_IRI, cls.getIRI().toString());
			entity.set(ONTOLOGY_LABEL, ontologyLoader.getOntologyName());
			entity.set(ENTITY_TYPE, TYPE_ONTOLOGYTERM);
			entity.set(SYNONYMS, synonym.replaceAll("[^a-zA-Z0-9 ]", " "));
			entity.set(ALTERNATIVE_DEFINITION, alternativeDefinitions.toString());
			entities.add(entity);
		}

		if (listOfChildren.size() > 0)
		{
			int i = 0;
			for (OWLClass childClass : listOfChildren)
			{
				int level = parentTermPath.split("\\.").length - 1;
				String childTermPath = parentTermPath + "[" + level + "]." + i;
				recursiveAddEntity(childTermPath, cls.getIRI().toString(), childClass, entities, false);
				i++;
			}
		}
	}
}
