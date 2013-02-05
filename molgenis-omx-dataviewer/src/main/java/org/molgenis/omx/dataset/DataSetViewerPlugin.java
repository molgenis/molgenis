package org.molgenis.omx.dataset;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.framework.tupletable.view.JQGridView;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridSearchOptions;
import org.molgenis.framework.tupletable.view.renderers.ExcelExporter;
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenView;
import org.molgenis.framework.ui.html.MolgenisForm;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.view.DataSetChooser;
import org.molgenis.omx.view.DataSetDownloader;
import org.molgenis.util.HandleRequestDelegationException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/** Simple plugin that only shows a data table for testing */
public class DataSetViewerPlugin extends EasyPluginController<DataSetViewerPlugin>
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DataSetViewerPlugin.class);
	private JQGridView tableView;
	private TupleTable tupleTable;
	private DataSetChooser dataSetChooser;
	private DataSetDownloader dataSetDownloader;

	public DataSetViewerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		setModel(this);
	}

	@Override
	public void reload(Database db)
	{
		if (tableView == null)
		{
			createViews(db, null, null);
		}
		else
		{
			// DataSet could be added or could be deleted
			try
			{
				List<DataSet> dataSets = db.find(DataSet.class);
				dataSetChooser.setDataSets(dataSets);

				// Check if selected dataset still exists
				if (dataSetChooser.getSelectedDataSetId() != null)
				{
					DataSet dataSet = db.findById(DataSet.class, dataSetChooser.getSelectedDataSetId());
					if (dataSet == null)
					{
						createViews(db, null, null);
					}
				}

			}
			catch (DatabaseException e)
			{
				logger.error("TableException creating DataSetViewer", e);
				throw new RuntimeException(e);
			}
		}
	}

	// handling of the ajax; should be auto-wired via the JQGridTableView
	// contructor (TODO)
	public void download_json_dataset(Database db, MolgenisRequest request, OutputStream out)
			throws HandleRequestDelegationException
	{
		// handle requests for the table named 'dataset'
		tableView.handleRequest(db, request, out);
	}

	public void download_xls(Database db, MolgenisRequest request) throws TableException, IOException
	{
		if (tupleTable instanceof DatabaseTupleTable) ((DatabaseTupleTable) tupleTable).setDb(db);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		HttpServletResponse response = request.getResponse();
		response.setContentType("application/ms-excel");
		response.addHeader("Content-Disposition",
				"attachment; filename=" + tableView.getName() + "_" + dateFormat.format(new Date()) + ".xls");
		new ExcelExporter(tupleTable).export(response.getOutputStream());
	}

	public void selectDataSet(Database db, MolgenisRequest request) throws HandleRequestDelegationException
	{
		Integer selectedDataSetId = request.getInt("dataSetId");
		HttpSession session = request.getRequest().getSession();

		if (selectedDataSetId != null)
		{
			@SuppressWarnings("unchecked")
			List<ObservableFeature> selectedObservableFeatures = (List<ObservableFeature>) session
					.getAttribute("selectedObservableFeatures");

			createViews(db, selectedDataSetId, selectedObservableFeatures);

			session.removeAttribute("selectedObservableFeatures");
		}

	}

	private void createViews(Database db, Integer selectedDataSetId, List<ObservableFeature> selectedObservableFeatures)
	{
		try
		{
			List<DataSet> dataSets = db.find(DataSet.class);

			if ((dataSets != null) && !dataSets.isEmpty())
			{
				DataSet dataSet = null;
				if (selectedDataSetId != null)
				{
					dataSet = db.findById(DataSet.class, selectedDataSetId);
				}

				if (dataSet == null)
				{
					dataSet = dataSets.get(0);
					selectedDataSetId = dataSet.getId();
				}

				DataSetTable table = new DataSetTable(dataSet, db);

				if (selectedObservableFeatures != null)
				{
					for (final Field field : table.getAllColumns())
					{
						ObservableFeature observableFeature = Iterables.find(selectedObservableFeatures,
								new Predicate<ObservableFeature>()
								{
									@Override
									public boolean apply(ObservableFeature of)
									{
										return of.getIdentifier().equals(field.getName());
									}

								}, null);

						if (observableFeature == null)
						{
							table.hideColumn(field.getName());
						}
						else
						{
							table.showColumn(field.getName());
						}
					}
				}

				// construct the gridview
				JQGridSearchOptions searchOptions = new JQGridSearchOptions();
				searchOptions.setMultipleGroup(false);
				searchOptions.setMultipleSearch(false);
				searchOptions.setShowQuery(false);

				tupleTable = table;
				tableView = new JQGridView(dataSet.getName(), this, table, searchOptions);

				dataSetChooser = new DataSetChooser(dataSets, selectedDataSetId);
				dataSetDownloader = new DataSetDownloader();
			}
		}
		catch (TableException e)
		{
			logger.error("TableException creating views", e);
			throw new RuntimeException(e);
		}
		catch (DatabaseException e)
		{
			logger.error("TableException creating views", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<link rel=\"stylesheet\" href=\"css/ui.jqgrid.css\" type=\"text/css\" />");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.jqGrid.min.js\"></script>");
		return s.toString();
	}

	// what is shown to the user
	@Override
	public ScreenView getView()
	{
		MolgenisForm view = new MolgenisForm(this);

		if (dataSetChooser != null)
		{
			view.add(dataSetChooser);
		}

		if (tableView != null)
		{
			view.add(tableView);
		}

		if (dataSetDownloader != null)
		{
			view.add(dataSetDownloader);
		}

		return view;
	}

}