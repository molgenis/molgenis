package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.CommandTemplate;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * The command to add a new record. If the add has an error, the user gets the
 * screen again.
 */
public class AddCommand2<E extends Entity> extends SimpleCommand
{
	private static final long serialVersionUID = -8509177499350778624L;
	Tuple previousRequest = new KeyValueTuple();
	Vector<ScreenMessage> messages = new Vector<ScreenMessage>();
	boolean success = false;

	public AddCommand2(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.setLabel("Add new record");
		this.setIcon("img/new.png");
		this.setDialog(true);
	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		CommandTemplate f = new CommandTemplate();
		f.addAll(this.getFormScreen().getNewRecordForm().getInputs());
		f.setAll(previousRequest);
		return f.getInputs();
	}

	@Override
	public List<ActionInput> getActions()
	{
		List<ActionInput> inputs = new ArrayList<ActionInput>();

		// HiddenInput inDialog = new HiddenInput("__indialog","add");
		// inputs.add(inDialog);

		ActionInput submit = new ActionInput("add", ActionInput.Type.NEXT);
		submit.setIcon("img/save.png");
		inputs.add(submit);

		ActionInput cancel = new ActionInput("", ActionInput.Type.CLOSE);
		cancel.setIcon("img/cancel.png");
		inputs.add(cancel);

		return inputs;
	}

	@Override
	public boolean isVisible()
	{
		// hide add button from the menu/toolbar if the screen is readonly
		return !this.getFormScreen().isReadonly();
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception
	{
		if (this.getName().equals(request.getAction()))
		{
			// delegate to the form controller
			// boolean success = ((FormController)
			// this.getScreen().getController()).doAdd(db, request);
			this.messages = this.getFormScreen().getMessages();
		}
		return ScreenModel.Show.SHOW_MAIN;
	}

	@Override
	public Vector<ScreenMessage> getMessages()
	{
		return messages;
	}

	@Override
	public void setMessages(Vector<ScreenMessage> messages)
	{
		this.messages = messages;
	}
}