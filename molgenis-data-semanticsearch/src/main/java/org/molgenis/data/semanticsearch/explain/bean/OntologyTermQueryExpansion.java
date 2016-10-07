package org.molgenis.data.semanticsearch.explain.bean;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

/**
 * The assumption here is that the target data schema is always more general the the indidvual source datasets.
 * Therefore we only expanq queries with child {@link OntologyTerm}s.
 * Expands {@link OntologyTerm}s mapping each of the matched terms to its children up to the DEFAULT_EXPANSION_LEVEL.
 */
public class OntologyTermQueryExpansion
{
	private final List<OntologyTerm> ontologyTerms;
	private final OntologyService ontologyService;
	/**
	 * Maps matched OntologyTerm to its expanded children.
	 */
	private Multimap<OntologyTerm, OntologyTerm> children;

	public OntologyTermQueryExpansion(List<OntologyTerm> matchedOntologyTerms, OntologyService ontologyService)
	{
		this.ontologyTerms = requireNonNull(matchedOntologyTerms);
		this.ontologyService = requireNonNull(ontologyService);
		this.children = LinkedHashMultimap.create();
		populate();
	}

	public List<OntologyTerm> getOntologyTerms()
	{
		return newArrayList(children.values());
	}

	public OntologyTermQueryExpansionSolution getQueryExpansionSolution(TagGroup tagGroup)
	{
		Map<OntologyTerm, OntologyTerm> matchedOntologyTerms = new LinkedHashMap<>();

		for (OntologyTerm sourceOntologyTerm : tagGroup.getOntologyTerms())
		{
			children.asMap().entrySet().stream().filter(entry -> entry.getValue().contains(sourceOntologyTerm))
					.forEach(entry -> matchedOntologyTerms.put(entry.getKey(), sourceOntologyTerm));
		}

		// If for each of the root ontology terms, the term itself or one of its children get matched,
		// then the quality is high
		boolean highQuality = matchedOntologyTerms.size() == children.asMap().keySet().size();

		return OntologyTermQueryExpansionSolution.create(matchedOntologyTerms, highQuality);
	}

	private void populate()
	{
		for (OntologyTerm atomicOntologyTerm : ontologyTerms)
		{
			children.put(atomicOntologyTerm, atomicOntologyTerm);
			children.putAll(atomicOntologyTerm,
					ontologyService.getChildren(atomicOntologyTerm, DEFAULT_EXPANSION_LEVEL));
		}
	}
}
