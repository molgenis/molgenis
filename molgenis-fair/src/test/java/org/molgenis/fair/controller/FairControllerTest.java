package org.molgenis.fair.controller;

import org.mockito.Mockito;
import org.molgenis.core.ui.converter.RdfConverter;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class FairControllerTest extends AbstractTestNGSpringContextTests
{
	private FairController controller;
	private DataService dataService;
	private EntityModelWriter entityModelWriter;

	private MockMvc mockMvc;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@BeforeMethod
	public void beforeTest()
	{
		dataService = mock(DataService.class);
		entityModelWriter = mock(EntityModelWriter.class);
		controller = new FairController(dataService, entityModelWriter);

		mockMvc = MockMvcBuilders.standaloneSetup(controller)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter,
										 new RdfConverter())
								 .build();
	}

	@Test
	public void getMetadataTest() throws Exception
	{
		Entity answer = mock(Entity.class);
		when(dataService.findOne(eq("fdp_Metadata"), any())).thenReturn(answer);

		this.mockMvc.perform(get("/api/fdp").header("X-Forwarded-Host", "website.com")
											.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk());
		Mockito.verify(entityModelWriter).createRdfModel("http://website.com/api/fdp", answer);
	}

	@Test
	public void getCatalogTest() throws Exception
	{
		reset(dataService);

		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Catalog", "catalogID")).thenReturn(answer);

		this.mockMvc.perform(
				get(URI.create("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID?blah=value")).contentType(
				MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk());

		Mockito.verify(entityModelWriter).createRdfModel("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID", answer);
	}

	@Test
	public void getCatalogTestUnknownCatalog() throws Exception
	{
		reset(dataService);

		Entity answer = mock(Entity.class);

		this.mockMvc.perform(
				get(URI.create("http://molgenis01.gcc.rug.nl:8080/api/fdp/catalogID?blah=value")).contentType(
				MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	}

	@Test
	public void getCatalogTestForwarded() throws Exception
	{
		reset(dataService);

		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Catalog", "catalogID")).thenReturn(answer);

		this.mockMvc.perform(get("/api/fdp/catalogID").header("X-Forwarded-Host", "website.com")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk());

		Mockito.verify(entityModelWriter).createRdfModel("http://website.com/api/fdp/catalogID", answer);
	}

	@Test
	public void getDatasetTest() throws Exception
	{
		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Dataset", "datasetID")).thenReturn(answer);
		this.mockMvc.perform(get("/api/fdp/catalogID/datasetID").header("X-Forwarded-Host", "website.com")
																.contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk());

		Mockito.verify(entityModelWriter).createRdfModel("http://website.com/api/fdp/catalogID/datasetID", answer);
	}

	@Test
	public void getDistributionTest() throws Exception
	{
		Entity answer = mock(Entity.class);

		when(dataService.findOneById("fdp_Distribution", "distributionID")).thenReturn(answer);
		this.mockMvc.perform(
				get("/api/fdp/catalogID/datasetID/distributionID").header("X-Forwarded-Host", "website.com")
																  .header("X-Forwarded-Proto", "https")
																  .contentType(
																				   MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk());

		Mockito.verify(entityModelWriter)
			   .createRdfModel("https://website.com/api/fdp/catalogID/datasetID/distributionID", answer);
	}

}