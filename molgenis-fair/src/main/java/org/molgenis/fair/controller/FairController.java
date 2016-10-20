package org.molgenis.fair.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.converter.RDFMediaType;
import org.molgenis.ui.model.SubjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(BASE_URI)
/**
 * Serves metadata for the molgenis FAIR DataPoint.
 */
public class FairController
{
	static final String BASE_URI = "/fdp";

	private final DataService dataService;

	@Autowired
	public FairController(DataService dataService)
	{
		this.dataService = dataService;
	}

	private static String getBaseUri(HttpServletRequest request)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + BASE_URI;
		}
		else
		{
			apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + BASE_URI;
		}
		return apiUrl;
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public SubjectEntity getMetadata(HttpServletRequest request)
	{
		return new SubjectEntity(getBaseUri(request),
				dataService.findOne("fdp_Metadata", new QueryImpl<>()));
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE, value = "/{catalogID:.+}")
	@ResponseBody
	@RunAsSystem
	public SubjectEntity getCatalog(@PathVariable("catalogID") String catalogID, HttpServletRequest request)
	{
		return new SubjectEntity(getBaseUri(request)+"/"+catalogID, dataService.findOneById("fdp_Catalog", catalogID));
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE, value = "/{catalogID:.+}/{datasetID:.+}")
	@ResponseBody
	@RunAsSystem
	public SubjectEntity getDataset(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, HttpServletRequest request)
	{
		return new SubjectEntity(getBaseUri(request) + "/" + catalogID + "/" + datasetID,
				dataService.findOneById("fdp_Dataset", datasetID));
	}

	@RequestMapping(method = GET, produces = RDFMediaType.TEXT_TURTLE_VALUE, value="/{catalogID:.+}/{datasetID:.+}/{distributionID:.+}")
	@ResponseBody
	@RunAsSystem
	public SubjectEntity getDistribution(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, @PathVariable("distributionID") String distributionID,
			HttpServletRequest request)
	{
		return new SubjectEntity(getBaseUri(request) + "/" + catalogID + "/" + datasetID + "/" + distributionID,
				dataService.findOneById("fdp_Distribution", distributionID));
	}

}
