package org.molgenis.data.semanticsearch.explain.bean;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class OntologyTermQueryExpansion
{
	private final OntologyTerm ontologyTerm;
	private final OntologyService ontologyService;
	private Multimap<OntologyTerm, OntologyTerm> queryExpansionRelation;

	public OntologyTermQueryExpansion(OntologyTerm ontologyTerm, OntologyService ontologyService)
	{
		this.ontologyTerm = requireNonNull(ontologyTerm);
		this.ontologyService = requireNonNull(ontologyService);
		this.queryExpansionRelation = LinkedHashMultimap.create();
		populate();
	}

	public List<OntologyTerm> getOntologyTerms()
	{
		return Lists.newArrayList(queryExpansionRelation.values());
	}

	public OntologyTermQueryExpansionSolution getQueryExpansionSolution(TagGroup tagGroup)
	{
		Map<OntologyTerm, OntologyTerm> matchedOntologyTerms = new LinkedHashMap<>();

		for (OntologyTerm sourceOntologyTerm : tagGroup.getOntologyTerms())
		{
			for (Entry<OntologyTerm, Collection<OntologyTerm>> entry : queryExpansionRelation.asMap().entrySet())
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
		for (OntologyTerm atomicOntologyTerm : ontologyService.getAtomicOntologyTerms(ontologyTerm))
		{
			queryExpansionRelation.put(atomicOntologyTerm, atomicOntologyTerm);
			queryExpansionRelation.putAll(atomicOntologyTerm,
					ontologyService.getChildren(atomicOntologyTerm, DEFAULT_EXPANSION_LEVEL));
		}
	}
}
