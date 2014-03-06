package org.molgenis.omx.biobankconnect.analysis;

import static org.molgenis.omx.biobankconnect.analysis.BiobankAnalysisController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.algorithm.ApplyAlgorithms;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class BiobankAnalysisController extends MolgenisPluginController
{
	public static final String ID = "biobankanalysis";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String ANALYSIS_PROTOCOL_IDENTIFIER = "analysis_protocol";
	private static final String ANALYSIS_SCRIPT_IDENTIFIER = "analysis_script";
	private static final String ANALYSIS_PREDICTION_IDENTIFIER = "analysis_prediction";
	private static final String ANALYSIS_OBSERVATION_IDENTIFIER = "analysis_observation";
	private static final String ANALYSIS_SOURCEDATA_IDENTIFIER = "analysis_sourcedata";

	private final DataService dataService;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private ApplyAlgorithms applyAlgorithms;

	@Autowired
	public BiobankAnalysisController(DataService dataService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("Dataservice is null");
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, Model model)
	{
		Protocol protocol = initMetadata();
		Iterable<DataSet> dataSetIterator = dataService.findAll(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.PROTOCOLUSED, protocol), DataSet.class);
		List<DataSet> allDataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : dataSetIterator)
		{
			allDataSets.add(dataSet);
		}
		model.addAttribute("dataSets", allDataSets);
		if (selectedDataSetId != null)
		{
			model.addAttribute("selectedDataSet", selectedDataSetId);
		}
		if (allDataSets.size() > 0)
		{
			model.addAttribute("sourceDataSets", retrieveQualifiedSourceDataSets());
		}
		return "BiobankAnalysisPlugin";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newanalysis")
	public String addAnalysis(@RequestParam(value = "newDataSet")
	String dataSetName, Model model)
	{
		if (dataSetName.isEmpty()) return init(null, model);
		Protocol protocol = initMetadata();
		String analysisIdentifier = createAnalysisIdentifier(dataSetName);
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, analysisIdentifier), DataSet.class);
		if (dataSet == null)
		{
			dataSet = new DataSet();
			dataSet.setIdentifier(analysisIdentifier);
			dataSet.setName(dataSetName);
			dataSet.setProtocolUsed(protocol);
			dataService.add(DataSet.ENTITY_NAME, dataSet);
			dataService.getCrudRepository(DataSet.ENTITY_NAME).flush();
		}
		return init(dataSet.getId().toString(), model);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/retrievescript")
	@ResponseBody
	public Map<String, Object> retrieve(@RequestBody
	AnalysisComponent request)
	{
		Map<String, Object> results = new HashMap<String, Object>();
		String dataSetId = request.getDataSetId();
		if (dataSetId != null)
		{
			results.put("analyses", retrieveAnalysisComponents(Integer.parseInt(dataSetId)));
		}
		return results;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/runanalysis")
	@ResponseBody
	public Map<Integer, Object> runAnalysis(@RequestBody
	AnalysisComponent request)
	{
		Map<Integer, Object> results = applyAlgorithms.createValueFromAlgorithm("decimal",
				Integer.parseInt(request.getSourceDataSetId()), request.getScript());
		return results;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newsourcedata")
	public String addSourceData(@RequestParam(value = "selectedDataSet")
	String dataSetId, @RequestParam(value = "sourceDataSets")
	String sourceDataSetId, Model model)
	{
		if (dataSetId == null || sourceDataSetId == null) return init(null, model);
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, Integer.parseInt(dataSetId), DataSet.class);
		if (dataSet != null)
		{
			StringBuilder observationidentifier = new StringBuilder();
			observationidentifier.append(dataSet.getIdentifier()).append('_').append(sourceDataSetId);
			ObservationSet os = getObservationSet(observationidentifier.toString());
			if (os == null)
			{
				os = new ObservationSet();
				os.setIdentifier(observationidentifier.toString());
				os.setPartOfDataSet(dataSet);
				dataService.add(ObservationSet.ENTITY_NAME, os);

				IntValue value = new IntValue();
				value.setValue(Integer.parseInt(sourceDataSetId));
				dataService.add(IntValue.ENTITY_NAME, value);

				ObservedValue ov = new ObservedValue();
				ov.setObservationSet(os);
				ov.setFeature(getObservableFeature(ANALYSIS_SOURCEDATA_IDENTIFIER));
				ov.setValue(value);
				dataService.add(ObservedValue.ENTITY_NAME, ov);
				dataService.getCrudRepository(ObservedValue.ENTITY_NAME).flush();
			}
		}
		return init(dataSetId, model);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/savescript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> saveScript(@RequestBody
	AnalysisComponent request)
	{
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", false);
		if (request.getObservationSetId() == null) return results;
		ObservationSet os = dataService.findOne(ObservationSet.ENTITY_NAME,
				Integer.parseInt(request.getObservationSetId()), ObservationSet.class);
		ObservedValue ov = dataService.findOne(
				ObservedValue.ENTITY_NAME,
				new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os).and()
						.eq(ObservedValue.FEATURE, getObservableFeature(ANALYSIS_SCRIPT_IDENTIFIER)),
				ObservedValue.class);

		if (ov == null)
		{
			StringValue value = new StringValue();
			value.setValue(request.getScript() == null ? StringUtils.EMPTY : request.getScript());
			dataService.add(StringValue.ENTITY_NAME, value);

			ov = new ObservedValue();
			ov.setFeature(getObservableFeature(ANALYSIS_SCRIPT_IDENTIFIER));
			ov.setObservationSet(os);
			ov.setValue(value);

			dataService.add(ObservedValue.ENTITY_NAME, ov);
		}
		else
		{
			StringValue value = (StringValue) ov.getValue();
			value.setValue(request.getScript() == null ? StringUtils.EMPTY : request.getScript());
			dataService.update(StringValue.ENTITY_NAME, value);
		}

		dataService.getCrudRepository(ObservedValue.ENTITY_NAME).flush();
		results.put("success", true);
		return results;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/observation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> observation()
	{
		return Collections.emptyMap();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/prediction", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> prediction()
	{
		return Collections.emptyMap();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/evaluation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> analysis()
	{
		return Collections.emptyMap();
	}

	private List<DataSet> retrieveQualifiedSourceDataSets()
	{
		List<DataSet> qualifiedDataSets = new ArrayList<DataSet>();
		Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet sourceDataSet : dataSets)
		{
			String identifier = sourceDataSet.getProtocolUsed().getIdentifier();
			if (!identifier.equals(ANALYSIS_PROTOCOL_IDENTIFIER) && !identifier.equals(PROTOCOL_IDENTIFIER))
			{
				qualifiedDataSets.add(sourceDataSet);
			}
		}
		return qualifiedDataSets;
	}

	private List<AnalysisComponent> retrieveAnalysisComponents(Integer dataSetId)
	{
		List<AnalysisComponent> analyses = new ArrayList<AnalysisComponent>();
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, dataSetId, DataSet.class);
		Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);
		ObservableFeature scriptFeature = getObservableFeature(ANALYSIS_SCRIPT_IDENTIFIER);
		ObservableFeature sourceDataFeature = getObservableFeature(ANALYSIS_SOURCEDATA_IDENTIFIER);
		for (ObservationSet os : observationSets)
		{
			ObservedValue ovSourceData = getObservedValue(os, sourceDataFeature);
			ObservedValue ovScript = getObservedValue(os, scriptFeature);
			if (ovSourceData != null)
			{
				analyses.add(new AnalysisComponent(dataSetId.toString(), ovSourceData.getValue().getString("value"), os
						.getId().toString(), ovScript == null ? StringUtils.EMPTY : ovScript.getValue().getString(
						"value")));
			}
		}
		return analyses;
	}

	private ObservationSet getObservationSet(String identifier)
	{
		ObservationSet observationSet = dataService.findOne(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.IDENTIFIER, identifier), ObservationSet.class);
		return observationSet;
	}

	private ObservableFeature getObservableFeature(String identifier)
	{
		ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, identifier), ObservableFeature.class);
		return feature;
	}

	private ObservedValue getObservedValue(ObservationSet os, ObservableFeature feature)
	{
		ObservedValue ov = dataService.findOne(ObservedValue.ENTITY_NAME,
				new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os).and().eq(ObservedValue.FEATURE, feature),
				ObservedValue.class);
		return ov;
	}

	private String createAnalysisIdentifier(String dataSetName)
	{
		StringBuilder dataSetIdentifier = new StringBuilder();
		dataSetIdentifier.append(userAccountService.getCurrentUser().getUsername()).append('_')
				.append(ANALYSIS_PROTOCOL_IDENTIFIER).append('_').append(dataSetName);
		return dataSetIdentifier.toString();
	}

	private Protocol initMetadata()
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, ANALYSIS_PROTOCOL_IDENTIFIER), Protocol.class);

		if (protocol == null)
		{
			List<ObservableFeature> features = new ArrayList<ObservableFeature>();

			ObservableFeature featureScript = new ObservableFeature();
			featureScript.setName(ANALYSIS_SCRIPT_IDENTIFIER);
			featureScript.setIdentifier(ANALYSIS_SCRIPT_IDENTIFIER);
			featureScript.setDataType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString());
			features.add(featureScript);

			ObservableFeature featurePrediction = new ObservableFeature();
			featurePrediction.setName(ANALYSIS_PREDICTION_IDENTIFIER);
			featurePrediction.setIdentifier(ANALYSIS_PREDICTION_IDENTIFIER);
			featureScript.setDataType(MolgenisFieldTypes.FieldTypeEnum.DECIMAL.toString());
			features.add(featurePrediction);

			ObservableFeature featureObservation = new ObservableFeature();
			featureObservation.setName(ANALYSIS_OBSERVATION_IDENTIFIER);
			featureObservation.setIdentifier(ANALYSIS_OBSERVATION_IDENTIFIER);
			featureScript.setDataType(MolgenisFieldTypes.FieldTypeEnum.DECIMAL.toString());
			features.add(featureObservation);

			ObservableFeature featureSourceData = new ObservableFeature();
			featureSourceData.setName(ANALYSIS_SOURCEDATA_IDENTIFIER);
			featureSourceData.setIdentifier(ANALYSIS_SOURCEDATA_IDENTIFIER);
			featureScript.setDataType(MolgenisFieldTypes.FieldTypeEnum.INT.toString());
			features.add(featureSourceData);

			protocol = new Protocol();
			protocol.setIdentifier(ANALYSIS_PROTOCOL_IDENTIFIER);
			protocol.setName(ANALYSIS_PROTOCOL_IDENTIFIER);
			protocol.setFeatures(features);

			dataService.add(ObservableFeature.ENTITY_NAME, features);
			dataService.add(Protocol.ENTITY_NAME, protocol);
			dataService.getCrudRepository(Protocol.ENTITY_NAME).flush();
		}
		return protocol;
	}

	class AnalysisComponent
	{
		private final String dataSetId;
		private final String sourceDataSetId;
		private final String observationSetId;
		private final String script;

		public AnalysisComponent(String dataSetId, String sourceDataSetId, String observationSetId, String script)
		{
			this.dataSetId = dataSetId;
			this.sourceDataSetId = sourceDataSetId;
			this.observationSetId = observationSetId;
			this.script = script;
		}

		public String getSourceDataSetId()
		{
			return sourceDataSetId;
		}

		public String getObservationSetId()
		{
			return observationSetId;
		}

		public String getScript()
		{
			return script;
		}

		public String getDataSetId()
		{
			return dataSetId;
		}
	}
}