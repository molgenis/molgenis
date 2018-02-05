package org.molgenis.beacon.controller;

import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.beacon.service.BeaconInfoService;
import org.molgenis.beacon.service.BeaconQueryService;
import org.molgenis.core.ui.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class BeaconControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;

	@Mock
	private BeaconInfoService beaconInfoService;

	@Mock
	private BeaconQueryService beaconQueryService;

	@BeforeMethod
	private void beforeMethod()
	{
		initMocks(this);

		BeaconController beaconController = new BeaconController(beaconInfoService, beaconQueryService);
		mockMvc = MockMvcBuilders.standaloneSetup(beaconController)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
								 .build();
	}

	@Test
	public void getAllBeaconsTest() throws Exception
	{
		List<BeaconDatasetResponse> beaconDatasets = newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
		BeaconResponse beaconResponse = BeaconResponse.create("beaconA", "beacon A", "0.3.0", null, "", "", "",
				beaconDatasets);

		when(beaconInfoService.getAvailableBeacons()).thenReturn(Lists.newArrayList(beaconResponse));

		mockMvc.perform(get("/beacon/list"))
			   .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string(getBeaconResponseListAsJson()));

		verify(beaconInfoService).getAvailableBeacons();
	}

	@Test
	public void infoTest() throws Exception
	{
		List<BeaconDatasetResponse> beaconDatasets = newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
		BeaconResponse beaconResponse = BeaconResponse.create("beaconA", "beacon A", "0.3.0", null, "", "", "",
				beaconDatasets);

		when(beaconInfoService.info("beaconA")).thenReturn(beaconResponse);

		mockMvc.perform(get("/beacon/{beaconId}", "beaconA"))
			   .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string(getBeaconResponseAsJson()));

		verify(beaconInfoService).info("beaconA");
	}

	@Test
	public void getQueryTest() throws Exception
	{
		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");
		BeaconAlleleResponse response = BeaconAlleleResponse.create("beaconA", true, null, request);

		when(beaconQueryService.query("1", 100L, "A", "T", "beaconA")).thenReturn(response);

		mockMvc.perform(get("/beacon/{beaconId}/query", "beaconA").param("referenceName", "1")
																  .param("start", "100")
																  .param("referenceBases", "A")
																  .param("alternateBases", "T"))
			   .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string(getBeaconAlleleResponseAsJson()));

		verify(beaconQueryService, times(1)).query("1", 100L, "A", "T", "beaconA");
	}

	@Test
	public void testPostQuery() throws Exception
	{
		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");
		BeaconAlleleResponse response = BeaconAlleleResponse.create("beaconA", true, null, request);

		when(beaconQueryService.query("beaconA", request)).thenReturn(response);

		mockMvc.perform(post("/beacon/{beaconId}/query", "beaconA").content(getBeaconAlleleRequestJson())
																   .contentType(APPLICATION_JSON))
			   .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string(getBeaconAlleleResponseAsJson()));

		verify(beaconQueryService, times(1)).query("beaconA", request);
	}

	private String getBeaconResponseListAsJson()
	{
		return "[" + getBeaconResponseAsJson() + "]";
	}

	private String getBeaconResponseAsJson()
	{
		return "{\"id\":\"beaconA\",\"name\":\"beacon A\",\"apiVersion\":\"0.3.0\",\"description\":\"\",\"version\":\"\",\"welcomeUrl\":\"\",\"datasets\":[{\"id\":\"dataset\",\"name\":\"DATA\",\"description\":\"\"}]}";
	}

	private String getBeaconAlleleResponseAsJson()
	{
		return "{\"beaconId\":\"beaconA\",\"exists\":true,\"alleleRequest\":{\"referenceName\":\"1\",\"start\":100,\"referenceBases\":\"A\",\"alternateBases\":\"T\"}}";
	}

	private String getBeaconAlleleRequestJson()
	{
		return "{\"referenceName\":\"1\",\"start\":\"100\",\"referenceBases\":\"A\",\"alternateBases\":\"T\"}";
	}
}
