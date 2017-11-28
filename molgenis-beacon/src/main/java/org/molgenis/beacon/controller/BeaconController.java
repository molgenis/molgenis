package org.molgenis.beacon.controller;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.service.BeaconInfoService;
import org.molgenis.beacon.service.BeaconQueryService;
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

	private BeaconInfoService beaconInfoService;
	private BeaconQueryService beaconQueryService;

	BeaconController(BeaconInfoService beaconInfoService, BeaconQueryService beaconQueryService)
	{
		this.beaconInfoService = requireNonNull(beaconInfoService);
		this.beaconQueryService = requireNonNull(beaconQueryService);
	}

	@GetMapping(value = "/list", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Beacon> getAllBeacons()
	{
		return beaconInfoService.getAvailableBeacons();
	}

	@GetMapping(value = "/{beaconId}", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Beacon info(@PathVariable("beaconId") final String beaconId)
	{
		return beaconInfoService.info(beaconId);
	}

	@GetMapping(value = "/{beaconId}/query", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public BeaconAlleleResponse query(@RequestParam("referenceName") String referenceName,
			@RequestParam("start") Long start, @RequestParam("referenceBases") String referenceBases,
			@RequestParam("alternateBases") String alternateBases, @PathVariable("beaconId") final String beaconId)
	{
		return beaconQueryService.query(referenceName, start, referenceBases, alternateBases, beaconId);
	}

	@PostMapping(value = "/{beaconId}/query", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public BeaconAlleleResponse query(@PathVariable("beaconId") final String beaconId, BeaconAlleleRequest request)
	{
		return beaconQueryService.query(beaconId, request);
	}
}
