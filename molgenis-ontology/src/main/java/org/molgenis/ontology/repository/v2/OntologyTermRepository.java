package org.molgenis.ontology.repository.v2;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

public class OntologyTermRepository implements Repository
{
	private final static String PSEUDO_ROOT_CLASS_LABEL = "top";
	private final OntologyLoader ontologyLoader;
	private final DataService dataService;

	public OntologyTermRepository(OntologyLoader ontologyLoader, DataService dataService)
	{
		this.ontologyLoader = ontologyLoader;
		this.dataService = dataService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final TreeTraverser<OWLClass> traverser = new TreeTraverser<OWLClass>()
		{
			@Override
			public Iterable<OWLClass> children(OWLClass owlClass)
			{
				return ontologyLoader.getChildClass(owlClass);
			}
		};

		return new Iterator<Entity>()
		{
			// Since there are multiple root classes, in order to use tree
			// traverse function from Guava, a psudoRoot class is created to
			// hold all the real root classes
			private final OWLClass pseudoRootClass = ontologyLoader.createClass(PSEUDO_ROOT_CLASS_LABEL,
					ontologyLoader.getRootClasses());
			private final Iterator<OWLClass> iterator = traverser.preOrderTraversal(pseudoRootClass).iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				OWLClass cls = iterator.next();
				String ontologyIRI = ontologyLoader.getOntologyIRI();
				String ontologyTermIRI = cls.getIRI().toString();

				Set<String> synonymIds = FluentIterable.from(ontologyLoader.getSynonyms(cls))
						.transform(new Function<String, String>()
						{
							public String apply(String synonym)
							{
								return OntologyRepositoryCollection.createUniqueId(ontologyIRI, ontologyTermIRI,
										synonym);
							}
						}).toSet();
				Set<String> annotationIds = FluentIterable.from(ontologyLoader.getDatabaseIds(cls))
						.transform(new Function<String, String>()
						{
							public String apply(String synonym)
							{
								return OntologyRepositoryCollection.createUniqueId(ontologyIRI, ontologyTermIRI,
										synonym);
							}
						}).toSet();

				Entity entity = new MapEntity();
				entity.set(OntologyTermMetaData.ID,
						OntologyRepositoryCollection.createUniqueId(ontologyIRI, ontologyTermIRI, StringUtils.EMPTY));
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermIRI);
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, ontologyLoader.getLabel(cls));

				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, getSynonymEntities(synonymIds));
				entity.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						getOntologyTermDynamicAnnotationEntities(annotationIds));
				entity.set(OntologyTermMetaData.ONTOLOGY, getOntology(ontologyIRI));

				return entity;
			}
		};
	}

	private List<Entity> getOntologyTermDynamicAnnotationEntities(Set<String> annotationIds)
	{
		Iterable<Entity> ontologyTermDynamicAnnotationEntities = dataService.findAll(
				OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
				new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, annotationIds));

		if (Iterables.size(ontologyTermDynamicAnnotationEntities) != annotationIds.size()) throw new IllegalArgumentException(
				"The expected number of synonym (" + annotationIds.size() + ") is different from actual size ("
						+ Iterables.size(ontologyTermDynamicAnnotationEntities) + ")");

		return Lists.newArrayList(ontologyTermDynamicAnnotationEntities);
	}

	private List<Entity> getSynonymEntities(Set<String> synonymIds)
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
