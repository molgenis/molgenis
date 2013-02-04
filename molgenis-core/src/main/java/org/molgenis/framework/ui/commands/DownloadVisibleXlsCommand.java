/**
 * 
 */
package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.EntityTuple;

/**
 * This command downloads the records currently shown as csv.
 * 
 * @author Morris Swertz
 * 
 * @param <E>
 */
public class DownloadVisibleXlsCommand extends SimpleCommand
{
	private static final long serialVersionUID = -6279819301321361448L;

	public DownloadVisibleXlsCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setDownload(true);
		this.setLabel("Download visible (.xls)");
		this.setIcon("img/download.png");
		this.setMenu("File");
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream xlsDownload)
			throws ParseException, DatabaseException, Exception
	{
		FormModel<?> view = this.getFormScreen();
		List<String> fieldsToExport = ((FormController<?>) this.getController()).getVisibleColumnNames();
		ExcelWriter excelWriter = new ExcelWriter(xlsDownload);
		TupleWriter sheetWriter = excelWriter.createTupleWriter("Entity");

		try
		{
			sheetWriter.writeColNames(fieldsToExport);

			for (Entity e : view.getRecords())
				sheetWriter.write(new EntityTuple(e));
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