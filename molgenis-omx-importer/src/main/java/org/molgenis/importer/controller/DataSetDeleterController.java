package org.molgenis.importer.controller;

import static org.molgenis.importer.controller.DataSetDeleterController.URI;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.search.SearchService;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class DataSetDeleterController {
	public static final String URI = "/plugin/datasetdeleter";
	private static final Logger logger = Logger
			.getLogger(DataSetDeleterController.class);

	@Autowired
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private SearchService searchService;

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception {
		return "datasetdeleter";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public @ResponseBody String delete(@RequestParam("dataset") String datasetIdentifier,
			org.springframework.web.context.request.WebRequest webRequest)
			throws HandleRequestDelegationException, Exception {
		boolean deletemetadata = webRequest.getParameter("deletemetadata") != null;
		DataSet dataSet = DataSet.findByIdentifier(database, datasetIdentifier);
		String dataSetName = dataSet.getName();
		deleteData(dataSet);
		if (deletemetadata) {
			Protocol protocolUsed = dataSet.getProtocolUsed();
			database.remove(dataSet);
			deleteProtocol(protocolUsed);
		} else {
			searchService.deleteDocumentsByType(dataSet.getIdentifier());
		}
		return dataSetName;
	}

	private void deleteData(DataSet dataset) throws DatabaseException {
		List<ObservationSet> observationSets = database.find(
				ObservationSet.class, new QueryRule("partOfDataSet",
						Operator.EQUALS, dataset.getIdValue()));
		for (ObservationSet observationSet : observationSets) {
			deleteObservedValues(observationSet);
			database.remove(observationSet);
		}
	}

	private void deleteObservedValues(ObservationSet observationSet)
			throws DatabaseException {
		List<ObservedValue> observedValues = database.find(ObservedValue.class,
				new QueryRule("ObservationSet_id", Operator.EQUALS,
						observationSet.getIdValue()));
		for (ObservedValue value : observedValues) {
			database.remove(value);
		}
	}

	private void deleteProtocol(Protocol protocol) throws DatabaseException {
		for (Protocol subprotocol : protocol.getSubprotocols()) {
			int superprotocolcount = database.count(
					Protocol.class,
					new QueryRule("subprotocols", Operator.EQUALS, subprotocol
							.getIdValue()));
			if (superprotocolcount == 1) {
				deleteProtocol(subprotocol);
			}
		}
		List<ObservableFeature> features = protocol.getFeatures();
		database.remove(protocol);
		deleteFeatures(features);
	}

	private void deleteFeatures(List<ObservableFeature> features)
			throws DatabaseException {
		for (ObservableFeature feature : features) {
			List<Category> categories = database.find(
					Category.class,
					new QueryRule("observableFeature", Operator.EQUALS, feature
							.getIdValue()));
			deleteCategories(categories);
			int protocolcount = database.count(Protocol.class, new QueryRule(
					"Features", Operator.EQUALS, feature.getIdValue()));
			if (protocolcount == 1) {
				database.remove(feature);
			}
		}
	}

	private void deleteCategories(List<Category> categories)
			throws DatabaseException {
		for (Category category : categories) {
			List<CategoricalValue> values = database.find(
					CategoricalValue.class, new QueryRule("value",
							Operator.EQUALS, category.getIdValue()));
			for (CategoricalValue categoricalValue : values) {
				database.remove(categoricalValue);
			}
			database.remove(category);
		}
	}

	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated() {
		return "redirect:/";
	}
}
