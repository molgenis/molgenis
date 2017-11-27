package org.molgenis.beacon.controller;

import org.molgenis.beacon.controller.model.Beacon;
import org.molgenis.beacon.controller.model.request.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.response.BeaconAlleleResponse;
import org.molgenis.beacon.service.BeaconService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
	public BeaconAlleleResponse query(@RequestParam("chrom") String chromosome, @RequestParam("pos") String position,
			@RequestParam("ref") String reference, @RequestParam("alt") String allele,
			@RequestParam("dataset") String entityTypeID)
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
