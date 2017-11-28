package org.molgenis.beacon.controller;

import org.mockito.Mock;
import org.molgenis.beacon.service.BeaconInfoService;
import org.molgenis.beacon.service.BeaconQueryService;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class BeaconControllerTest
{
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

		mockMvc = MockMvcBuilders.standaloneSetup(beaconController).build();
	}

	@Test
	public void testQuery() throws Exception
	{
		mockMvc.perform(get("/beacon/query").param("chrom", "1")
											.param("pos", "1000")
											.param("ref", "A")
											.param("alt", "G")
											.param("dataset", "test")).andExpect(status().isOk());

		verify(beaconQueryService).query("1", 1000L, "A", "G", "test");
	}
}
