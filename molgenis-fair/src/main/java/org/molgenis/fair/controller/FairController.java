package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.Model;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.requireNonNull;
import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;

/**
 * Serves metadata for the molgenis FAIR DataPoint.
 */
@Controller
@RequestMapping(BASE_URI)
public class FairController
{
	static final String BASE_URI = "/fdp";

	private final DataService dataService;
	private final EntityModelWriter entityModelWriter;

	FairController(DataService dataService, EntityModelWriter entityModelWriter)
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
			throw new UnknownCatalogException(catalogID);
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

	// TODO add Exception eception handler that retrows FairException
}
