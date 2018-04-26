package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;
import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Serves metadata for the molgenis FAIR DataPoint.
 */
@Controller
@RequestMapping(BASE_URI)
public class FairController
{
	private static final Logger LOG = LoggerFactory.getLogger(FairController.class);

	static final String BASE_URI = "/api/fdp";

	private final DataService dataService;
	private final EntityModelWriter entityModelWriter;

	public FairController(DataService dataService, EntityModelWriter entityModelWriter)
	{
		this.dataService = requireNonNull(dataService);
		this.entityModelWriter = requireNonNull(entityModelWriter);
	}

	private static UriComponentsBuilder getBaseUri()
	{
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE_URI);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public Model getMetadata()
	{
		String subjectIRI = getBaseUri().toUriString();
		Entity subjectEntity = dataService.findOne("fdp_Metadata", new QueryImpl<>());
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}")
	@ResponseBody
	@RunAsSystem
	public Model getCatalog(@PathVariable("catalogID") String catalogID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Catalog", catalogID);
		if (subjectEntity == null)
		{
			throw new UnknownEntityException(format("Catalog with id [%s] does not exist", catalogID));
		}
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}")
	@ResponseBody
	@RunAsSystem
	public Model getDataset(@PathVariable("catalogID") String catalogID, @PathVariable("datasetID") String datasetID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID, datasetID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Dataset", datasetID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}/{distributionID}")
	@ResponseBody
	@RunAsSystem
	public Model getDistribution(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, @PathVariable("distributionID") String distributionID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID, datasetID, distributionID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Distribution", distributionID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	public Model handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.warn(e.getMessage(), e);
		Model emptyModel = new LinkedHashModel();
		return emptyModel;
	}
}
