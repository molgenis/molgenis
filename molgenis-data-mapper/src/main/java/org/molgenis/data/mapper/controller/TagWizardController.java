package org.molgenis.data.mapper.controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.mapper.data.request.AddTagRequest;
import org.molgenis.data.mapper.data.request.AutoTagRequest;
import org.molgenis.data.mapper.data.request.GetOntologyTermRequest;
import org.molgenis.data.mapper.data.request.RemoveTagRequest;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.semantic.OntologyTag;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTagObject;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.elasticsearch.common.collect.ImmutableSet.of;
import static org.molgenis.data.mapper.controller.TagWizardController.URI;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

@Controller
@RequestMapping(URI)
public class TagWizardController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "tagwizard";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String VIEW_TAG_WIZARD = "view-tag-wizard";

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	public TagWizardController()
	{
		super(URI);
	}

	/**
	 * Displays on tag wizard button press
	 *
	 * @param target The target entity name
	 * @param model  the model
	 * @return name of the tag wizard view
	 */
	@RequestMapping
	public String viewTagWizard(@RequestParam(required = false, value = "selectedTarget") String target, Model model)
	{
		List<String> entityNames = dataService.findAll(ENTITY_META_DATA)
				.map(e -> e.getString(EntityMetaDataMetaData.FULL_NAME)).collect(toList());

		if (StringUtils.isEmpty(target))
		{
			Optional<String> findFirst = entityNames.stream().findFirst();
			if (findFirst.isPresent())
			{
				target = findFirst.get();
			}
		}

		if (StringUtils.isEmpty(target))
		{
			throw new UnknownEntityException("There are no entities available!");
		}

		List<Ontology> ontologies = ontologyService.getOntologies();
		EntityMetaData emd = dataService.getEntityMetaData(target);
		List<AttributeMetaData> attributes = newArrayList(emd.getAttributes());
		Map<String, Multimap<Relation, OntologyTagObject>> taggedAttributeMetaDatas = attributes.stream()
				.collect(toMap((x -> x.getName()), (x -> ontologyTagService.getTagsForAttribute(emd, x))));

		model.addAttribute("entity", emd);
		model.addAttribute("entityNames", entityNames);
		model.addAttribute("attributes", attributes);
		model.addAttribute("ontologies", ontologies);
		model.addAttribute("taggedAttributeMetaDatas", taggedAttributeMetaDatas);
		model.addAttribute("relations", Relation.values());

		return VIEW_TAG_WIZARD;
	}

	/**
	 * Add a tag for a single attribute
	 *
	 * @param request the {@link AddTagRequest} containing the entityName, attributeName, relationIRI and ontologyTermIRIs
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/tagattribute")
	public
	@ResponseBody
	OntologyTag addTagAttribute(@Valid @RequestBody AddTagRequest request)
	{
		return ontologyTagService
				.addAttributeTag(request.getEntityName(), request.getAttributeName(), request.getRelationIRI(),
						request.getOntologyTermIRIs());
	}

	/**
	 * Delete a single tag
	 *
	 * @param request the {@link RemoveTagRequest} containing entityName, attributeName, relationIRI and ontologyTermIRI
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/deletesingletag")
	public
	@ResponseBody
	void deleteSingleTag(@Valid @RequestBody RemoveTagRequest request)
	{
		ontologyTagService
				.removeAttributeTag(request.getEntityName(), request.getAttributeName(), request.getRelationIRI(),
						request.getOntologyTermIRI());
	}

	/**
	 * Clears all tags from every attribute in the current target entity
	 *
	 * @param entityName The name of the {@link Entity}
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/clearalltags")
	public
	@ResponseBody
	void clearAllTags(@RequestParam String entityName)
	{
		ontologyTagService.removeAllTagsFromEntity(entityName);
	}

	/**
	 * Automatically tags all attributes in the current entity using Lucene lexical matching. Stores the tags in the
	 * OntologyTag Repository.
	 *
	 * @param request containing the entityName and selected ontology identifiers
	 * @return A {@link Map} containing AttributeMetaData name and a Map of Tag iri and label
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/autotagattributes")
	public
	@ResponseBody
	Map<String, OntologyTag> autoTagAttributes(@Valid @RequestBody AutoTagRequest request)
	{
		Map<AttributeMetaData, Hit<OntologyTagObject>> autoGeneratedTags = semanticSearchService
				.findTags(request.getEntityName(), request.getOntologyIds());

		return ontologyTagService.tagAttributesInEntity(request.getEntityName(),
				Maps.transformValues(autoGeneratedTags, hit -> hit.getResult()));
	}

	/**
	 * Returns ontology terms based on a search term and a selected ontology
	 *
	 * @param request Containing ontology identifiers and a search term
	 * @return A {@link List} of {@link OntologyTerm}s
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getontologyterms")
	public
	@ResponseBody
	List<OntologyTerm> getAllOntologyTerms(@Valid @RequestBody GetOntologyTermRequest request)
	{
		return ontologyService.findOntologyTerms(request.getOntologyIds(), of(request.getSearchTerm()), 100);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}
