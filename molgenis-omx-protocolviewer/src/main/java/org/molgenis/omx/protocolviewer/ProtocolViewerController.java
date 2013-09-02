package org.molgenis.omx.protocolviewer;

import static org.molgenis.omx.protocolviewer.ProtocolViewerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.tuple.KeyValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class ProtocolViewerController extends MolgenisPlugin
{
	public static final String URI = "/plugin/protocolviewer";

	public static final String KEY_ACTION_DOWNLOAD = "plugin.catalogue.action.download";
	private static final boolean DEFAULT_KEY_ACTION_DOWNLOAD = true;
	public static final String KEY_ACTION_ORDER = "plugin.catalogue.action.order";
	private static final boolean DEFAULT_KEY_ACTION_ORDER = true;

	private final Database database;
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public ProtocolViewerController(Database database, MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("MolgenisSettings is null");
		this.database = database;
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = GET)
	public String init(Model model) throws DatabaseException
	{
		List<DataSet> dataSets = database.query(DataSet.class).find();

		// create new model
		ProtocolViewer protocolViewer = new ProtocolViewer();
		protocolViewer.setAuthenticated(database.getLogin() != null ? database.getLogin().isAuthenticated() : false);
		protocolViewer.setDataSets(dataSets);

		protocolViewer.setEnableDownloadAction(molgenisSettings.getBooleanProperty(KEY_ACTION_DOWNLOAD,
				DEFAULT_KEY_ACTION_DOWNLOAD));
		protocolViewer.setEnableOrderAction(molgenisSettings.getBooleanProperty(KEY_ACTION_ORDER,
				DEFAULT_KEY_ACTION_ORDER));
		model.addAttribute("model", protocolViewer);

		return "view-protocolviewer";
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException,
			DatabaseException
	{
		// get features
		String featuresStr = request.getParameter("features");
		List<ObservableFeature> features = null;
		if (featuresStr != null && !featuresStr.isEmpty())
		{
			String[] featuresStrArr = request.getParameter("features").split(",");
			List<Integer> featureIds = new ArrayList<Integer>(featuresStrArr.length);
			for (String featureStr : featuresStrArr)
				featureIds.add(Integer.valueOf(featureStr));
			features = findFeatures(database, featureIds);
		}

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
			if (features != null)
			{
				for (ObservableFeature feature : features)
				{
					KeyValueTuple tuple = new KeyValueTuple();
					tuple.set(header.get(0), feature.getIdentifier());
					tuple.set(header.get(1), feature.getName());
					tuple.set(header.get(2), feature.getDescription());
					sheetWriter.write(tuple);
				}
			}
		}
		finally
		{
			excelWriter.close();
		}
	}

	private List<ObservableFeature> findFeatures(Database db, List<Integer> featureIds) throws DatabaseException
	{
		if (featureIds == null || featureIds.isEmpty()) return null;
		List<ObservableFeature> features = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		return features;
	}
}
