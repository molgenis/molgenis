package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.TextInput;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.Entity;

/**
 * The command to add in batch/upload csv
 */
public class AddBatchCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = -4067952586340535730L;
	private static final Logger logger = Logger.getLogger(AddBatchCommand.class);

	public AddBatchCommand(String name, ScreenController<?> owner)
	{
		super(name, owner);
		this.setLabel("Add in batch/upload CSV");
		this.setIcon("img/upload.png");
		this.setDialog(true);
		this.setMenu("File");
	}

	@Override
	public List<ActionInput> getActions()
	{
		List<ActionInput> inputs = new ArrayList<ActionInput>();

		ActionInput submit = new ActionInput("Add", ActionInput.Type.SAVE);
		submit.setValue("upload_csv");
		submit.setIcon("img/save.png");
		submit.setDescription("Store the data");
		inputs.add(submit);

		ActionInput cancel = new ActionInput("Cancel", ActionInput.Type.CLOSE);
		cancel.setIcon("img/cancel.png");
		cancel.setDescription("Cancel adding data");
		inputs.add(cancel);

		return inputs;
	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		// delegate to the formscreen
		List<HtmlInput<?>> inputs = this.getFormScreen().getNewRecordForm().getInputs();

		// remove not-null constraints
		for (HtmlInput<?> i : inputs)
			i.setNillable(true);

		// add the textarea for csv
		TextInput csvInput = new TextInput("__csvdata", "put here your data in comma-separated format.");
		csvInput.setLabel("CSV data");
		csvInput.setTooltip("put here your data in comma-separated format.");
		csvInput.setDescription("Put your CSV data here.");
		inputs.add(csvInput);

		return inputs;
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception
	{
		logger.debug(this.getName());

		// check if in dialog
		if (request.getString(FormModel.INPUT_SHOW) == null)
		{
			ScreenMessage msg = null;
			try
			{
				CsvReader csvReader = new CsvReader(new StringReader(request.getString("__csvdata")));
				String entityName = this.getFormScreen().getEntityClass().getSimpleName();

				EntitiesImporter entitiesImporter = this.getFormScreen().getCsvEntityImporter();
				entitiesImporter.setDatabase(db);

				EntityImportReport importReport = null;
				try
				{
					importReport = entitiesImporter.importEntities(csvReader, entityName, DatabaseAction.ADD);
				}
				finally
				{
					csvReader.close();
				}

				msg = new ScreenMessage("CSV UPLOAD SUCCESS: added " + importReport.getNrImported() + " rows", null,
						true);
				logger.debug("CSV UPLOAD SUCCESS: added " + importReport.getNrImported() + " rows");
				getFormScreen().getPager().resetFilters();
				getFormScreen().getPager().last(db);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				msg = new ScreenMessage("CSV UPLOAD FAILED: " + e.getMessage(), null, false);
				logger.error("CSV UPLOAD FAILED: " + e.getMessage());
			}
			getFormScreen().getMessages().add(msg);
		}

		// show result in the main screen
		return ScreenModel.Show.SHOW_MAIN;
	}

	@Override
	public boolean isVisible()
	{
		// hide add button if the screen is readonly
		return !this.getFormScreen().isReadonly();
	}

}