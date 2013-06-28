package org.molgenis.omx.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Protocol viewer controller
 */
public class ProtocolViewerController extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	public static final String KEY_ACTION_DOWNLOAD = "plugin.catalogue.action.download";
	private static final boolean DEFAULT_KEY_ACTION_DOWNLOAD = true;
	public static final String KEY_ACTION_VIEW = "plugin.catalogue.action.view";
	private static final boolean DEFAULT_KEY_ACTION_VIEW = true;
	public static final String KEY_ACTION_ORDER = "plugin.catalogue.action.order";
	private static final boolean DEFAULT_KEY_ACTION_ORDER = true;

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
		s.append("<link rel=\"stylesheet\" href=\"css/chosen.css\" type=\"text/css\">");
		s.append("<script type=\"text/javascript\" src=\"js/protocolviewer.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.dynatree.min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.fileDownload-min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.validate.min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/chosen.jquery.min.js\"></script>");
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
		this.protocolViewer.setDataSets(dataSets);

		this.protocolViewer.setEnableDownloadAction(getMolgenisSettingFlag(KEY_ACTION_DOWNLOAD,
				DEFAULT_KEY_ACTION_DOWNLOAD));
		this.protocolViewer.setEnableViewAction(getMolgenisSettingFlag(KEY_ACTION_VIEW, DEFAULT_KEY_ACTION_VIEW));
		this.protocolViewer.setEnableOrderAction(getMolgenisSettingFlag(KEY_ACTION_ORDER, DEFAULT_KEY_ACTION_ORDER));
	}

	private List<ObservableFeature> findFeatures(Database db, List<Integer> featureIds) throws DatabaseException
	{
		if (featureIds == null || featureIds.isEmpty()) return null;
		List<ObservableFeature> features = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		return features;
	}
}