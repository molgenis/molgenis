package org.molgenis.ontology.repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.beans.OWLClassContainer;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.TreeTraverser;

public class OntologyTermIndexRepository extends AbstractOntologyRepository
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
		recursivelyFindDatabaseIds(ontologyLoader.getAllclasses());
	}

	private void recursivelyFindDatabaseIds(Set<OWLClass> owlClasses)
	{
		if (owlClasses != null)
		{
			for (OWLClass cls : owlClasses)
			{
				Map<String, Set<String>> allDatabaseIds = ontologyLoader.getAllDatabaseIds(cls);
				if (allDatabaseIds != null)
				{
					for (String databaseId : allDatabaseIds.keySet())
					{
						dynamaticFields.add(databaseId);
					}
				}
			}
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final TreeTraverser<OWLClassContainer> traverser = new TreeTraverser<OWLClassContainer>()
		{
			@Override
			public Iterable<OWLClassContainer> children(OWLClassContainer classContainer)
			{
				// In order to index the synonyms, ontology term entries are
				// duplicated for that purpose. However we only index the
				// ontology terms that are original.
				if (!classContainer.isLabel()) return Collections.emptySet();

				int count = 0;
				Set<OWLClassContainer> orderedList = new HashSet<OWLClassContainer>();
				OWLClass parentOWLClass = classContainer.getOWLClass();
				for (OWLClass childClass : ontologyLoader.getChildClass(parentOWLClass))
				{
					String definition = ontologyLoader.getDefinition(childClass);
					String parentOntologyTermIRI = parentOWLClass.getIRI().toString();
					String nodePath = constructNodePath(classContainer.getNodePath(), count);
					String parentNodePath = classContainer.getNodePath();
					Set<String> synonyms = ontologyLoader.getSynonyms(childClass);
					synonyms.add(ontologyLoader.getLabel(childClass));
					Map<String, Set<String>> allDatabaseIds = ontologyLoader.getAllDatabaseIds(childClass);

					// Not only the subClass is added to returned List, but also
					// synonym treated as OWLClass is added in the ordered List
					for (String synonym : synonyms)
					{
						orderedList.add(new OWLClassContainer(childClass, synonym, definition, nodePath,
								parentNodePath, parentOntologyTermIRI, false, ontologyLoader.getChildClass(childClass)
										.size() == 0, synonym.equals(ontologyLoader.getLabel(childClass)),
								createAlternativeDefinitions(childClass), allDatabaseIds));
					}

					count++;
				}
				return orderedList;
			}
		};
		return new Iterator<Entity>()
		{
			// Since there are multiple root classes, in order to use tree
			// traverse function from Guava, a psudoRoot class is created to
			// hold all the real root classes
			private final OWLClass pseudoRootClass = ontologyLoader.createClass(PSEUDO_ROOT_CLASS_LABEL,
					ontologyLoader.getRootClasses());
			private final Iterator<OWLClassContainer> iterator = traverser.preOrderTraversal(
					new OWLClassContainer(pseudoRootClass, PSEUDO_ROOT_CLASS_LABEL, null, "0[0]", null, null, true,
							false, true, null, null)).iterator();

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
				entity.set(ID, extractOWLClassId(cls));
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
				entity.set(SYNONYMS, classContainer.getClassLabel());
				entity.set(ALTERNATIVE_DEFINITION, classContainer.getAssociatedClasses());

				if (classContainer.getAllDatabaseIds() != null)
				{
					for (Entry<String, Set<String>> entry : classContainer.getAllDatabaseIds().entrySet())
					{
						entity.set(entry.getKey(), entry.getValue());
					}
				}
				return entity;
			}

			private String extractOWLClassId(OWLClass cls)
			{
				StringBuilder stringBuilder = new StringBuilder();
				String clsIri = cls.getIRI().toString();
				// Case where id is separated by #
				String[] split = null;
				if (clsIri.contains("#"))
				{
					split = clsIri.split("#");
				}
				else
				{
					split = clsIri.split("/");
				}
				stringBuilder.append(split[split.length - 1]);
				return stringBuilder.toString();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * A helper function to construct the nodePath with a pattern which is started with the position of current node
	 * compared to its siblings and followed by the depth of the tree such as 2[1]. Moreover it the parent node is
	 * prepended to the current node to construct complete path such as 0[0].2[1]
	 * 
	 * @param parentNodePath
	 * @return
	 */
	private String constructNodePath(String parentNodePath, int currentPosition)
	{
		StringBuilder nodePathStringBuilder = new StringBuilder();
		// If the parentNodePath is empty, that is the starting position of the
		// tree
		if (!StringUtils.isEmpty(parentNodePath)) nodePathStringBuilder.append(parentNodePath).append('.');
		// Create pattern of current node and add it to the path
		nodePathStringBuilder.append(currentPosition).append('[')
				.append(nodePathStringBuilder.toString().split("\\.").length - 1).append(']');
		return nodePathStringBuilder.toString();
	}

	/**
	 * A helper function to create alternative definitions stored in string for ontology terms.
	 * 
	 * @param cls
	 * @return
	 */
	private String createAlternativeDefinitions(OWLClass cls)
	{
		// TODO : it`s not important at the moment, but fix it in the future!
		StringBuilder alternativeDefinitions = new StringBuilder();
		// for (Set<OWLClass> alternativeDefinition :
		// ontologyLoader.getAssociatedClasses(cls))
		// {
		// StringBuilder newDefinition = new StringBuilder();
		// for (OWLClass associatedClass : alternativeDefinition)
		// {
		// if (newDefinition.length() != 0)
		// newDefinition.append(SemanticSearchServiceImpl.COMMON_SEPERATOR);
		// newDefinition.append(associatedClass.getIRI().toString());
		// }
		// if (alternativeDefinitions.length() != 0 && newDefinition.length() !=
		// 0)
		// {
		// alternativeDefinitions.append(SemanticSearchServiceImpl.ALTERNATIVE_DEFINITION_SEPERATOR);
		// }
		// alternativeDefinitions.append(newDefinition);
		// }
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
