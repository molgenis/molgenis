package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.js.magma.JsMagmaScriptRegistrator.SCRIPT_TYPE_JAVASCRIPT_MAGMA;
import static org.molgenis.script.Script.ENTITY_NAME;
import static org.molgenis.script.Script.TYPE;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.script.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmTemplateServiceImpl implements AlgorithmTemplateService
{
	private final DataService dataService;
	private final OntologyTagService ontologyTagService;
	private final OntologyTermRepository ontologyTermRepository;
	private final SemanticSearchService semanticSearchService;

	@Autowired
	public AlgorithmTemplateServiceImpl(DataService dataService, OntologyTagService ontologyTagService,
			OntologyTermRepository ontologyTermRepository, SemanticSearchService semanticSearchService)
	{
		this.dataService = checkNotNull(dataService);
		this.ontologyTagService = checkNotNull(ontologyTagService);
		this.ontologyTermRepository = checkNotNull(ontologyTermRepository);
		this.semanticSearchService = checkNotNull(semanticSearchService);
	}

	@Override
	public Stream<AlgorithmTemplate> find(AttributeMetaData targetAttr, EntityMetaData targetEntityMeta,
			EntityMetaData sourceEntityMeta)
	{
		// get all js scripts
		Iterable<Script> jsScripts = dataService.findAll(ENTITY_NAME,
				new QueryImpl().eq(TYPE, SCRIPT_TYPE_JAVASCRIPT_MAGMA), Script.class);

		// select all magma js scripts that can be used with target and sources
		return StreamSupport.stream(jsScripts.spliterator(), false)
				.filter(jsScript -> isRenderable(jsScript, targetAttr, targetEntityMeta, sourceEntityMeta))
				.map(jsScript -> new AlgorithmTemplate(jsScript, sourceEntityMeta, this));
	}

	private boolean isRenderable(Script script, AttributeMetaData targetAttr, EntityMetaData targetEntityMeta,
			EntityMetaData sourceEntityMeta)
	{
		boolean isRenderable = false;
		String targetParam = script.getName();
		// script name match target attribute?
		if (canMapToOntologyTerm(targetAttr, targetEntityMeta, targetParam))
		{
			// script paramaters match source entity meta?
			isRenderable = script.getParameters().stream()
					.allMatch(param -> mapParamToAttr(sourceEntityMeta, param.getName()) != null);
		}
		return isRenderable;
	}

	public AttributeMetaData mapParamToAttr(EntityMetaData entityMeta, String param)
	{
		AttributeMetaData paramAttr;
		OntologyTerm ontologyTerm = findOntologyTerm(param);
		if (ontologyTerm != null)
		{
			paramAttr = StreamSupport.stream(entityMeta.getAtomicAttributes().spliterator(), false)
					.filter(attr -> canMapToOntologyTerm(attr, entityMeta, ontologyTerm)).findFirst().orElse(null);
		}
		else
		{
			paramAttr = null;
		}
		return paramAttr;
	}

	private boolean canMapToOntologyTerm(AttributeMetaData attr, EntityMetaData entityMeta, String param)
	{
		OntologyTerm ontologyTerm = findOntologyTerm(param);
		return ontologyTerm != null ? canMapToOntologyTerm(attr, entityMeta, ontologyTerm) : null;
	}

	private boolean canMapToOntologyTerm(AttributeMetaData attr, EntityMetaData entityMeta, OntologyTerm ontologyTerm)
	{
		boolean containsParam;
		// Multimap<Relation, OntologyTerm> attrTags = ontologyTagService.getTagsForAttribute(entityMeta, attr);
		// if (!attrTags.isEmpty())
		// {
		// containsParam = attrTags.values().stream()
		// .anyMatch(attrOntologyTerm -> attrOntologyTerm.equals(ontologyTerm));
		// }
		// else
		// {
		OntologyTerm attrOntologyTerm = findOntologyTerm(attr.getName());
		if (attrOntologyTerm != null)
		{
			containsParam = attrOntologyTerm.equals(ontologyTerm);
		}
		else
		{
			containsParam = false;
		}
		// }

		return containsParam;
	}

	private OntologyTerm findOntologyTerm(String paramName)
	{
		// map param to ontology term
		List<OntologyTerm> ontologyTerms = ontologyTermRepository.findOntologyTerms(paramName, 1);
		return !ontologyTerms.isEmpty() ? ontologyTerms.iterator().next() : null;
	}
}
