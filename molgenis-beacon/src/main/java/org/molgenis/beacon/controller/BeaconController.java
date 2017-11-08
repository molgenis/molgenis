package org.molgenis.beacon.controller;

import org.molgenis.beacon.controller.response.BeaconResponse;
import org.molgenis.beacon.service.BeaconService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.beacon.controller.BeaconController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(URI)
public class BeaconController
{
	static final String URI = "/beacon";

	private BeaconService beaconService;

	public BeaconController(BeaconService beaconService)
	{
		this.beaconService = requireNonNull(beaconService);
	}

	@GetMapping(value = "/datasets", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<String> listDatasets()
	{
		return beaconService.listDatasets();
	}

	@GetMapping(value = "/query", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public BeaconResponse query(@RequestParam("chrom") String chromosome, @RequestParam("pos") String position,
			@RequestParam("ref") String reference, @RequestParam("alt") String allele,
			@RequestParam("dataset") String entityTypeID)
	{
		boolean exists = beaconService.query(chromosome, Long.valueOf(position), reference, allele, entityTypeID);
		return BeaconResponse.create("MOLGENIS", exists);
	}
}
