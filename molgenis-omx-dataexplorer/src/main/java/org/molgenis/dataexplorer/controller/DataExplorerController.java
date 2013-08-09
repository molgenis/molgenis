package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.ValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for the data explorer.
 * 
 * The implementation javascript file for the resultstable is defined in a MolgenisSettings property named
 * 'dataexplorer.resultstable.js' possible values are '/js/SingleObservationSetTable.js' or
 * '/js/MultiObservationSetTable.js' with '/js/MultiObservationSetTable.js' as the default
 * 
 * @author erwin
 * 
 */
@Controller
@RequestMapping(URI)
public class DataExplorerController
{
	public static final String URI = "/plugin/dataexplorer";
	private static final Logger logger = Logger.getLogger(DataExplorerController.class);
	private static final int DOWNLOAD_SEARCH_LIMIT = 1000;
    private static final String[] runtimeProperties = {"app.href.logo", "app.href.css"};

	@Autowired
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private SearchService searchService;

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{

		String resultsTableJavascriptFile = molgenisSettings.getProperty("dataexplorer.resultstable.js",
				"/js/MultiObservationSetTable.js");

		model.addAttribute("resultsTableJavascriptFile", resultsTableJavascriptFile);

        for (final String property : runtimeProperties)
        {
            final String value = molgenisSettings.getProperty(property);
            if (StringUtils.isNotBlank(value))
            {
                model.addAttribute(property.replaceAll("\\.", "_"), value);
            }
        }

		return "dataexplorer";
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("searchRequest")
	String searchRequest, HttpServletResponse response) throws IOException, DatabaseException, TableException
	{
		searchRequest = URLDecoder.decode(searchRequest, "UTF-8");
		logger.info("Download request: [" + searchRequest + "]");

		SearchRequest request = new GsonHttpMessageConverter().getGson().fromJson(searchRequest, SearchRequest.class);
		request.getQueryRules().add(new QueryRule(Operator.LIMIT, DOWNLOAD_SEARCH_LIMIT));

		QueryRule offsetRule = new QueryRule(Operator.OFFSET, 0);
		request.getQueryRules().add(offsetRule);

		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		TupleWriter tupleWriter = null;
		try
		{
			tupleWriter = new CsvWriter(response.getWriter());

			// The fieldsToReturn contain identifiers, we need the names
			tupleWriter.write(getFeatureNames(request.getFieldsToReturn()));

			int count = 0;
			SearchResult searchResult;

			do
			{
				offsetRule.setValue(count);
				searchResult = searchService.search(request);

				for (Hit hit : searchResult.getSearchHits())
				{
					List<Object> values = new ArrayList<Object>();
					for (String field : request.getFieldsToReturn())
					{
						values.add(hit.getColumnValueMap().get(field));
					}

					tupleWriter.write(new ValueTuple(values));
				}

				count += searchResult.getSearchHits().size();
			}
			while (count < searchResult.getTotalHitCount());
		}
		finally
		{
			IOUtils.closeQuietly(tupleWriter);
		}

	}

	private Tuple getFeatureNames(List<String> identifiers) throws DatabaseException
	{
		List<ObservableFeature> features = database.query(ObservableFeature.class)
				.in(ObservableFeature.IDENTIFIER, identifiers).find();

		// Keep order the same
		Map<String, String> nameByIdentifier = new HashMap<String, String>();
		for (ObservableFeature feature : features)
		{
			nameByIdentifier.put(feature.getIdentifier(), feature.getName());
		}

		List<String> names = new ArrayList<String>();
		for (String identifier : identifiers)
		{
			names.add(nameByIdentifier.get(identifier));
		}

		return new ValueTuple(names);
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}

	/**
	 * When someone directly accesses /dataexplorer and is not logged in an DataAccessException is thrown, redirect him
	 * to the home page
	 * 
	 * @return
	 */
	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated()
	{
		return "redirect:/";
	}
}
