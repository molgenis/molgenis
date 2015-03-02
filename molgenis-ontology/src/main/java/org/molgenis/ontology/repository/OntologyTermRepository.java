package org.molgenis.ontology.repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.util.ApplicationContextProvider;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OntologyTermRepository implements Repository
{
	private final OntologyLoader ontologyLoader;
	private final DataService dataService;
	private final UuidGenerator uuidGenerator;
	private final OntologyTermDynamicAnnotationRepository ontologyTermDynamicAnnotationRepo;
	private final OntologyTermSynonymRepository ontologyTermSynonymRepo;
	private final OntologyTermNodePathRepository ontologyTermNodePathRepository;
	private final Map<String, String> referenceIds = new HashMap<String, String>();

	public OntologyTermRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator,
			OntologyTermDynamicAnnotationRepository ontologyTermDynamicAnnotationRepo,
			OntologyTermSynonymRepository ontologyTermSynonymRepo,
			OntologyTermNodePathRepository ontologyTermNodePathRepository)
	{
		this.ontologyLoader = ontologyLoader;
		this.uuidGenerator = uuidGenerator;
		this.dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
		this.ontologyTermDynamicAnnotationRepo = ontologyTermDynamicAnnotationRepo;
		this.ontologyTermSynonymRepo = ontologyTermSynonymRepo;
		this.ontologyTermNodePathRepository = ontologyTermNodePathRepository;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			private final Iterator<OWLClass> iterator = ontologyLoader.getAllclasses().iterator();

			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			public Entity next()
			{
				OWLClass cls = iterator.next();
				String ontologyIRI = ontologyLoader.getOntologyIRI();
				String ontologyTermIRI = cls.getIRI().toString();
				String ontologyTermName = ontologyLoader.getLabel(cls);

				Map<String, Map<String, String>> referenceIds2 = ontologyTermSynonymRepo.getReferenceIds();

				Map<String, Map<String, String>> referenceIds3 = ontologyTermDynamicAnnotationRepo.getReferenceIds();

				Map<String, Map<String, String>> referenceIds4 = ontologyTermNodePathRepository.getReferenceIds();

				Collection<String> synonymIds = ontologyTermSynonymRepo.getReferenceIds().containsKey(ontologyTermIRI) ? referenceIds2
						.get(ontologyTermIRI).values() : Collections.emptySet();

				Collection<String> annotationIds = ontologyTermDynamicAnnotationRepo.getReferenceIds().containsKey(
						ontologyTermIRI) ? referenceIds3.get(ontologyTermIRI).values() : Collections.emptySet();

				Collection<String> nodePathIds = ontologyTermNodePathRepository.getReferenceIds().containsKey(
						ontologyTermIRI) ? referenceIds4.get(ontologyTermIRI).values() : Collections.emptySet();

				if (!referenceIds.containsKey(ontologyTermIRI))
				{
					referenceIds.put(ontologyTermIRI, uuidGenerator.generateId());
				}

				Entity entity = new MapEntity();
				entity.set(OntologyTermMetaData.ID, referenceIds.get(ontologyTermIRI));
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermIRI);
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, ontologyTermName);
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, getSynonymEntities(synonymIds));
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						getOntologyTermDynamicAnnotationEntities(annotationIds));
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, getNodePathEntities(nodePathIds));
				entity.set(OntologyTermMetaData.ONTOLOGY, getOntology(ontologyIRI));

				return entity;
			}
		};
	}

	private List<Entity> getNodePathEntities(Collection<String> nodePathIds)
	{
		Iterable<Entity> ontologyTermNodePathEntities = dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
				new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, nodePathIds));

		if (Iterables.size(ontologyTermNodePathEntities) != nodePathIds.size()) throw new IllegalArgumentException(
				"The expected number of synonym (" + nodePathIds.size() + ") is different from actual size ("
						+ Iterables.size(nodePathIds) + ")");

		return Lists.newArrayList(ontologyTermNodePathEntities);
	}

	private List<Entity> getOntologyTermDynamicAnnotationEntities(Collection<String> annotationIds)
	{
		Iterable<Entity> ontologyTermDynamicAnnotationEntities = dataService.findAll(
				OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
				new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, annotationIds));

		if (Iterables.size(ontologyTermDynamicAnnotationEntities) != annotationIds.size()) throw new IllegalArgumentException(
				"The expected number of synonym (" + annotationIds.size() + ") is different from actual size ("
						+ Iterables.size(ontologyTermDynamicAnnotationEntities) + ")");

		return Lists.newArrayList(ontologyTermDynamicAnnotationEntities);
	}

	private List<Entity> getSynonymEntities(Collection<String> synonymIds)
	{
		Iterable<Entity> ontologyTermSynonymEntities = dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
				new QueryImpl().in(OntologyMetaData.ID, synonymIds));

		if (Iterables.size(ontologyTermSynonymEntities) != synonymIds.size()) throw new IllegalArgumentException(
				"The expected number of synonym (" + synonymIds.size() + ") is different from actual size ("
						+ Iterables.size(ontologyTermSynonymEntities) + ")");

		return Lists.newArrayList(ontologyTermSynonymEntities);
	}

	private Entity getOntology(String ontologyIRI)
	{
		Entity ontologyEntity = dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIRI));

		if (ontologyEntity == null) throw new IllegalArgumentException("Ontology " + ontologyIRI
				+ " does not exist in the database!");

		return ontologyEntity;
	}

	@Override
	public void close() throws IOException
	{
		// Do nothing
	}

	@Override
	public String getName()
	{
		return OntologyTermMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyTermMetaData.getEntityMetaData();
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
