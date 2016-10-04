package org.molgenis.ontology.core.service.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.ontology.core.model.ChildrenRetrievalParam;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class OntologyServiceImpl implements OntologyService
{
	private final static Logger LOG = LoggerFactory.getLogger(OntologyServiceImpl.class);
	private OntologyRepository ontologyRepository;
	private OntologyTermRepository ontologyTermRepository;

	private LoadingCache<ChildrenRetrievalParam, Iterable<OntologyTermImpl>> cachedOntologyTermChildren = CacheBuilder
			.newBuilder().maximumSize(2000).expireAfterWrite(1, TimeUnit.HOURS)
			.build(new CacheLoader<ChildrenRetrievalParam, Iterable<OntologyTermImpl>>()
			{
				public Iterable<OntologyTermImpl> load(ChildrenRetrievalParam childrenRetrievalParam)
				{
					return ontologyTermRepository.getChildren(childrenRetrievalParam.getOntologyTermImpl(),
							childrenRetrievalParam.getMaxLevel());
				}
			});

	@Autowired
	public OntologyServiceImpl(OntologyRepository ontologyRepository, OntologyTermRepository ontologyTermRepository)
	{
		this.ontologyRepository = requireNonNull(ontologyRepository);
		this.ontologyTermRepository = requireNonNull(ontologyTermRepository);
	}

	@Override
	public List<Ontology> getOntologies()
	{
		return ontologyRepository.getOntologies().collect(toList());
	}

	@Override
	public List<String> getAllOntologiesIds()
	{
		final List<String> allOntologiesIds = new ArrayList<String>();
		ontologyRepository.getOntologies().forEach(e -> allOntologiesIds.add(e.getId()));
		return allOntologiesIds;
	}

	@Override
	public Ontology getOntology(String name)
	{
		return ontologyRepository.getOntology(name);
	}

	@Override
	public OntologyTermImpl getOntologyTerm(String iri)
	{
		return ontologyTermRepository.getOntologyTerm(iri);
	}

	@Override
	public List<OntologyTermImpl> getOntologyTerms(List<String> iris)
	{
		return ontologyTermRepository.getOntologyTerms(iris);
	}

	@Override
	public List<OntologyTermImpl> getAllOntologyTerms(String ontologyIri)
	{
		return ontologyTermRepository.getAllOntologyTerms(ontologyIri);
	}

	@Override
	public List<OntologyTermImpl> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTermImpl> newArrayList();
		}
		Set<String> stemmedTerms = terms.stream().map(Stemmer::stem).collect(toSet());
		List<OntologyTermImpl> collect = ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize).stream()
				.filter(ontologyTerm -> isOntologyTermExactMatch(stemmedTerms, ontologyTerm)).collect(toList());
		return collect;
	}

	@Override
	public List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTermImpl> newArrayList();
		}
		return ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize);
	}

	@Override
	public List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTermImpl> ontologyTermDomains)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTermImpl> newArrayList();
		}
		return ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize, ontologyTermDomains);
	}

	@Override
	public Iterable<OntologyTermImpl> getAllParents(OntologyTermImpl ontologyTermImpl)
	{
		return getParents(ontologyTermImpl, Integer.MAX_VALUE);
	}

	@Override
	public Iterable<OntologyTermImpl> getParents(OntologyTermImpl ontologyTermImpl, int maxLevel)
	{
		return getParents(ontologyTermImpl, maxLevel);
	}

	@Override
	public Iterable<OntologyTermImpl> getAllChildren(OntologyTermImpl ontologyTermImpl)
	{
		try
		{
			return cachedOntologyTermChildren.get(ChildrenRetrievalParam.create(ontologyTermImpl, Integer.MAX_VALUE));
		}
		catch (ExecutionException e)
		{
			LOG.error(e.getMessage());
		}
		return emptyList();
	}

	@Override
	public Iterable<OntologyTermImpl> getChildren(OntologyTermImpl ontologyTermImpl, int maxLevel)
	{
		try
		{
			return cachedOntologyTermChildren.get(ChildrenRetrievalParam.create(ontologyTermImpl, maxLevel));
		}
		catch (ExecutionException e)
		{
			LOG.error(e.getMessage());
		}
		return emptyList();
	}

	@Override
	public Integer getOntologyTermDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2)
	{
		return ontologyTermRepository.getOntologyTermDistance(ontologyTermImpl1, ontologyTermImpl2);
	}

	@Override
	public Double getOntologyTermSemanticRelatedness(OntologyTermImpl ontologyTermImpl1,
			OntologyTermImpl ontologyTermImpl2)
	{
		return ontologyTermRepository.getOntologyTermSemanticRelatedness(ontologyTermImpl1, ontologyTermImpl2);
	}

	@Override
	public boolean related(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2, int stopLevel)
	{
		return ontologyTermRepository.related(ontologyTermImpl1, ontologyTermImpl2, stopLevel);
	}

	@Override
	public boolean areWithinDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2,
			int maxDistance)
	{
		return ontologyTermRepository.areWithinDistance(ontologyTermImpl1, ontologyTermImpl2, maxDistance);
	}

	@Override
	public boolean isDescendant(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2)
	{
		if (ontologyTermImpl1.getNodePaths().isEmpty() || ontologyTermImpl2.getNodePaths().isEmpty())
		{
			return false;
		}

		return ontologyTermImpl1.getNodePaths().stream().anyMatch(targetNodePath -> ontologyTermImpl2.getNodePaths()
				.stream().anyMatch(sourceNodePath -> targetNodePath.contains(sourceNodePath)));
	}

	@Override
	public List<SemanticType> getAllSemanticTypes()
	{
		return ontologyTermRepository.getAllSemanticType();
	}

	private boolean isOntologyTermExactMatch(Set<String> terms, OntologyTermImpl ontologyTermImpl)
	{
		List<String> synonyms = Lists.newArrayList(ontologyTermImpl.getSynonyms());
		synonyms.add(ontologyTermImpl.getLabel());
		return synonyms.stream().anyMatch(synonym -> terms.containsAll(splitAndStem(synonym.toString())));
	}
}