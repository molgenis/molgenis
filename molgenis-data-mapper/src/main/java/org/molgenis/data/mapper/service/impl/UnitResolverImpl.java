package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnitResolverImpl implements UnitResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(UnitResolverImpl.class);

	static final String UNIT_ONTOLOGY_IRI = "http://purl.obolibrary.org/obo/uo.owl";
	private static final Pattern PATTERN_BETWEEN_PARENTHESIS = Pattern.compile("\\(([^\\)]+)");

	private final OntologyService ontologyService;

	@Autowired
	public UnitResolverImpl(OntologyService ontologyService)
	{
		this.ontologyService = checkNotNull(ontologyService);
	}

	@Override
	public OntologyTerm resolveUnit(AttributeMetaData attr, EntityMetaData entityMeta, Repository repo)
	{
		String label = attr.getLabel();
		String description = attr.getDescription();

		OntologyTerm unitOntologyTerm;
		if (label != null || description != null)
		{
			Ontology unitOntology = ontologyService.getOntology(UNIT_ONTOLOGY_IRI);
			if (unitOntology != null)
			{
				Set<String> terms = new HashSet<String>();
				if (label != null)
				{
					extractCandidateUnitTerms(label, terms);
				}
				if (description != null)
				{
					extractCandidateUnitTerms(description, terms);
				}

				if (!terms.isEmpty())
				{
					List<String> ontologyIds = Arrays.asList(unitOntology.getId());
					List<OntologyTerm> ontologyTerms = ontologyService.findOntologyTerms(ontologyIds, terms,
							Integer.MAX_VALUE);
					if (ontologyTerms != null && !ontologyTerms.isEmpty())
					{
						if (ontologyTerms.size() == 1)
						{
							unitOntologyTerm = ontologyTerms.get(0);
						}
						else
						{
							// multiple unit ontology terms detected, pick first
							unitOntologyTerm = ontologyTerms.get(0);
						}
					}
					else
					{
						unitOntologyTerm = null;
					}
				}
				else
				{
					unitOntologyTerm = null;
				}
			}
			else
			{
				LOG.warn("Unit resolver is missing required unit ontology [" + UNIT_ONTOLOGY_IRI + "]");
				unitOntologyTerm = null;
			}
		}
		else
		{
			unitOntologyTerm = null;
		}
		return unitOntologyTerm;
	}

	/**
	 * Extracts strings between parenthesis (e.g. weight (cm) results in term 'cm')
	 * 
	 * @param str
	 * @param terms
	 */
	private void extractCandidateUnitTerms(String str, Set<String> terms)
	{
		Matcher matcher = PATTERN_BETWEEN_PARENTHESIS.matcher(str);

		for (int pos = -1; matcher.find(pos + 1);)
		{
			pos = matcher.start();
			terms.add(matcher.group(1));
		}
	}
}
