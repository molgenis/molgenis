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
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.Entity;

/**
 * This command downloads the currently selected records as csv
 * 
 * @param <E>
 */
public class DownloadSelectedXlsCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = 3619865367653131342L;
	private static final Logger logger = Logger.getLogger(DownloadSelectedXlsCommand.class);

	public DownloadSelectedXlsCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Download selected (.xls)");
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
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream xlsDownload)
			throws Exception
	{
		logger.debug(this.getName());

		FormModel<?> model = this.getFormScreen();

		List<String> records = request.getList(FormModel.INPUT_SELECTED);
		if (records.isEmpty())
		{
			return ScreenModel.Show.SHOW_MAIN;
		}

		List<String> fieldsToExport = ((FormController<?>) this.getController()).getVisibleColumnNames();

		ExcelWriter excelWriter = new ExcelWriter(xlsDownload);
		try
		{
			Class<? extends Entity> entityClass = model.getController().getEntityClass();
			db.find(model.getController().getEntityClass(), excelWriter.createTupleWriter(entityClass.getSimpleName()),
					fieldsToExport, new QueryRule("id", Operator.IN, records));
		}
		finally
		{
			excelWriter.close();
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