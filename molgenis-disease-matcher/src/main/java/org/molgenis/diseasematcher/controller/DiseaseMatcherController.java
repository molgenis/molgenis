package org.molgenis.diseasematcher.controller;

import static org.molgenis.diseasematcher.controller.DiseaseMatcherController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(BASE_URI)
public class DiseaseMatcherController
{
	public static final String BASE_URI = "/diseasematcher";
	private DataService dataService;

	@Autowired
	public DiseaseMatcherController(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RequestMapping(value = "/diseases", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<DiseaseMapping> findDiseases(@RequestBody FindDiseasesRequest request)
	{
		QueryRule queryRule = new QueryRule(Disease.DISEASEID, Operator.IN, request.getGeneSymbols());
		Query query = new QueryImpl(queryRule);

		Iterator<DiseaseMapping> it = dataService.findAll(DiseaseMapping.ENTITY_NAME, query, DiseaseMapping.class)
				.iterator();
		List<DiseaseMapping> diseases = Lists.newArrayList(it);

		// get unique
		for (DiseaseMapping d : diseases)
		{

		}

		return diseases;
	}
}
