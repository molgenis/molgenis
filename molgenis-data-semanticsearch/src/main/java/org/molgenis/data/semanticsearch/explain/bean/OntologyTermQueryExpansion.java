package org.molgenis.data.semanticsearch.explain.bean;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.service.OntologyService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

public class OntologyTermQueryExpansion
{
	private final List<OntologyTermImpl> ontologyTerms;
	private final OntologyService ontologyService;
	private Multimap<OntologyTermImpl, OntologyTermImpl> queryExpansionRelation;

	public OntologyTermQueryExpansion(List<OntologyTermImpl> ontologyTerms, OntologyService ontologyService)
	{
		this.ontologyTerms = requireNonNull(ontologyTerms);
		this.ontologyService = requireNonNull(ontologyService);
		this.queryExpansionRelation = LinkedHashMultimap.create();
		populate();
	}

	public List<OntologyTermImpl> getOntologyTerms()
	{
		return Lists.newArrayList(queryExpansionRelation.values());
	}

	public OntologyTermQueryExpansionSolution getQueryExpansionSolution(TagGroup tagGroup)
	{
		Map<OntologyTermImpl, OntologyTermImpl> matchedOntologyTerms = new LinkedHashMap<>();

		for (OntologyTermImpl sourceOntologyTerm : tagGroup.getOntologyTerms())
		{
			for (Entry<OntologyTermImpl, Collection<OntologyTermImpl>> entry : queryExpansionRelation.asMap()
					.entrySet())
			{
				if (entry.getValue().contains(sourceOntologyTerm))
				{
					matchedOntologyTerms.put(entry.getKey(), sourceOntologyTerm);
				}
			}
		}

		// If all the root ontology terms get matched, then the quality is high
		boolean highQuality = matchedOntologyTerms.size() == queryExpansionRelation.asMap().keySet().size();

		return OntologyTermQueryExpansionSolution.create(matchedOntologyTerms, highQuality);
	}

	private void populate()
	{
		for (OntologyTermImpl atomicOntologyTerm : ontologyTerms)
		{
			queryExpansionRelation.put(atomicOntologyTerm, atomicOntologyTerm);
			queryExpansionRelation.putAll(atomicOntologyTerm,
					ontologyService.getChildren(atomicOntologyTerm, DEFAULT_EXPANSION_LEVEL));
		}
	}
}
