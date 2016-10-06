package org.molgenis.ontology.core.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.ontology.core.model.ChildrenRetrievalParam;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;

public class OntologyServiceImpl implements OntologyService
{
	private final static Logger LOG = LoggerFactory.getLogger(OntologyServiceImpl.class);
	private OntologyRepository ontologyRepository;
	private OntologyTermRepository ontologyTermRepository;

	private LoadingCache<ChildrenRetrievalParam, Iterable<OntologyTerm>> cachedOntologyTermChildren = CacheBuilder
			.newBuilder().maximumSize(2000).expireAfterWrite(1, TimeUnit.HOURS)
			.build(new CacheLoader<ChildrenRetrievalParam, Iterable<OntologyTerm>>()
			{
				public Iterable<OntologyTerm> load(ChildrenRetrievalParam childrenRetrievalParam)
				{
					return ontologyTermRepository.getChildren(childrenRetrievalParam.getOntologyTerm(),
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
	public OntologyTerm getOntologyTerm(String iri)
	{
		return ontologyTermRepository.getOntologyTerm(iri);
	}

	@Override
	public List<OntologyTerm> getOntologyTerms(List<String> iris)
	{
		return ontologyTermRepository.getOntologyTerms(iris);
	}

	@Override
	public List<OntologyTerm> getAllOntologyTerms(String ontologyId)
	{
		return ontologyTermRepository.getAllOntologyTerms(ontologyId);
	}

	@Override
	public List<OntologyTerm> findExactOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTerm>newArrayList();
		}
		Set<String> stemmedTerms = terms.stream().map(Stemmer::stem).collect(toSet());
		List<OntologyTerm> collect = ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize).stream()
				.filter(ontologyTerm -> isOntologyTermExactMatch(stemmedTerms, ontologyTerm)).collect(toList());
		return collect;
	}

	@Override
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTerm>newArrayList();
		}
		return ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize);
	}

	@Override
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTerm> ontologyTermDomains)
	{
		if (null == terms || terms.isEmpty())
		{
			return Lists.<OntologyTerm>newArrayList();
		}
		return ontologyTermRepository.findOntologyTerms(ontologyIds, terms, pageSize, ontologyTermDomains);
	}

	@Override
	public Iterable<OntologyTerm> getAllParents(OntologyTerm ontologyTerm)
	{
		return getParents(ontologyTerm, Integer.MAX_VALUE);
	}

	@Override
	public Iterable<OntologyTerm> getParents(OntologyTerm ontologyTerm, int maxLevel)
	{
		return ontologyTermRepository.getParents(ontologyTerm, maxLevel);
	}

	@Override
	public Iterable<OntologyTerm> getAllChildren(OntologyTerm ontologyTerm)
	{
		try
		{
			return cachedOntologyTermChildren.get(ChildrenRetrievalParam.create(ontologyTerm, Integer.MAX_VALUE));
		}
		catch (ExecutionException e)
		{
			LOG.error(e.getMessage());
		}
		return emptyList();
	}

	@Override
	public Iterable<OntologyTerm> getChildren(OntologyTerm ontologyTerm, int maxLevel)
	{
		try
		{
			return cachedOntologyTermChildren.get(ChildrenRetrievalParam.create(ontologyTerm, maxLevel));
		}
		catch (ExecutionException e)
		{
			LOG.error(e.getMessage());
		}
		return emptyList();
	}

	@Override
	public Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2)
	{
		return ontologyTermRepository.getOntologyTermDistance(ontologyTerm1, ontologyTerm2);
	}

	@Override
	public Double getOntologyTermSemanticRelatedness(OntologyTerm ontologyTerm1,
			OntologyTerm ontologyTerm2)
	{
		return ontologyTermRepository.getOntologyTermSemanticRelatedness(ontologyTerm1, ontologyTerm2);
	}

	@Override
	public boolean related(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2, int stopLevel)
	{
		return ontologyTermRepository.related(ontologyTerm1, ontologyTerm2, stopLevel);
	}

	@Override
	public boolean areWithinDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2,
			int maxDistance)
	{
		return ontologyTermRepository.areWithinDistance(ontologyTerm1, ontologyTerm2, maxDistance);
	}

	@Override
	public boolean isDescendant(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2)
	{
		if (ontologyTerm1.getNodePaths().isEmpty() || ontologyTerm2.getNodePaths().isEmpty())
		{
			return false;
		}

		return ontologyTerm1.getNodePaths().stream().anyMatch(
				targetNodePath -> ontologyTerm2.getNodePaths().stream()
						.anyMatch(sourceNodePath -> targetNodePath.contains(sourceNodePath)));
	}

	@Override
	public List<SemanticType> getAllSemanticTypes()
	{
		return ontologyTermRepository.getAllSemanticType();
	}

	private boolean isOntologyTermExactMatch(Set<String> terms, OntologyTerm ontologyTerm)
	{
		List<String> synonyms = Lists.newArrayList(ontologyTerm.getSynonyms());
		synonyms.add(ontologyTerm.getLabel());
		return synonyms.stream().anyMatch(synonym -> terms.containsAll(splitAndStem(synonym.toString())));
	}
}