package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.DiseaseMatcherController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.diseasemapping.Disease;
import org.molgenis.omx.diseasemapping.DiseaseMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Controller for handling communication between the Data Explorer Disease Matcher module and the DiseaseMapping and
 * Disease datasets.
 * 
 * @author tommydeboer
 */
@Controller
@RequestMapping(BASE_URI)
public class DiseaseMatcherController
{
	/**
	 * The URI to which this controller is mapped.
	 */
	public static final String BASE_URI = "/diseasematcher";

	private DataService dataService;

	/**
	 * Constructor for the DiseaseMatcher controller.
	 * 
	 * @param dataService
	 *            an instance of the MOLGENIS Data Service
	 */
	@Autowired
	public DiseaseMatcherController(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	/**
	 * Get a list of disease names from Disease for a list of OMIM identifiers.
	 * 
	 * @param diseaseIds
	 *            the OMIM identifier(s)
	 * @param query
	 *            eventual data query
	 * @return mapping of OMIM identifiers with corresponding disease names
	 */
	@RequestMapping(value = "/diseaseNames", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Collection<String>> getDiseaseNamesforDiseaseIds(
			@RequestParam("diseaseId") List<String> diseaseIds,
			@RequestParam(value = "query", required = false) List<QueryRule> query)
	{

		Iterable<Disease> diseases;
		if (query == null)
		{
			QueryRule queryRule = new QueryRule(Disease.DISEASEID, Operator.IN, diseaseIds);

			diseases = dataService.findAll(Disease.ENTITY_NAME, new QueryImpl(queryRule), Disease.class);
		}
		else
		{
			QueryRule queryRule = new QueryRule(Disease.DISEASEID, Operator.EQUALS, diseaseIds);
			query.add(queryRule);
			diseases = dataService.findAll(Disease.DISEASEID, new QueryImpl(query), Disease.class);
		}

		// iterate over diseases and order them in a map
		Multimap<String, String> result = HashMultimap.create();
		for (Disease d : diseases)
		{
			result.put(d.getDiseaseId(), d.getDiseaseName());
		}

		Map<String, Collection<String>> jsonResult = result.asMap();

		return jsonResult;

	}

	/**
	 * Finds unique genes in a dataset and returns a slice of that set (for paging) stored in a FindGenesResponse
	 * object.
	 * 
	 * @param request
	 *            a FindRequest
	 * @return a FindGenesResponse
	 */
	@RequestMapping(value = "/genes", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public FindGenesResponse findGenesInDatasetPaged(@Valid @RequestBody FindRequest request)
	{
		Iterable<Entity> entities;
		if (request.getQuery() == null)
		{
			entities = dataService.findAll(request.getDatasetName(), new QueryImpl());
		}
		else
		{
			entities = dataService.findAll(request.getDatasetName(), new QueryImpl(request.getQuery()));
		}

		// TODO use aggregates to get unique values
		// TODO when aggregates are used, use Query pageSize and range to slice the data

		int index = 0;
		int start = request.getStart();
		int stop = request.getStart() + request.getNum();

		Set<String> uniqueGenes = new HashSet<String>();
		List<String> geneList = Lists.newArrayList();
		for (Entity e : entities)
		{
			if (!uniqueGenes.contains(e.getString("geneSymbol")))
			{
				uniqueGenes.add(e.getString("geneSymbol"));

				if (index >= start && index < stop)
				{
					geneList.add(e.getString("geneSymbol"));
				}

				index++;
			}
		}

		FindGenesResponse response = new FindGenesResponse();
		response.setNum(request.getNum());
		response.setStart(request.getStart());
		response.setTotal(index);
		response.setGenes(geneList);

		return response;
	}

	/**
	 * Finds unique diseases in a dataset and returns a slice of that set (for paging) stored in a FindDiseasesResponse
	 * object.
	 * 
	 * @param request
	 *            a FindRequest
	 * @return a FindDiseasesResponse
	 */
	@RequestMapping(value = "/diseases", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public FindDiseasesResponse findDiseasesInDatasetPaged(@Valid @RequestBody FindRequest request)
	{
		// first get all genes from the dataset
		Iterable<Entity> entities;
		if (request.getQuery() == null)
		{
			entities = dataService.findAll(request.getDatasetName(), new QueryImpl());
		}
		else
		{
			entities = dataService.findAll(request.getDatasetName(), new QueryImpl(request.getQuery()));
		}

		List<String> genes = Lists.newArrayList();
		for (Entity e : entities)
		{
			genes.add(e.getString("geneSymbol"));
		}

		if (genes.isEmpty())
		{
			FindDiseasesResponse response = new FindDiseasesResponse();
			response.setNum(request.getNum());
			response.setStart(request.getStart());
			response.setTotal(0);
			response.setDiseases(Collections.<Disease> emptyList());
		}

		// get disease identifiers based on genes from the DiseaseMapping dataset
		QueryRule queryRule = new QueryRule(DiseaseMapping.GENESYMBOL, Operator.IN, genes);
		Query query = new QueryImpl(queryRule);
		Iterable<DiseaseMapping> diseaseMappingIterator = dataService.findAll(DiseaseMapping.ENTITY_NAME, query,
				DiseaseMapping.class);

		List<String> diseaseIds = Lists.newArrayList();
		for (DiseaseMapping dm : diseaseMappingIterator)
		{
			diseaseIds.add(dm.getDiseaseId());
		}

		// get disease names based on disease ids from the Disease dataset
		queryRule = new QueryRule(Disease.DISEASEID, Operator.IN, diseaseIds);
		query = new QueryImpl(queryRule);
		Iterable<Disease> diseases = dataService.findAll(Disease.ENTITY_NAME, query, Disease.class);

		// TODO use aggregates to get unique values

		// get unique diseases only
		List<Disease> uniqueDiseases = Lists.newArrayList();
		Set<String> temp = new HashSet<String>();
		for (Disease d : diseases)
		{
			if (!temp.contains(d.getDiseaseId()))
			{
				temp.add(d.getDiseaseId());
				uniqueDiseases.add(d);
			}
		}

		// TODO ORPHANET is ignored, 21 results instead of 29 with the test set

		// TODO when aggregates are used, use Query pageSize and range to slice the data

		// get specific range
		int index = 0;
		int start = request.getStart();
		int stop = request.getStart() + request.getNum();

		List<Disease> responseDiseases = Lists.newArrayList();

		for (Disease d : uniqueDiseases)
		{
			if (index >= start && index < stop)
			{
				responseDiseases.add(d);
			}
			index++;
		}

		FindDiseasesResponse response = new FindDiseasesResponse();
		response.setNum(request.getNum());
		response.setStart(request.getStart());
		response.setTotal(index);
		response.setDiseases(responseDiseases);

		return response;
	}
}
