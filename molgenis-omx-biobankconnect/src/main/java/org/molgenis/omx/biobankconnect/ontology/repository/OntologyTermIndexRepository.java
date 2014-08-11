package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyTermIndexRepository extends AbstractOntologyRepository implements Countable
{
	private final OntologyLoader ontologyLoader;
	private final String ontologyIRI;
	private final String ontologyName;

	@Autowired
	public OntologyTermIndexRepository(OntologyLoader loader, String name, SearchService searchService)
	{
		super(name, searchService);
		this.ontologyLoader = loader;
		if (ontologyLoader != null)
		{
			this.ontologyName = ontologyLoader.getOntologyName();
			this.ontologyIRI = ontologyLoader.getOntologyIRI();
		}
		else
		{
			ontologyName = name;
			ontologyIRI = name;
		}
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
