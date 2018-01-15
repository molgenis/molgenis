package org.molgenis.semanticmapper.service.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticmapper.utils.UnitHelper;
import org.molgenis.semanticsearch.string.NGramDistanceAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class UnitResolverImpl implements UnitResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(UnitResolverImpl.class);

	static final String UNIT_ONTOLOGY_IRI = "http://purl.obolibrary.org/obo/uo.owl";

	private final OntologyService ontologyService;

	public UnitResolverImpl(OntologyService ontologyService)
	{
		this.ontologyService = requireNonNull(ontologyService);
	}

	@Override
	public Unit<? extends Quantity> resolveUnit(Attribute attr, EntityType entityType)
	{
		Set<String> tokens = tokenize(attr.getLabel(), attr.getDescription());

		// Option 1: Check if a term matches a unit
		Unit<? extends Quantity> unit = null;
		if (!tokens.isEmpty())
		{
			for (String term : tokens)
			{
				try
				{
					unit = Unit.valueOf(term);
					break;
				}
				catch (IllegalArgumentException e)
				{
					// noop
				}
			}

			if (isUnitEmpty(unit))
			{
				// Option 2: Search unit ontology for a match
				OntologyTerm unitOntologyTerm = resolveUnitOntologyTerm(
						tokens.stream().map(this::convertNumberToOntologyTermStyle).collect(Collectors.toSet()));

				if (unitOntologyTerm != null)
				{
					// try label + synonym labels until hit
					for (String synonymLabel : unitOntologyTerm.getSynonyms())
					{
						try
						{
							unit = Unit.valueOf(synonymLabel);
							break;
						}
						catch (IllegalArgumentException e)
						{
							// noop
						}
					}
				}
			}
		}

		if (isUnitEmpty(unit))
		{
			unit = null;
		}

		return unit;
	}

	private OntologyTerm resolveUnitOntologyTerm(Set<String> tokens)
	{
		OntologyTerm unitOntologyTerm;
		Ontology unitOntology = ontologyService.getOntology(UNIT_ONTOLOGY_IRI);
		if (unitOntology != null)
		{
			if (!tokens.isEmpty())
			{
				List<String> ontologyIds = Arrays.asList(unitOntology.getId());
				List<OntologyTerm> ontologyTerms = ontologyService.findExcatOntologyTerms(ontologyIds, tokens,
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
		return unitOntologyTerm;
	}

	String convertNumberToOntologyTermStyle(String term)
	{
		term = UnitHelper.superscriptToNumber(term.replaceAll("\\^", StringUtils.EMPTY));
		Pattern pattern = Pattern.compile("\\w+(\\d+)");
		Matcher matcher = pattern.matcher(term);

		if (matcher.find())
		{
			String group = matcher.group(1);
			String modifiedPart = group.trim();
			modifiedPart = "^[" + modifiedPart + "]";
			term = term.replaceAll(group, modifiedPart);
		}
		return QueryParser.escape(term);
	}

	Set<String> tokenize(String... terms)
	{
		Set<String> tokens = new HashSet<>();
		if (terms != null && terms.length > 0)
		{
			Sets.newHashSet(terms)
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(StringUtils::lowerCase)
				.map(this::replaceIllegalChars)
				.forEach(term -> tokens.addAll(Sets.newHashSet(term.split("\\s+"))
												   .stream()
												   .filter(this::notPureNumberExpression)
												   .map(UnitHelper::numberToSuperscript)
												   .collect(Collectors.toSet())));

			tokens.removeAll(NGramDistanceAlgorithm.STOPWORDSLIST);
		}
		return tokens;
	}

	boolean isUnitEmpty(Unit<? extends Quantity> unit)
	{
		return unit == null || StringUtils.isBlank(unit.toString());
	}

	boolean notPureNumberExpression(String str)
	{
		return !str.matches("\\d+");
	}

	String replaceIllegalChars(String term)
	{
		return UnitHelper.superscriptToNumber(term).replaceAll("[^a-zA-Z0-9 /\\^]", " ");
	}
}
