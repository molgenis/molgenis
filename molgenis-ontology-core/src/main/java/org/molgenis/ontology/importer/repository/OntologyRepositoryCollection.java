package org.molgenis.ontology.importer.repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeTraverser;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.utils.OWLClassContainer;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM;

/**
 * RepositoryCollection for the import of an owl file.
 * <p>
 * Reads the owl file's contents using an {@link OntologyLoader}. Fills {@link InMemoryRepository}s with their contents.
 */
public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	private static final String PSEUDO_ROOT_CLASS_NODEPATH = "0[0]";
	private final static String PSEUDO_ROOT_CLASS_LABEL = "top";

	private final File file;
	private final String fileName;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private OntologyFactory ontologyFactory;

	@Autowired
	private OntologyTermNodePathFactory ontologyTermNodePathFactory;

	@Autowired
	private OntologyTermDynamicAnnotationFactory ontologyTermDynamicAnnotationFactory;

	@Autowired
	private OntologyTermSynonymFactory ontologyTermSynonymFactory;

	@Autowired
	private OntologyTermFactory ontologyTermFactory;

	// repositories
	private Repository<Entity> ontologyRepository;
	private Repository<Entity> nodePathRepository;
	private Repository<Entity> ontologyTermRepository;
	private Repository<Entity> annotationRepository;
	private Repository<Entity> synonymRepository;
	private Map<String, Repository<Entity>> repositories;

	private OntologyLoader loader;
	private Multimap<String, OntologyTermNodePath> nodePathsPerOntologyTerm = ArrayListMultimap.create();
	private Ontology ontologyEntity;

	/**
	 * Creates a new {@link OntologyRepositoryCollection} for an ontology file
	 *
	 * @param file the ontology file
	 */
	public OntologyRepositoryCollection(File file)
	{
		super(OntologyFileExtensions.getOntology());
		this.file = requireNonNull(file);

		String name = file.getName();
		if (name.endsWith(OntologyFileExtensions.OBO_ZIP.toString()))
		{
			name = name.substring(0, name.lastIndexOf('.' + OntologyFileExtensions.OBO_ZIP.toString()))
					   .replace('.', '_');
		}
		else if (name.endsWith(OntologyFileExtensions.OWL_ZIP.toString()))
		{
			name = name.substring(0, name.lastIndexOf('.' + OntologyFileExtensions.OWL_ZIP.toString()))
					   .replace('.', '_');
		}
		else
		{
			throw new IllegalArgumentException(format("Not a obo.zip or owl.zip file [%s]", file.getName()));
		}
		this.fileName = name;
	}

	@Override
	public void init() throws IOException
	{
		ontologyRepository = new InMemoryRepository(ontologyFactory.getEntityType());
		nodePathRepository = new InMemoryRepository(ontologyTermNodePathFactory.getEntityType());
		ontologyTermRepository = new InMemoryRepository(ontologyTermFactory.getEntityType());
		annotationRepository = new InMemoryRepository(ontologyTermDynamicAnnotationFactory.getEntityType());
		synonymRepository = new InMemoryRepository(ontologyTermSynonymFactory.getEntityType());
		repositories = ImmutableMap.of(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, annotationRepository, ONTOLOGY_TERM_SYNONYM,
				synonymRepository, ONTOLOGY_TERM_NODE_PATH, nodePathRepository, ONTOLOGY, ontologyRepository,
				ONTOLOGY_TERM, ontologyTermRepository);

		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		try
		{
			loader = new OntologyLoader(fileName, uploadedFiles.get(0));
		}
		catch (OWLOntologyCreationException e)
		{
			throw new IOException(e);
		}
		createOntology();
		createNodePaths();
		createOntologyTerms();
	}

	/**
	 * Initializes the {@link #ontologyEntity} and adds it to the {@link #ontologyRepository}.
	 */
	private void createOntology()
	{
		ontologyEntity = ontologyFactory.create();
		ontologyEntity.setId(idGenerator.generateId());
		ontologyEntity.setOntologyIri(loader.getOntologyIRI());
		ontologyEntity.setOntologyName(loader.getOntologyName());
		ontologyRepository.add(ontologyEntity);
	}

	/**
	 * Creates {@link OntologyTermNodePathMetaData} {@link Entity}s for an entire ontology tree and writes them to the
	 * {@link #nodePathsPerOntologyTerm} {@link Multimap}.
	 */
	private void createNodePaths()
	{
		TreeTraverser<OWLClassContainer> traverser = new TreeTraverser<OWLClassContainer>()
		{
			@Override
			public Iterable<OWLClassContainer> children(OWLClassContainer container)
			{
				int count = 0;
				List<OWLClassContainer> containers = new ArrayList<>();
				for (OWLClass childClass : loader.getChildClass(container.getOwlClass()))
				{
					containers.add(new OWLClassContainer(childClass, constructNodePath(container.getNodePath(), count),
							false));
					count++;
				}
				return containers;
			}
		};

		OWLClass pseudoRootClass = loader.createClass(PSEUDO_ROOT_CLASS_LABEL, loader.getRootClasses());

		for (OWLClassContainer container : traverser.preOrderTraversal(
				new OWLClassContainer(pseudoRootClass, PSEUDO_ROOT_CLASS_NODEPATH, true)))
		{
			OWLClass ontologyTerm = container.getOwlClass();
			String ontologyTermNodePath = container.getNodePath();
			String ontologyTermIRI = ontologyTerm.getIRI().toString();
			OntologyTermNodePath nodePathEntity = createNodePathEntity(container, ontologyTermNodePath);
			nodePathsPerOntologyTerm.put(ontologyTermIRI, nodePathEntity);
		}
	}

	/**
	 * Creates {@link OntologyTermMetaData} {@link Entity}s for all {@link OWLClass}ses in the {@link #loader} and adds
	 * them to the {@link #ontologyTermRepository}.
	 */
	private void createOntologyTerms()
	{
		loader.getAllclasses().forEach(this::createOntologyTerm);
	}

	/**
	 * Creates an {@link OntologyTermMetaData} {@link Entity} and adds it in the {@link #ontologyTermRepository}
	 *
	 * @param ontologyTermClass the OWLClass to create an entity for
	 * @return the created ontology term {@link Entity}
	 */
	private Entity createOntologyTerm(OWLClass ontologyTermClass)
	{
		String ontologyTermIRI = ontologyTermClass.getIRI().toString();
		String ontologyTermName = loader.getLabel(ontologyTermClass);
		OntologyTerm ontologyTerm = ontologyTermFactory.create();
		ontologyTerm.setId(idGenerator.generateId());
		ontologyTerm.setOntologyTermIri(ontologyTermIRI);
		ontologyTerm.setOntologyTermName(ontologyTermName);
		ontologyTerm.setOntologyTermSynonyms(createSynonyms(ontologyTermClass));
		ontologyTerm.setOntologyTermDynamicAnnotations(createDynamicAnnotations(ontologyTermClass));
		ontologyTerm.setOntologyTermNodePaths(nodePathsPerOntologyTerm.get(ontologyTermIRI));
		ontologyTerm.setOntology(ontologyEntity);
		ontologyTermRepository.add(ontologyTerm);
		return ontologyTerm;
	}

	/**
	 * Creates {@link OntologyTermSynonymMetaData} {@link Entity}s for an ontology term
	 *
	 * @param ontologyTerm {@link OWLClass} for the ontology term
	 * @return {@link List} of created synonym {@link Entity}s
	 */
	private List<OntologyTermSynonym> createSynonyms(OWLClass ontologyTerm)
	{
		return loader.getSynonyms(ontologyTerm).stream().map(this::createSynonym).collect(Collectors.toList());
	}

	/**
	 * Creates an {@link OntologyTermSynonymMetaData} {@link Entity} and adds it to the {@link #synonymRepository}.
	 *
	 * @param synonym String of the synonym to create an {@link Entity} for
	 * @return the created {@link Entity}
	 */
	private OntologyTermSynonym createSynonym(String synonym)
	{
		OntologyTermSynonym entity = ontologyTermSynonymFactory.create();
		entity.setId(idGenerator.generateId());
		entity.setOntologyTermSynonym(synonym);
		synonymRepository.add(entity);
		return entity;
	}

	/**
	 * Creates {@link OntologyTermDynamicAnnotationMetaData} {@link Entity}s for the databaseIds of an ontology term.
	 *
	 * @param term the term to create annotation entities for
	 * @return List of created {@link Entity}s.
	 */
	private List<OntologyTermDynamicAnnotation> createDynamicAnnotations(OWLClass term)
	{
		return loader.getDatabaseIds(term).stream().map(this::createDynamicAnnotation).collect(Collectors.toList());
	}

	/**
	 * Creates an {@link OntologyTermDynamicAnnotationMetaData} {@link Entity} for a key:value label.
	 *
	 * @param label the key:value label
	 * @return the {@link Entity}
	 */
	private OntologyTermDynamicAnnotation createDynamicAnnotation(String label)
	{
		OntologyTermDynamicAnnotation entity = ontologyTermDynamicAnnotationFactory.create();
		entity.setId(idGenerator.generateId());
		String fragments[] = label.split(":");
		entity.setName(fragments[0]);
		entity.setValue(fragments[1]);
		entity.setLabel(label);
		annotationRepository.add(entity);
		return entity;
	}

	/**
	 * Constructs the node path string for a child node
	 *
	 * @param parentNodePath  node path string of the node's parent
	 * @param currentPosition position of the node in the parent's child list
	 * @return node path string
	 */
	private String constructNodePath(String parentNodePath, int currentPosition)
	{
		StringBuilder nodePathStringBuilder = new StringBuilder();
		if (!StringUtils.isEmpty(parentNodePath)) nodePathStringBuilder.append(parentNodePath).append('.');
		nodePathStringBuilder.append(currentPosition)
							 .append('[')
							 .append(nodePathStringBuilder.toString().split("\\.").length - 1)
							 .append(']');
		return nodePathStringBuilder.toString();
	}

	/**
	 * Creates a {@link OntologyTermNodePathMetaData} {@link Entity} and stores it in the {@link #nodePathRepository}.
	 *
	 * @param container                {@link OWLClassContainer} for the path to the ontology term
	 * @param ontologyTermNodePathText the node path
	 * @return the created {@link Entity}
	 */
	private OntologyTermNodePath createNodePathEntity(OWLClassContainer container, String ontologyTermNodePathText)
	{
		OntologyTermNodePath ontologyTermNodePath = ontologyTermNodePathFactory.create();
		ontologyTermNodePath.setId(idGenerator.generateId());
		ontologyTermNodePath.setNodePath(ontologyTermNodePathText);
		ontologyTermNodePath.setRoot(container.isRoot());
		nodePathRepository.add(ontologyTermNodePath);
		return ontologyTermNodePath;
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return repositories.keySet();
	}

	@Override
	public String getName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		if (!repositories.containsKey(name))
		{
			throw new MolgenisDataException(format("Unknown entity name [%s]", name));
		}
		return repositories.get(name);
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		for (String s : getEntityTypeIds())
		{
			if (s.equals(name)) return true;
		}
		return false;
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getId());
	}
}