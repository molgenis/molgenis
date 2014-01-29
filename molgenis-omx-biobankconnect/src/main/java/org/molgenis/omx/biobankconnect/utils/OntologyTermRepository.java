package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
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
	private final OntologyLoader loader;
	private final String ontologyIRI;
	private final String ontologyName;
	private final String name;
	private final static String NODE_PATH = "nodePath";
	private final static String BOOST = "boost";
	private final static String ONTOLOGY_IRI = "ontologyIRI";
	private final static String ONTOLOGY_NAME = "ontologyName";
	private final static String ONTOLOGY_TERM = "ontologyTerm";
	private final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private final static String SYNONYMS = "ontologyTermSynonym";
	private final static String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private final static String ONTOLOGY_LABEL = "ontologyLabel";
	private final static String ENTITY_TYPE = "entity_type";

	public OntologyTermRepository(OntologyLoader loader, String name)
	{
		this.loader = loader;
		this.ontologyName = this.loader.getOntologyName();
		this.ontologyIRI = this.loader.getOntologyIRI();
		this.name = name;
	}

	@Override
	public long count()
	{
		List<Entity> entities = new ArrayList<Entity>();
		createOntologyTable(entities, loader);

		return entities.size();
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();
		createOntologyTable(entities, loader);

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
		DefaultEntityMetaData metaData = new DefaultEntityMetaData(name);
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(NODE_PATH, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(BOOST, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_LABEL, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(SYNONYMS, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE, FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(ALTERNATIVE_DEFINITION, FieldTypeEnum.STRING));

		return metaData;
	}

	private void createOntologyTable(List<Entity> entities, OntologyLoader model)
	{
		int count = 0;
		for (OWLClass subClass : model.getTopClasses())
		{
			recursiveAddEntity("0." + count, subClass, model, entities);
			count++;
		}
	}

	private void recursiveAddEntity(String termPath, OWLClass cls, OntologyLoader ontologyLoader, List<Entity> entities)
	{
		String label = ontologyLoader.getLabel(cls).replaceAll("[^a-zA-Z0-9 ]", " ");
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
		if (alternativeDefinitions.length() != 0)
		{
			System.out.println(alternativeDefinitions.toString());
		}
		for (String synonym : synonyms)
		{
			Entity entity = new MapEntity();
			entity.set(NODE_PATH, termPath);
			entity.set(BOOST, false);
			entity.set(ONTOLOGY_IRI, ontologyIRI);
			entity.set(ONTOLOGY_NAME, ontologyName);
			entity.set(ONTOLOGY_TERM, label);
			entity.set(ONTOLOGY_TERM_IRI, cls.getIRI().toString());
			entity.set(ONTOLOGY_LABEL, ontologyLoader.getOntologyName());
			entity.set(ENTITY_TYPE, "ontologyTerm");
			entity.set(SYNONYMS, synonym.replaceAll("[^a-zA-Z0-9 ]", " "));
			entity.set(ALTERNATIVE_DEFINITION, alternativeDefinitions.toString());
			entities.add(entity);
		}

		Set<OWLClass> listOfChildren = ontologyLoader.getChildClass(cls);
		if (listOfChildren.size() > 0)
		{
			int i = 0;
			for (OWLClass childClass : listOfChildren)
			{
				String childTermPath = termPath + "." + i;
				recursiveAddEntity(childTermPath, childClass, ontologyLoader, entities);
				i++;
			}
		}
	}
}
