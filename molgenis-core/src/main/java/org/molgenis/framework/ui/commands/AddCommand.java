package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.ArrayList;
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
import org.molgenis.util.Entity;

/**
 * The command to add a new record
 */
public class AddCommand<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = 1512493344265778285L;

	public AddCommand(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.setLabel("Add new record");
		this.setIcon("img/new.png");
		this.setDialog(true);
		this.setMenu("Edit");
		this.setToolbar(true);
	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		// delegate to the formscreen
		return this.getFormScreen().getNewRecordForm().getInputs();
	}

	@Override
	public List<ActionInput> getActions()
	{
		List<ActionInput> inputs = new ArrayList<ActionInput>();

		// HiddenInput inDialog = new HiddenInput("__indialog","add");
		// inputs.add(inDialog);

		ActionInput submit = new ActionInput("Add", ActionInput.Type.SAVE);
		submit.setValue("edit_new");
		submit.setIcon("img/save.png");
		inputs.add(submit);

		ActionInput cancel = new ActionInput("cancel", ActionInput.Type.CLOSE);
		cancel.setIcon("img/cancel.png");
		inputs.add(cancel);

		return inputs;
	}

	@Override
	public boolean isVisible()
	{
		// hide add button if the screen is readonly
		return !this.getFormScreen().isReadonly();
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception
	{
		if (request.getString(FormModel.INPUT_SHOW) == null)
		{
			// delegate to the form controller
			((FormController<?>) this.getController()).doAdd(db, request);
		}
		return ScreenModel.Show.SHOW_MAIN;
	}
}