package org.molgenis.beacon.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.beacon.service.BeaconInfoService;
import org.molgenis.beacon.service.BeaconQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.beacon.controller.BeaconController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api("Beacon")
@Controller
@RequestMapping(URI)
public class BeaconController
{
	public static final String URI = "/beacon";

	private BeaconInfoService beaconInfoService;
	private BeaconQueryService beaconQueryService;

	BeaconController(BeaconInfoService beaconInfoService, BeaconQueryService beaconQueryService)
	{
		this.beaconInfoService = requireNonNull(beaconInfoService);
		this.beaconQueryService = requireNonNull(beaconQueryService);
	}

	@ResponseBody
	@GetMapping(value = "/list", produces = APPLICATION_JSON_VALUE)
	@ApiOperation(value = "List all beacons", response = BeaconResponse.class, responseContainer = "List")
	public List<BeaconResponse> getAllBeacons()
	{
		return beaconInfoService.getAvailableBeacons();
	}

	@ResponseBody
	@GetMapping(value = "/{beaconId}", produces = APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Retrieve info on one beacon", response = BeaconResponse.class)
	public BeaconResponse info(@PathVariable("beaconId") final String beaconId)
	{
		return beaconInfoService.info(beaconId);
	}

	@ResponseBody
	@GetMapping(value = "/{beaconId}/query", produces = APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Query a beacon via a GET request", response = BeaconAlleleResponse.class)
	public BeaconAlleleResponse query(@RequestParam("referenceName") String referenceName,
			@RequestParam("start") Long start, @RequestParam("referenceBases") String referenceBases,
			@RequestParam("alternateBases") String alternateBases, @PathVariable("beaconId") final String beaconId)
	{
		return beaconQueryService.query(referenceName, start, referenceBases, alternateBases, beaconId);
	}

	@ResponseBody
	@PostMapping(value = "/{beaconId}/query", produces = APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Query a beacon via a POST request", response = BeaconAlleleResponse.class)
	public BeaconAlleleResponse query(@PathVariable("beaconId") final String beaconId,
			@RequestBody BeaconAlleleRequest request)
	{
		return beaconQueryService.query(beaconId, request);
	}
}
