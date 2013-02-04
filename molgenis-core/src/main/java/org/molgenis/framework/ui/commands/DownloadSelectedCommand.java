/**
 * 
 */
package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.FormModel.Mode;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.Entity;

/**
 * This command downloads the currently selected records as csv
 * 
 * @param <E>
 */
public class DownloadSelectedCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = 3619865367653131342L;
	private static final Logger logger = Logger.getLogger(DownloadSelectedCommand.class);

	public DownloadSelectedCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Download selected (.txt)");
		this.setIcon("img/download.png");
		this.setDownload(true);
		this.setMenu("File");
	}

	@Override
	public boolean isVisible()
	{
		// only show in listview
		return this.getFormScreen().getMode().equals(Mode.LIST_VIEW);
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream csvDownload)
			throws Exception
	{
		logger.debug(this.getName());

		FormModel<?> view = this.getFormScreen();

		List<String> records = request.getList(FormModel.INPUT_SELECTED);

		if (records.isEmpty())
		{
			return ScreenModel.Show.SHOW_MAIN;
		}

		List<String> fieldsToExport = ((FormController<?>) this.getController()).getVisibleColumnNames();

		// watch out, the "IN" operator expects an Object[]
		CsvWriter csvWriter = new CsvWriter(csvDownload);
		try
		{
			db.find(view.getController().getEntityClass(), csvWriter, fieldsToExport, new QueryRule("id", Operator.IN,
					records));
		}
		finally
		{
			csvWriter.close();
		}
		return ScreenModel.Show.SHOW_MAIN;
	}

	@Override
	public List<ActionInput> getActions()
	{
		return null;
	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		return null;
	}
}