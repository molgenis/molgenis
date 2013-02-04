package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.Entity;

public class DownloadAllXlsCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = -2682113764135477871L;
	private static final Logger logger = Logger.getLogger(DownloadAllCommand.class);

	public DownloadAllXlsCommand(String name, FormController<E> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Download all (.xls)");
		this.setIcon("img/download.png");
		this.setDownload(true);
		this.setMenu("File");
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream xlsDownload)
			throws Exception
	{
		logger.debug(this.getName());

		FormModel<? extends Entity> model = this.getFormScreen();
		FormController<?> controller = ((FormController<?>) this.getController());

		List<String> fieldsToExport = controller.getVisibleColumnNames();

		QueryRule[] rules = model.getRulesExclLimitOffset();
		ExcelWriter excelWriter = new ExcelWriter(xlsDownload);
		try
		{
			Class<? extends Entity> entityClass = model.getController().getEntityClass();
			db.find(entityClass, excelWriter.createTupleWriter(entityClass.getSimpleName()), fieldsToExport, rules);
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
