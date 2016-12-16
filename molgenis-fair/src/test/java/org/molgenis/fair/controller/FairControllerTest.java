package org.molgenis.fair.controller;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FairControllerTest
{
	private FairController controller;
	private DataService dataService;
	private EntityModelWriter entityModelWriter;

	@BeforeMethod
	public void beforeTest()
	{
		dataService = mock(DataService.class);
		entityModelWriter = mock(EntityModelWriter.class);
		controller = new FairController(dataService, entityModelWriter);
	}

	@Test
	public void getMetadataTest()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("molgenis01.gcc.rug.nl");
		when(request.getLocalPort()).thenReturn(8080);

		Entity answer = mock(Entity.class);
		when(dataService.findOne(eq("fdp_Metadata"), anyObject())).thenReturn(answer);

		controller.getMetadata(request);
		Mockito.verify(entityModelWriter).createRdfModel("http://molgenis01.gcc.rug.nl:8080/fdp", answer);
	}

	@Test
	public void getCatalogTest()
	{
		reset(dataService);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("molgenis01.gcc.rug.nl");
		when(request.getLocalPort()).thenReturn(8080);

		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Catalog", "catalogID")).thenReturn(answer);

		controller.getCatalog("catalogID", request);

		Mockito.verify(entityModelWriter).createRdfModel("http://molgenis01.gcc.rug.nl:8080/fdp/catalogID", answer);
	}

	@Test
	public void getDatasetTest()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("molgenis01.gcc.rug.nl");
		when(request.getLocalPort()).thenReturn(8080);

		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Dataset", "datasetID")).thenReturn(answer);
		controller.getDataset("catalogID", "datasetID", request);

		Mockito.verify(entityModelWriter)
				.createRdfModel("http://molgenis01.gcc.rug.nl:8080/fdp/catalogID/datasetID", answer);
	}

	@Test
	public void getDistributionTest()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("molgenis01.gcc.rug.nl");
		when(request.getLocalPort()).thenReturn(8080);

		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Distribution", "distributionID")).thenReturn(answer);
		controller.getDistribution("catalogID", "datasetID", "distributionID", request);

		Mockito.verify(entityModelWriter)
				.createRdfModel("http://molgenis01.gcc.rug.nl:8080/fdp/catalogID/datasetID/distributionID", answer);
	}

}
