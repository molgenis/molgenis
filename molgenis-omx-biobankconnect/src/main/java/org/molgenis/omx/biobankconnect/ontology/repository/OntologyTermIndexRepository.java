package org.molgenis.omx.biobankconnect.ontology.repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.TreeTraverser;

public class OntologyTermIndexRepository extends AbstractOntologyRepository implements Countable
{
	private final static String PSEUDO_ROOT_CLASS_LABEL = "top";
	private final OntologyLoader ontologyLoader;
	private final Set<String> dynamaticFields;

	@Autowired
	public OntologyTermIndexRepository(OntologyLoader loader, String entityName, SearchService searchService)
	{
		super(entityName, searchService);
		if (loader == null) throw new IllegalArgumentException("OntologyLoader is null!");
		ontologyLoader = loader;
		dynamaticFields = new HashSet<String>();
	}

	@Override
	public long count()
	{
		return ontologyLoader.count();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final TreeTraverser<OWLClassContainer> traverser = new TreeTraverser<OWLClassContainer>()
		{
			@Override
			public Iterable<OWLClassContainer> children(OWLClassContainer classContainer)
			{
				if (!classContainer.isOriginal()) return Collections.emptySet();

				Set<OWLClassContainer> orderedList = new HashSet<OWLClassContainer>();
				int count = 0;
				OWLClass parentOWLClass = classContainer.getOWLClass();
				for (OWLClass childClass : ontologyLoader.getChildClass(parentOWLClass))
				{
					// Not only the subClass is added to returned List, but also
					// synonym treated as OWLClass is added in the ordered List
					String definition = ontologyLoader.getDefinition(childClass);
					String parentOntologyTermIRI = parentOWLClass.getIRI().toString();
					String nodePath = constructNodePath(classContainer.getNodePath()) + "." + count;
					String parentNodePath = classContainer.getNodePath();
					Set<String> synonyms = ontologyLoader.getSynonyms(childClass);
					synonyms.add(ontologyLoader.getLabel(childClass));

					for (String synonym : synonyms)
					{
						orderedList.add(new OWLClassContainer(childClass, synonym, definition, nodePath,
								parentNodePath, parentOntologyTermIRI, false, ontologyLoader.getChildClass(childClass)
										.size() == 0, synonym.equals(ontologyLoader.getLabel(childClass)),
								createAlternativeDefinitions(childClass)));
					}
					count++;
				}
				return orderedList;
			}
		};

		return new Iterator<Entity>()
		{
			private final OWLClass pseudoRootClass = ontologyLoader.createClass(PSEUDO_ROOT_CLASS_LABEL,
					ontologyLoader.getRootClasses());
			private final Iterator<OWLClassContainer> iterator = traverser.preOrderTraversal(
					new OWLClassContainer(pseudoRootClass, PSEUDO_ROOT_CLASS_LABEL, null, "0", null, null, true, false,
							true, null)).iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				OWLClassContainer classContainer = iterator.next();
				OWLClass cls = classContainer.getOWLClass();
				Entity entity = new MapEntity();
				entity.set(ID, ontologyLoader.getId(cls));
				entity.set(NODE_PATH,
						classContainer.getNodePath().replaceAll(NODE_PATH_REPLACEMENT_PATTERN, StringUtils.EMPTY));
				entity.set(PARENT_NODE_PATH,
						classContainer.getParentNodePath().replaceAll(NODE_PATH_REPLACEMENT_PATTERN, StringUtils.EMPTY));
				entity.set(PARENT_ONTOLOGY_TERM_IRI, classContainer.getParentOntologyTermIRI());
				entity.set(ROOT, classContainer.isRoot());
				entity.set(LAST, classContainer.isLast());
				entity.set(ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
				entity.set(ONTOLOGY_NAME, ontologyLoader.getOntologyName());
				entity.set(ONTOLOGY_TERM, ontologyLoader.getLabel(cls));
				entity.set(ONTOLOGY_TERM_DEFINITION, classContainer.getClassDefinition());
				entity.set(ONTOLOGY_TERM_IRI, cls.getIRI().toString());
				entity.set(ENTITY_TYPE, TYPE_ONTOLOGYTERM);
				entity.set(
						SYNONYMS,
						classContainer.getClassLabel().replaceAll(ILLEGAL_CHARACTERS_PATTERN,
								ILLEGAL_CHARACTERS_REPLACEMENT));
				entity.set(ALTERNATIVE_DEFINITION, classContainer.getAssociatedClasses());
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private String constructNodePath(String parentNodePath)
	{
		StringBuilder nodePathStringBuilder = new StringBuilder();
		nodePathStringBuilder.append(parentNodePath).append('[').append(parentNodePath.split("\\.").length - 1)
				.append(']');
		return nodePathStringBuilder.toString();
	}

	/**
	 * A helper function to create alternative definitions stored in string for
	 * ontology terms.
	 * 
	 * @param cls
	 * @return
	 */
	private String createAlternativeDefinitions(OWLClass cls)
	{
		StringBuilder alternativeDefinitions = new StringBuilder();
		for (Set<OWLClass> alternativeDefinition : ontologyLoader.getAssociatedClasses(cls))
		{
			StringBuilder newDefinition = new StringBuilder();
			for (OWLClass associatedClass : alternativeDefinition)
			{
				if (newDefinition.length() != 0) newDefinition.append(AsyncOntologyMatcher.COMMON_SEPERATOR);
				newDefinition.append(associatedClass.getIRI().toString());
			}
			if (alternativeDefinitions.length() != 0 && newDefinition.length() != 0)
			{
				alternativeDefinitions.append(AsyncOntologyMatcher.ALTERNATIVE_DEFINITION_SEPERATOR);
			}
			alternativeDefinitions.append(newDefinition);
		}
		return alternativeDefinitions.toString();
	}

	public Set<String> getDynamaticFields()
	{
		return dynamaticFields;
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUrl()
	{
		throw new UnsupportedOperationException();
	}
}