package org.molgenis.importer.controller;

import static org.molgenis.importer.controller.DataSetDeleterController.URI;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
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
	private List<Protocol> allProtocols = new ArrayList<Protocol>();

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
	public @ResponseBody
	String delete(@RequestParam("dataset") String datasetIdentifier,
			org.springframework.web.context.request.WebRequest webRequest)
			throws HandleRequestDelegationException, Exception {
		boolean deletemetadata = webRequest.getParameter("deletemetadata") != null;
		DataSet dataSet = DataSet.findByIdentifier(database, datasetIdentifier);
		// list used to check if Features and SubProtocols are used by different
		// datasets
		allProtocols = database.find(Protocol.class);

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

	/**
	 * Deletes the data from a given dataSet
	 * 
	 * @param the DataSet from which the data should be deleted
	 */
	private void deleteData(DataSet dataset) throws DatabaseException {
		List<ObservationSet> observationSets = database.find(
				ObservationSet.class, new QueryRule("partOfDataSet",
						Operator.EQUALS, dataset.getIdValue()));
		for (ObservationSet observationSet : observationSets) {
			deleteObservedValues(observationSet);
		}
		database.remove(observationSets);
	}

	/**
	 * Deletes all ObservedValues from a given observationSet
	 * 
	 * @param the observationSet from which the values should be deleted
	 */
	private void deleteObservedValues(ObservationSet observationSet)
			throws DatabaseException {
		List<ObservedValue> observedValues = database.find(ObservedValue.class,
				new QueryRule("ObservationSet_id", Operator.EQUALS,
						observationSet.getIdValue()));
		database.remove(observedValues);
	}

	/**
	 * Deletes all subprotocols which do not have multiple Protocols referencing them
	 * 
	 * @param the protocols that should be deleted
	 */
	private void deleteProtocol(Protocol protocol) throws DatabaseException {
		boolean deleteInBatch = true;
		List<Protocol> subprotocols = protocol.getSubprotocols();
		// check if any of the subprotocols had subprotocols of its own
		for (Protocol subprotocol : subprotocols) {
			if (subprotocol.getSubprotocols().size() > 0)
				deleteInBatch = false;
		}
		for (Protocol subprotocol : subprotocols) {
			int superprotocolcount = countReferringProtocols(subprotocol);
			// only delete if there is only one parent protocol
			if (superprotocolcount <= 1) {
				if (!deleteInBatch) {
					deleteProtocol(subprotocol);
				} else {
					List<ObservableFeature> features = subprotocol
							.getFeatures();
					deleteFeatures(features);
				}
			}
		}
		List<ObservableFeature> features = protocol.getFeatures();
		if (deleteInBatch) {
			database.remove(subprotocols);
			allProtocols.removeAll(subprotocols);
		}
		database.remove(protocol);
		deleteFeatures(features);
		allProtocols.remove(protocol);
	}
	
	/**
	 * Deletes all features which do not have multiple Protocols referencing them
	 * 
	 * @param the features that should be deleted
	 */
	private void deleteFeatures(List<ObservableFeature> features)
			throws DatabaseException {
		List<ObservableFeature> removableFeatures = new ArrayList<ObservableFeature>();

		for (ObservableFeature feature : features) {
			List<Category> categories = database.find(
					Category.class,
					new QueryRule("observableFeature", Operator.EQUALS, feature
							.getIdValue()));
			deleteCategories(categories);
			int protocolcount = countReferringProtocols(feature);
			if (protocolcount <= 1) {
				removableFeatures.add(feature);
			}
		}
		database.remove(removableFeatures);
	}

	private void deleteCategories(List<Category> categories)
			throws DatabaseException {
		for (Category category : categories) {
			List<CategoricalValue> values = database.find(
					CategoricalValue.class, new QueryRule("value",
							Operator.EQUALS, category.getIdValue()));
			database.remove(values);
		}
		database.remove(categories);
	}
	
	/**
	 * Count the number of times a protocol of feature is referred to from a(n other) protocol
	 * 
	 * @param the feature of protocol that is referred to
	 * 
	 * @return the number of referring protocols
	 */
	private int countReferringProtocols(Characteristic characteristic) {
		int protocolcount = 0;
		Class<? extends Characteristic> clazz = characteristic.getClass();
		for (Protocol p : allProtocols) {
			if ((clazz.equals(ObservableFeature.class)
					&& p.getFeatures_Id().contains(characteristic.getId()) || (clazz
					.equals(Protocol.class) && p.getSubprotocols_Id().contains(
					characteristic.getId())))) {
				protocolcount++;
			}
		}
		return protocolcount;
	}


	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated() {
		return "redirect:/";
	}
}
