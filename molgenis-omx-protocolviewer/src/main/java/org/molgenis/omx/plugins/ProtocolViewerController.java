package org.molgenis.omx.plugins;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.omx.dataset.DataSetViewerPlugin;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Protocol viewer controller
 */
public class ProtocolViewerController extends PluginModel<Entity>
{
	private static final long serialVersionUID = -6143910771849972946L;
	private static final String KEY_SHOW_VIEW_BUTTON = "plugin.catalogue.showviewbutton";
	private static final boolean DEFAULT_KEY_SHOW_VIEW_BUTTON = true;
	private static final String KEY_SHOW_SAVE_SELECTION_BUTTON = "plugin.catalogue.showorderbutton";
	private static final boolean DEFAULT_KEY_SAVE_SELECTION_BUTTON = true;

	/** Protocol viewer model */
	private ProtocolViewer protocolViewer;

	public ProtocolViewerController(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	public ProtocolViewer getMyModel()
	{
		return protocolViewer;
	}

	@Override
	public String getViewName()
	{
		return ProtocolViewer.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + ProtocolViewer.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<link rel=\"stylesheet\" href=\"css/protocolviewer.css\" type=\"text/css\">");
		s.append("<link rel=\"stylesheet\" href=\"css/ui.dynatree.css\" type=\"text/css\">");
		s.append("<script type=\"text/javascript\" src=\"js/protocolviewer.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.dynatree.min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.fileDownload-min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.validate.min.js\"></script>");
		return s.toString();
	}

	private boolean getMolgenisSettingFlag(String key, boolean defaultValue)
	{
		try
		{
			MolgenisSettings molgenisSettings = ApplicationContextProvider.getApplicationContext().getBean(
					MolgenisSettings.class);
			String property = molgenisSettings.getProperty(key, Boolean.toString(defaultValue));
			return Boolean.valueOf(property);
		}
		catch (NoSuchBeanDefinitionException e)
		{
			logger.warn(e);
			return defaultValue;
		}
	}

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream out) throws Exception
	{

		if (out == null)
		{
			this.handleRequest(db, request);
			return Show.SHOW_MAIN;
		}

		Object src = null;
		if (request.getAction().equals("download_json_getdataset"))
		{
			Integer dataSetId = request.getInt("datasetid");
			List<DataSet> dataSets = db.find(DataSet.class, new QueryRule(DataSet.ID, Operator.EQUALS, dataSetId));
			if (dataSets != null && !dataSets.isEmpty()) src = toJSDataSet(db, dataSets.get(0));
		}
		else if (request.getAction().equals("download_json_getfeature"))
		{
			Integer featureId = request.getInt("featureid");
			List<ObservableFeature> features = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
					Operator.EQUALS, featureId));
			if (features != null && !features.isEmpty()) src = toJSFeature(db, features.get(0));
		}
		else if (request.getAction().equals("download_json_getprotocol"))
		{
			Integer id = request.getInt("id");

			boolean recursive = request.getBoolean("recursive");

			List<Protocol> protocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.EQUALS, id));

			if (protocols != null && !protocols.isEmpty()) src = toJSProtocol(db, protocols.get(0), recursive);
		}
		else if (request.getAction().equals("download_json_searchdataset"))
		{
			String query = request.getString("query");
			Integer datasetID = request.getInt("id");
			List<Protocol> topProtocol = null;
			List<DataSet> dataSets = db.find(DataSet.class, new QueryRule(DataSet.ID, Operator.EQUALS, datasetID));
			if (dataSets != null && !dataSets.isEmpty()) topProtocol = db.find(Protocol.class, new QueryRule(
					Protocol.ID, Operator.EQUALS, dataSets.get(0).getProtocolUsed_Id()));
			List<Protocol> listOfProtocols = db
					.find(Protocol.class, new QueryRule(Protocol.NAME, Operator.LIKE, query));
			List<ObservableFeature> listOfFeaturesByName = db.find(ObservableFeature.class, new QueryRule(
					ObservableFeature.NAME, Operator.LIKE, query));
			List<ObservableFeature> listOfFeaturesByDescription = db.find(ObservableFeature.class, new QueryRule(
					ObservableFeature.DESCRIPTION, Operator.LIKE, query));
			List<Category> listOfCategories = db.find(Category.class, new QueryRule(Category.DESCRIPTION,
					Operator.LIKE, query));
			if (listOfCategories.isEmpty() && listOfFeaturesByName.isEmpty() && listOfFeaturesByDescription.isEmpty()
					&& listOfProtocols.isEmpty())
			{
				src = toJSProtocol(db, topProtocol.get(0), false);
			}
			else if (topProtocol != null && !topProtocol.isEmpty()) src = searchProtocol(db, topProtocol.get(0), query,
					true);
		}
		else
		{
			throw new RuntimeException("unknown action: " + request.getAction());
		}

		// serialize object to json
		if (src != null)
		{
			Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
			try
			{
				new Gson().toJson(src, writer);
			}
			finally
			{
				writer.close();
			}
		}
		return Show.SHOW_MAIN;
	}

	/**
	 * This function is to search user-typed query in all the features. The searching starts from the top-protocol (the
	 * one that is used to define the dataset), it recursively calls itself until reaching the features. If the query is
	 * found in some features, their corresponding protocols will be informed that the features have been matched. Those
	 * protocols which have successful "hits" are sent back to client side for display.
	 * 
	 * @param db
	 * @param topProtocol
	 * @param query
	 * @param expanded
	 * @return
	 * @throws DatabaseException
	 */
	private JSProtocol searchProtocol(Database db, Protocol topProtocol, String query, boolean expanded)
			throws DatabaseException
	{
		query = query.toLowerCase();
		JSProtocol jsProtocol = null;
		List<Protocol> subProtocols = topProtocol.getSubprotocols();
		List<JSProtocol> jsSubProtocols = new ArrayList<JSProtocol>();
		List<JSFeature> jsFeatures = new ArrayList<JSFeature>();

		if (subProtocols != null && !subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				if (p.getName().toLowerCase().contains(query))
				{
					jsSubProtocols.add(toJSProtocol(db, p, true));
				}
				else
				{
					JSProtocol subJSProtocol = searchProtocol(db, p, query, expanded);
					if (subJSProtocol != null) jsSubProtocols.add(subJSProtocol);
				}
			}
		}
		else
		{
			for (ObservableFeature feature : topProtocol.getFeatures())
			{

				if (feature.getName().toLowerCase().contains(query))
				{
					jsFeatures.add(toJSFeature(db, feature));
				}
				else
				{
					Map<String, String> i18nDescription = new Gson().fromJson(feature.getDescription(),
							new TypeToken<Map<String, String>>()
							{
							}.getType());

					boolean descriptionMatch = false;
					if (i18nDescription != null)
					{
						for (String value : i18nDescription.values())
						{
							if (value.toLowerCase().contains(query))
							{
								descriptionMatch = true;
								break;
							}
						}
					}
					if (descriptionMatch) jsFeatures.add(toJSFeature(db, feature));
					else
					{
						List<Category> categories = findCategories(db, feature);
						if (categories != null && !categories.isEmpty())
						{
							for (Category c : findCategories(db, feature))
							{
								if (c.getDescription() != null && c.getDescription().toLowerCase().contains(query))
								{
									jsFeatures.add(toJSFeature(db, feature));
								}
							}
						}
					}
				}
			}
		}

		if (!jsSubProtocols.isEmpty() || !jsFeatures.isEmpty()) jsProtocol = new JSProtocol(topProtocol, jsFeatures,
				jsSubProtocols);

		return jsProtocol;
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws Exception
	{
		MolgenisRequest req = request;
		HttpServletResponse response = req.getResponse();

		// get data set
		Integer dataSetId = request.getInt("datasetid");

		// get features
		String featuresStr = request.getString("features");
		List<ObservableFeature> features = null;
		if (featuresStr != null && !featuresStr.isEmpty())
		{
			String[] featuresStrArr = request.getString("features").split(",");
			List<Integer> featureIds = new ArrayList<Integer>(featuresStrArr.length);
			for (String featureStr : featuresStrArr)
				featureIds.add(Integer.valueOf(featureStr));
			features = findFeatures(db, featureIds);
		}

		if (request.getAction().equals("download_xls"))
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm");
			String fileName = "variables_" + dateFormat.format(new Date()) + ".xls";

			// write response headers
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

			// write excel file
			List<String> header = Arrays.asList("Id", "Variable", "Description");
			ExcelWriter excelWriter = new ExcelWriter(response.getOutputStream());
			try
			{
				TupleWriter sheetWriter = excelWriter.createTupleWriter("Variables");
				sheetWriter.writeColNames(header);

				for (ObservableFeature feature : features)
				{
					KeyValueTuple tuple = new KeyValueTuple();
					tuple.set(header.get(0), feature.getIdentifier());
					tuple.set(header.get(1), feature.getName());
					tuple.set(header.get(2), feature.getDescription());
					sheetWriter.write(tuple);
				}
			}
			finally
			{
				excelWriter.close();
			}
		}
		else if (request.getAction().equals("download_viewer"))
		{
			req.getRequest().getSession().setAttribute("selectedObservableFeatures", features);

			String dataSetViewerName = this.getDataSetViewerName();
			if (dataSetViewerName != null)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(req.getAppLocation());
				sb.append("/molgenis.do?__target=").append(dataSetViewerName);
				sb.append("&select=").append(dataSetViewerName);
				sb.append("&__action=selectDataSet");
				sb.append("&dataSetId=").append(dataSetId);
				response.sendRedirect(sb.toString());
			}
		}

	}

	/*
	 * Find the name of the DataSetViewer for user in a url. For now if there are multiple DataSetViewers it returns the
	 * first Returns null if not found
	 */
	private String getDataSetViewerName()
	{
		ScreenController<?> menu = getParent();
		for (ScreenController<?> controller : menu.getAllChildren())
		{
			if (controller instanceof DataSetViewerPlugin)
			{
				return controller.getName();
			}
		}

		return null;
	}

	// TODO reload should throw DatabaseException
	@Override
	public void reload(Database db)
	{
		List<DataSet> dataSets;

		try
		{
			dataSets = db.query(DataSet.class).find();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

		// create new model
		this.protocolViewer = new ProtocolViewer();
		this.protocolViewer.setAuthenticated(db.getLogin() != null ? db.getLogin().isAuthenticated() : false);
		List<JSDataSet> jsDataSets;
		if (dataSets != null && !dataSets.isEmpty())
		{
			jsDataSets = new ArrayList<JSDataSet>(dataSets.size());
			for (DataSet dataSet : dataSets)
				// performance: do not add protocols
				jsDataSets.add(new JSDataSet(dataSet, null));
		}
		else
		{
			jsDataSets = Collections.emptyList();
		}
		this.protocolViewer.setDataSets(jsDataSets);

		this.protocolViewer
				.setShowViewButton(getMolgenisSettingFlag(KEY_SHOW_VIEW_BUTTON, DEFAULT_KEY_SHOW_VIEW_BUTTON));
		this.protocolViewer.setShowOrderButton(getMolgenisSettingFlag(KEY_SHOW_SAVE_SELECTION_BUTTON,
				DEFAULT_KEY_SAVE_SELECTION_BUTTON));
	}

	private List<Category> findCategories(Database db, ObservableFeature feature) throws DatabaseException
	{
		// TODO can we get by (internal) id instead of identifier?
		List<Category> categories = db.find(Category.class, new QueryRule(Category.OBSERVABLEFEATURE_IDENTIFIER,
				Operator.EQUALS, feature.getIdentifier()));
		return categories;
	}

	private List<Protocol> findSubProtocols(Database db, Protocol protocol) throws DatabaseException
	{
		List<Integer> subProtocolIds = protocol.getSubprotocols_Id();
		if (subProtocolIds == null || subProtocolIds.isEmpty()) return Collections.emptyList();

		List<Protocol> protocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.IN, subProtocolIds));
		return protocols;
	}

	private List<ObservableFeature> findFeatures(Database db, List<Integer> featureIds) throws DatabaseException
	{
		if (featureIds == null || featureIds.isEmpty()) return null;
		List<ObservableFeature> features = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		return features;
	}

	private OntologyTerm findOntologyTerm(Database db, ObservableFeature feature) throws DatabaseException
	{
		Integer unitId = feature.getUnit_Id();
		if (unitId == null) return null;

		List<OntologyTerm> protocols = db.find(OntologyTerm.class,
				new QueryRule(OntologyTerm.ID, Operator.IN, Arrays.asList(unitId)));
		return protocols.iterator().next();
	}

	private JSDataSet toJSDataSet(Database db, DataSet dataSet) throws DatabaseException
	{
		Integer protocolId = dataSet.getProtocolUsed_Id();
		List<Protocol> protocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.EQUALS, protocolId));
		JSProtocol jsProtocol = null;
		if (protocols != null && !protocols.isEmpty()) jsProtocol = toJSProtocol(db, protocols.get(0), false);
		return new JSDataSet(dataSet, jsProtocol);
	}

	private JSProtocol toJSProtocol(Database db, Protocol protocol, boolean recursive) throws DatabaseException
	{
		// get features
		List<JSFeature> jsFeatures = null;
		List<ObservableFeature> features = findFeatures(db, protocol.getFeatures_Id());
		if (features != null && !features.isEmpty())
		{
			jsFeatures = new ArrayList<JSFeature>(features.size());
			for (ObservableFeature feature : features)
				jsFeatures.add(toJSFeature(db, feature));

			// sort alphabetically by name
			Collections.sort(jsFeatures, new Comparator<JSFeature>()
			{
				@Override
				public int compare(JSFeature o1, JSFeature o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
		}

		// get sub protocols (recursive)
		List<JSProtocol> jsSubProtocols = null;
		List<Protocol> subProtocols = findSubProtocols(db, protocol);
		if (subProtocols != null && !subProtocols.isEmpty())
		{
			jsSubProtocols = new ArrayList<JSProtocol>(subProtocols.size());

			for (Protocol subProtocol : subProtocols)
			{
				if (recursive)
				{
					jsSubProtocols.add(toJSProtocol(db, subProtocol, recursive));
				}
				else
				{
					jsSubProtocols.add(new JSProtocol(subProtocol, null, null));
				}

			}

			// sort alphabetically by name
			Collections.sort(jsSubProtocols, new Comparator<JSProtocol>()
			{
				@Override
				public int compare(JSProtocol o1, JSProtocol o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
		}

		return new JSProtocol(protocol, jsFeatures, jsSubProtocols);
	}

	private JSFeature toJSFeature(Database db, ObservableFeature feature) throws DatabaseException
	{
		List<JSCategory> jsCategories = null;

		List<Category> categories = findCategories(db, feature);
		if (categories != null && !categories.isEmpty())
		{
			jsCategories = new ArrayList<JSCategory>(categories.size());
			for (Category category : categories)
				jsCategories.add(new JSCategory(category));

			// sort alphabetically by name
			Collections.sort(jsCategories, new Comparator<JSCategory>()
			{
				@Override
				public int compare(JSCategory o1, JSCategory o2)
				{
					String c1 = o1.getCode();
					String c2 = o2.getCode();
					try
					{
						// try numerical sort
						return Integer.valueOf(c1).compareTo(Integer.valueOf(c2));
					}
					catch (NumberFormatException e)
					{
						// alphabetic sort
						return c1.compareTo(c2);
					}
				}
			});
		}

		OntologyTerm ontologyTerm = findOntologyTerm(db, feature);
		return new JSFeature(feature, jsCategories, ontologyTerm != null ? new JSOntologyTerm(ontologyTerm) : null);
	}

	public static class JSDataSet implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int id;
		private final String name;
		private final JSProtocol protocol;

		public JSDataSet(DataSet dataSet, JSProtocol protocol)
		{
			this.id = dataSet.getId();
			this.name = dataSet.getName();
			this.protocol = protocol;
		}

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public JSProtocol getProtocol()
		{
			return protocol;
		}
	}

	@SuppressWarnings("unused")
	private static class JSProtocol implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int id;
		private final String name;
		private final List<JSFeature> features;
		private final List<JSProtocol> subProtocols;

		public JSProtocol(Protocol protocol, List<JSFeature> features, List<JSProtocol> subProtocols)
		{
			this.id = protocol.getId();
			this.name = protocol.getName();
			this.features = features;
			this.subProtocols = subProtocols;
		}

		public String getName()
		{
			return name;
		}
	}

	@SuppressWarnings("unused")
	private static class JSFeature implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int id;
		private final String name;
		private final Map<String, String> i18nDescription;
		private final String dataType;
		private final JSOntologyTerm unit;
		private final List<JSCategory> categories;

		public JSFeature(ObservableFeature feature, List<JSCategory> categories, JSOntologyTerm unit)
		{
			this.id = feature.getId();
			this.name = feature.getName();
			String description = feature.getDescription();
			if (description != null && (!description.startsWith("{") || !description.endsWith("}")))
			{
				description = " {\"en\":\"" + description + "\"}";
			}
			this.i18nDescription = new Gson().fromJson(description, new TypeToken<Map<String, String>>()
			{
			}.getType());
			this.dataType = feature.getDataType();
			this.unit = unit;
			this.categories = categories;
		}

		public String getName()
		{
			return name;
		}
	}

	@SuppressWarnings("unused")
	private static class JSOntologyTerm implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int id;
		private final String name;

		public JSOntologyTerm(OntologyTerm ontologyTerm)
		{
			this.id = ontologyTerm.getId();
			this.name = ontologyTerm.getName();
		}
	}

	@SuppressWarnings("unused")
	private static class JSCategory implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int id;
		private final String name;
		private final String code;
		private final String description;

		public JSCategory(Category category)
		{
			this.id = category.getId();
			this.name = category.getName();
			this.code = category.getValueCode();
			this.description = category.getDescription();
		}

		public String getCode()
		{
			return code;
		}
	}
}