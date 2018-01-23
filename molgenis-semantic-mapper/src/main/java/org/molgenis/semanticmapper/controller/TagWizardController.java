package org.molgenis.semanticmapper.controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.Relation;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticmapper.data.request.AddTagRequest;
import org.molgenis.semanticmapper.data.request.AutoTagRequest;
import org.molgenis.semanticmapper.data.request.GetOntologyTermRequest;
import org.molgenis.semanticmapper.data.request.RemoveTagRequest;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.OntologyTag;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
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

import static com.google.common.collect.ImmutableSortedSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.semanticmapper.controller.TagWizardController.URI;

@Controller
@RequestMapping(URI)
public class TagWizardController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(TagWizardController.class);

	public static final String ID = "tagwizard";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

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
	@GetMapping
	public String viewTagWizard(@RequestParam(required = false, value = "selectedTarget") String target, Model model)
	{
		List<String> entityTypeIds = dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)
												.map(EntityType::getId)
												.collect(toList());

		if (StringUtils.isEmpty(target))
		{
			Optional<String> findFirst = entityTypeIds.stream().findFirst();
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
		EntityType emd = dataService.getEntityType(target);
		List<Attribute> attributes = newArrayList(emd.getAttributes());
		Map<String, Multimap<Relation, OntologyTerm>> taggedAttributes = attributes.stream()
																				   .collect(toMap((Attribute::getName),
																						   (x -> ontologyTagService.getTagsForAttribute(
																								   emd, x))));

		model.addAttribute("entity", emd);
		model.addAttribute("entityTypeIds", entityTypeIds);
		model.addAttribute("attributes", attributes);
		model.addAttribute("ontologies", ontologies);
		model.addAttribute("taggedAttributes", taggedAttributes);
		model.addAttribute("relations", Relation.values());

		return VIEW_TAG_WIZARD;
	}

	/**
	 * Add a tag for a single attribute
	 *
	 * @param request the {@link AddTagRequest} containing the entityTypeId, attributeName, relationIRI and ontologyTermIRIs
	 */
	@PostMapping("/tagattribute")
	public @ResponseBody
	OntologyTag addTagAttribute(@Valid @RequestBody AddTagRequest request)
	{
		return ontologyTagService.addAttributeTag(request.getEntityTypeId(), request.getAttributeName(),
				request.getRelationIRI(), request.getOntologyTermIRIs());
	}

	/**
	 * Delete a single tag
	 *
	 * @param request the {@link RemoveTagRequest} containing entityTypeId, attributeName, relationIRI and ontologyTermIRI
	 */
	@PostMapping("/deletesingletag")
	public @ResponseBody
	void deleteSingleTag(@Valid @RequestBody RemoveTagRequest request)
	{
		ontologyTagService.removeAttributeTag(request.getEntityTypeId(), request.getAttributeName(),
				request.getRelationIRI(), request.getOntologyTermIRI());
	}

	/**
	 * Clears all tags from every attribute in the current target entity
	 *
	 * @param entityTypeId The name of the {@link Entity}
	 */
	@PostMapping("/clearalltags")
	public @ResponseBody
	void clearAllTags(@RequestParam String entityTypeId)
	{
		ontologyTagService.removeAllTagsFromEntity(entityTypeId);
	}

	/**
	 * Automatically tags all attributes in the current entity using Lucene lexical matching. Stores the tags in the
	 * OntologyTag Repository.
	 *
	 * @param request containing the entityTypeId and selected ontology identifiers
	 * @return A {@link Map} containing Attribute name and a Map of Tag iri and label
	 */
	@PostMapping("/autotagattributes")
	public @ResponseBody
	Map<String, OntologyTag> autoTagAttributes(@Valid @RequestBody AutoTagRequest request)
	{
		Map<Attribute, Hit<OntologyTerm>> autoGeneratedTags = semanticSearchService.findTags(request.getEntityTypeId(),
				request.getOntologyIds());

		return ontologyTagService.tagAttributesInEntity(request.getEntityTypeId(),
				Maps.transformValues(autoGeneratedTags, Hit::getResult));
	}

	/**
	 * Returns ontology terms based on a search term and a selected ontology
	 *
	 * @param request Containing ontology identifiers and a search term
	 * @return A {@link List} of {@link OntologyTerm}s
	 */
	@PostMapping("/getontologyterms")
	public @ResponseBody
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
