package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.Model;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.requireNonNull;
import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

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

	@Autowired
	public FairController(DataService dataService, EntityModelWriter entityModelWriter)
	{
		this.dataService = requireNonNull(dataService);
		this.entityModelWriter = requireNonNull(entityModelWriter);
	}

	private static String getBaseUri(HttpServletRequest request)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(BASE_URI).toUriString();
		}
		else
		{
			String scheme = request.getHeader("X-Forwarded-Proto");
			if (scheme == null) scheme = request.getScheme();
			apiUrl = scheme + "://" + request.getHeader("X-Forwarded-Host") + BASE_URI;
		}
		return apiUrl;
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public Model getMetadata(HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request);
		Entity subjectEntity = dataService.findOne("fdp_Metadata", new QueryImpl<>());
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID}")
	@ResponseBody
	@RunAsSystem
	public Model getCatalog(@PathVariable("catalogID") String catalogID, HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request) + '/' + catalogID;
		Entity subjectEntity = dataService.findOneById("fdp_Catalog", catalogID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}")
	@ResponseBody
	@RunAsSystem
	public Model getDataset(@PathVariable("catalogID") String catalogID, @PathVariable("datasetID") String datasetID,
			HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request) + '/' + catalogID + '/' + datasetID;
		Entity subjectEntity = dataService.findOneById("fdp_Dataset", datasetID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}/{distributionID}")
	@ResponseBody
	@RunAsSystem
	public Model getDistribution(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, @PathVariable("distributionID") String distributionID,
			HttpServletRequest request)
	{

		String subjectIRI = getBaseUri(request) + '/' + catalogID + '/' + datasetID + '/' + distributionID;
		Entity subjectEntity = dataService.findOneById("fdp_Distribution", distributionID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

}
