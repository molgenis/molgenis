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
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.Entity;

/**
 * This command returns all records currently selected as CSV download.
 * 
 * @param <E>
 */
public class DownloadAllCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = -2682113764135477871L;
	private static final Logger logger = Logger.getLogger(DownloadAllCommand.class);

	public DownloadAllCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Download all (.txt)");
		this.setIcon("img/download.png");
		this.setDownload(true);
		this.setMenu("File");
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream csvDownload)
			throws Exception
	{
		logger.debug(this.getName());

		FormModel<? extends Entity> model = this.getFormScreen();
		FormController<?> controller = ((FormController<?>) this.getController());

		List<String> fieldsToExport = controller.getVisibleColumnNames();

		// TODO remove entity name, capitals to small , and remove _name fields
		QueryRule[] rules = model.getRulesExclLimitOffset();
		CsvWriter csvWriter = new CsvWriter(csvDownload);
		try
		{
			db.find(model.getController().getEntityClass(), csvWriter, fieldsToExport, rules);
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