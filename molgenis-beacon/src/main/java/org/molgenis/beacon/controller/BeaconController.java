package org.molgenis.beacon.controller;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.service.BeaconService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

	BeaconController(BeaconService beaconService)
	{
		this.beaconService = requireNonNull(beaconService);
	}

	@GetMapping(value = "/{beacon}/", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Beacon info()
	{
		// TODO query beacon service for info on this beacon
		return null;
	}

	@GetMapping(value = "/query", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public BeaconAlleleResponse query(@RequestParam("referenceName") String referenceName,
			@RequestParam("start") Long start, @RequestParam("referenceBases") String referenceBases,
			@RequestParam("alternateBases") String alternateBases,
			@RequestParam(value = "assemblyId", required = false) String assemblyId,
			@RequestParam(value = "datasetIds", required = false) List<String> datasetIds,
			@RequestParam(value = "includeDatasetResponses", required = false) Boolean includeDatasetResponses)
	{
		// TODO create an allele response
		return null;
	}

	@PostMapping(value = "/query", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public BeaconAlleleResponse query(BeaconAlleleRequest request)
	{
		System.out.println("request = " + request);
		// TODO Do the same thing as the GET query
		return null;
	}
}
