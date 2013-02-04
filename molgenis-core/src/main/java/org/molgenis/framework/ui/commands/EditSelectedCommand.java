package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.FormModel.Mode;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HiddenInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.Paragraph;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * This command shows a dialog to edit in batch It therefor uses a custom
 * template
 */
public class EditSelectedCommand extends SimpleCommand
{
	private static final long serialVersionUID = -2996595009523144519L;
	private static final Logger logger = Logger.getLogger(EditSelectedCommand.class);
	private List<?> selectedIds = new ArrayList<Object>();

	public EditSelectedCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Update selected");
		this.setIcon("img/update.gif");
		this.setDialog(true);
		this.setMenu("Edit");

	}

	@Override
	public boolean isVisible()
	{
		FormModel<? extends Entity> view = this.getFormScreen();
		return !view.isReadonly() && view.getMode().equals(Mode.LIST_VIEW);
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream out) throws Exception
	{
		logger.debug(this.getName());

		// check whether in the popup
		if (request.getString(FormModel.INPUT_SHOW) == null)
		{
			FormModel<? extends Entity> view = this.getFormScreen();
			List<?> idList = request.getList(FormModel.INPUT_SELECTED);
			for (Object id : idList)
			{
				logger.info("mass updating id: " + id);
			}

			ScreenMessage msg = null;

			// cleanup the request, only use ticked (marked with 'use_'
			WritableTuple tuple = new KeyValueTuple();
			for (HtmlInput<?> input : this.getFormScreen().getNewRecordForm().getInputs())
			{
				if (!request.isNull("use_" + input.getName()))
				{
					tuple.set(input.getName(), request.get(input.getName()));
				}
			}

			int row = 0;
			try
			{
				Query<? extends Entity> q = db.query(view.getController().getEntityClass()).in(
						view.create().getIdField(), idList);
				List<? extends Entity> entities = q.find();

				db.beginTx();
				for (Entity e : entities)
				{
					row++;
					// set only not null values
					e.set(tuple, false);
					db.update(e);
				}
				db.commitTx();
				msg = new ScreenMessage("MASS UPDATE SUCCESS: updated " + entities.size() + " rows", null, true);
			}

			catch (Exception e)
			{
				try
				{
					db.rollbackTx();
				}
				catch (DatabaseException e1)
				{
					logger.error("doMassUpdate() Should never happen: " + e1);
					e1.printStackTrace();
				}
				msg = new ScreenMessage("MASS UPDATE FAILED on item '" + row + "': " + e, null, false);
			}

			view.getMessages().add(msg);
		}
		// record the selected ids
		else
		{
			this.selectedIds = request.getList(FormModel.INPUT_SELECTED);
		}

		return ScreenModel.Show.SHOW_MAIN;

	}

	@Override
	public List<ActionInput> getActions()
	{
		List<ActionInput> inputs = new ArrayList<ActionInput>();

		ActionInput submit = new ActionInput("Update", ActionInput.Type.SAVE);
		submit.setValue(this.getName());
		submit.setIcon("img/save.png");
		submit.setDescription("Save the changes.");
		inputs.add(submit);

		ActionInput cancel = new ActionInput("Cancel", ActionInput.Type.CLOSE);
		cancel.setIcon("img/cancel.png");
		submit.setDescription("Cancel the changes.");
		inputs.add(cancel);

		return inputs;
	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		List<HtmlInput<?>> inputs = new ArrayList<HtmlInput<?>>();

		if (this.selectedIds == null || this.selectedIds.size() == 0)
		{
			Paragraph t = new Paragraph("No records were selected for updating.");
			t.setDescription("Error.");
			inputs.add(t);
			return inputs;
		}

		Paragraph t = new Paragraph("Selected ids:" + this.selectedIds.toString());
		t.setDescription("The IDs you have selected for updating.");
		inputs.add(t);

		// put ids of selected rows in hidden field
		for (Object id : this.selectedIds)
		{
			HiddenInput h = new HiddenInput(FormModel.INPUT_SELECTED, id);
			h.setDescription("Hidden input");
			inputs.add(h);
		}

		// get inputs from formscreen
		for (HtmlInput<?> input : this.getFormScreen().getNewRecordForm().getInputs())
		{
			if (!input.isHidden() && !input.isReadonly())
			{
				EditSelectedInput e = new EditSelectedInput(input);
				e.setDescription("EditSelectedInput");
				inputs.add(e);
			}

		}

		return inputs;
	}
}
