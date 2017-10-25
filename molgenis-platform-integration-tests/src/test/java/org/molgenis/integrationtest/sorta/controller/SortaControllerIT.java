package org.molgenis.integrationtest.sorta.controller;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.integrationtest.data.DataTestConfig;
import org.molgenis.integrationtest.js.JsTestConfig;
import org.molgenis.integrationtest.script.ScriptTestConfig;
import org.molgenis.integrationtest.sorta.SortaTestConfig;
import org.molgenis.integrationtest.util.UtilTestConfig;
import org.molgenis.integrationtest.utils.AbstractMolgenisIntegrationTests;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.sorta.controller.SortaController;
import org.molgenis.ontology.sorta.request.SortaServiceRequest;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_ROLE;
import static org.molgenis.ontology.sorta.controller.SortaController.*;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData.IDENTIFIER;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("groupsTestNG")
@ContextConfiguration(classes = { SortaControllerIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class SortaControllerIT extends AbstractMolgenisIntegrationTests
{
	@Autowired
	private AppSettings appSettings;

	@Autowired
	private SortaITUtils sortaITUtils;

	private String expression = null;

	public void beforeMethod()
	{
		sortaITUtils.addUserIfExists(SUPERUSER_NAME);
		appSettings.setMenu(
				"{\"type\":\"menu\",\"id\":\"main\",\"label\":\"Home\",\"items\":[{\"type\":\"plugin\",\"id\":\"sortaservice\",\"label\":\"SORTA\",\"params\":\"\"}]}");
	}

	/**
	 * <p>Get jobid from system.</p>
	 *
	 * @return job id
	 * @throws Exception is related to MockMvc implementation
	 */
	private String getValueFromJobsResponse(String key) throws Exception
	{
		String value = "";
		if (this.expression == null || !this.expression.equals(key))
		{
			MvcResult result = mockMvc.perform(get(URI + "/jobs").header(TOKEN_HEADER, getAdminToken())).andReturn();
			JSONObject object = new JSONArray(result.getResponse().getContentAsString()).getJSONObject(0);
			value = object.getString(key);
		}
		return value;
	}

	@Test(groups = "withData")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testUploadFile() throws Exception
	{
		sortaITUtils.addOntologies();

		File file = ResourceUtils.getFile(SortaControllerIT.class, "/txt/sorta_test.txt");

		byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

		MockMultipartFile sortTestFile = new MockMultipartFile("file", file.getName(), MULTIPART_FORM_DATA_VALUE, data);

		mockMvc.perform(fileUpload(URI + "/match/upload").file(sortTestFile)
														 .header(TOKEN_HEADER, getAdminToken())
														 .
																 with(csrf())
														 .param("taskName", "sortaTest")
														 .param("selectOntologies",
																 "http://www.biobankconnect.org/ontologies/2014/2/custom_ontology"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(view().name("redirect:/menu/main/" + SortaController.ID));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testGetJobs() throws Exception
	{
		mockMvc.perform(get(URI + "/jobs"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_VALUE))
			   .andExpect(jsonPath("$[0].name").value("sortaTest"));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testMatchResult() throws Exception
	{
		mockMvc.perform(
				get(URI + "/result/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testMatchCount() throws Exception
	{
		mockMvc.perform(
				get(URI + "/count/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().string("{\"numberOfMatched\":0,\"numberOfUnmatched\":45}"));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testMatchRetrieve() throws Exception
	{

		String jobId = getValueFromJobsResponse("identifier");
		String ontologyIri = getValueFromJobsResponse("ontologyIri");
		String filterQuery = "test";

		SortaServiceRequest request = new SortaServiceRequest(jobId, ontologyIri, filterQuery, false,
				new EntityPager(0, 10, new Long(0), null));

		mockMvc.perform(post(URI + "/match/retrieve/").content(new Gson().toJson(request))
													  .
															  header(TOKEN_HEADER, getAdminToken())
													  .with(csrf())
													  .contentType(APPLICATION_JSON_VALUE)
													  .accept(APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testMathEntity() throws Exception
	{

		Map<String, Object> request = new HashMap<>();
		request.put("sortaJobExecutionId", getValueFromJobsResponse("identifier"));
		request.put(IDENTIFIER, getValueFromJobsResponse("sourceEntity"));

		mockMvc.perform(post(URI + "/match/entity/").content(new Gson().toJson(request))
													.
															header(TOKEN_HEADER, getAdminToken())
													.with(csrf())
													.contentType(APPLICATION_JSON_VALUE)
													.accept(APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testMatchDownload() throws Exception
	{
		mockMvc.perform(get(URI + "/match/download/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER,
				getAdminToken()).with(csrf())).andExpect(status().isOk());
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testUpdateThreshold() throws Exception
	{

		mockMvc.perform(post(URI + "/threshold/" + getValueFromJobsResponse("identifier")).param("threshold",
				String.valueOf(DEFAULT_THRESHOLD)).header(TOKEN_HEADER, getAdminToken()).with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@Test(dependsOnGroups = "withData")
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testDelete() throws Exception
	{
		mockMvc.perform(
				post(URI + "/delete/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken())
																			   .with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}
	
	public void afterClass()
	{
		sortaITUtils.cleanUp();
	}

	@Configuration
	@Import({ SortaTestConfig.class, OntologyTestConfig.class, ScriptTestConfig.class, UtilTestConfig.class,
			JsTestConfig.class, DataTestConfig.class, SortaITUtils.class })
	public static class Config
	{
	}

}
