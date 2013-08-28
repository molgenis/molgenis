package org.molgenis.protocolmanager.controller;

import static org.molgenis.protocolmanager.controller.ProtocolManagerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.search.SearchService;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.WritableTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
public class ProtocolManagerController extends MolgenisPlugin
{
	public static final String URI = "/plugin/protocolmanager";
	private static final Logger logger = Logger.getLogger(ProtocolManagerController.class);

	@Autowired
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private SearchService searchService;

	public ProtocolManagerController()
	{
		super(URI);
	}

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

		String resultsTableJavascriptFile = molgenisSettings.getProperty("protocolmanager.resultstable.js",
				"MultiObservationSetTable.js");

		model.addAttribute("resultsTableJavascriptFile", resultsTableJavascriptFile);

		return "view-protocolmanager";
	}

	@RequestMapping(value = "/protocolAmount", method = POST)
	public void protocolAmount(@RequestBody
	ProtocolAmount protAmount, HttpServletResponse response)
	{

	}

	@RequestMapping(value = "/save", method = POST)
	public void save(@RequestBody
	ProtocolManagerRequest protocolManagerRequest, HttpServletResponse response) throws IOException, DatabaseException,
			TableException, ValueConverterException
	{

		Protocol p = Protocol.findByIdentifier(database, protocolManagerRequest.getProtocolIdentifier());

		if (p == null)
		{
			throw new DatabaseException("There is no protocol with identifier: "
					+ protocolManagerRequest.getProtocolIdentifier().toString());
		}
		else
		{
			for (Map<String, String> m : protocolManagerRequest.getProtocolManagerRequest())
			{
				WritableTuple tuple = new KeyValueTuple();
				for (Entry<String, String> entry : m.entrySet())
				{
					ObservationSet obs = new ObservationSet();
					List<DataSet> allDataSets = database.find(DataSet.class);
					obs.setPartOfDataSet(allDataSets.get(0));
					database.add(obs);
					for (ObservableFeature obsF : p.getFeatures())
					{
						tuple.set(entry.getKey(), entry.getValue());
						if (obsF.getName().equals(entry.getKey()))
						{

							ValueConverter val = new ValueConverter(database);
							Value a = val.fromTuple(tuple, entry.getKey(), obsF);
							database.add(a);
							ObservedValue observedValue = new ObservedValue();
							observedValue.setValue(a);
							observedValue.setFeature(obsF);
							observedValue.setObservationSet(obs);
							database.add(observedValue);
						}
					}
				}
			}
		}
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

	public static class ProtocolAmount
	{
		private Integer protocolAmount;

		public Integer getProtocolAmount()
		{
			return protocolAmount;
		}

		public void setProtocolAmount(Integer protocolAmount)
		{
			this.protocolAmount = protocolAmount;
		}

	}

	private static class ProtocolManagerRequest
	{
		@NotNull
		@Size(min = 1)
		private List<Map<String, String>> protocolManagerRequest;
		@NotNull
		private String protocolIdentifier;

		public List<Map<String, String>> getProtocolManagerRequest()
		{
			return protocolManagerRequest;

		}

		@SuppressWarnings("unused")
		public void setMapOfValuesList(List<Map<String, String>> protocolManagerRequest)
		{
			this.protocolManagerRequest = protocolManagerRequest;
		}

		public String getProtocolIdentifier()
		{
			return protocolIdentifier;
		}

		public void setProtocolIdentifier(String protocolIdentifier)
		{
			this.protocolIdentifier = protocolIdentifier;
		}
	}
}
