/**
 * 
 */
package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
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
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.util.Entity;

public class RemoveSelectedCommand extends SimpleCommand
{
	private static final long serialVersionUID = 4730493886936446817L;
	private static final Logger logger = Logger.getLogger(RemoveSelectedCommand.class);

	public RemoveSelectedCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Remove selected");
		this.setIcon("img/delete.png");
		this.setMenu("Edit");
	}

	@Override
	public boolean isVisible()
	{
		// only in listview
		return getFormScreen().getMode().equals(Mode.LIST_VIEW) && !this.getFormScreen().isReadonly();
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception
	{
		logger.debug(this.getName());

		FormModel<? extends Entity> view = getFormScreen();

		ScreenMessage msg = null;
		try
		{
			// get ids
			List<?> idList = request.getList(FormModel.INPUT_SELECTED);
			if (idList == null || idList.size() == 0) throw new Exception("no items selected");
			for (Object id : idList)
			{
				logger.info("mass removing id: " + id);
			}

			// find selected entities
			Query<? extends Entity> q = db.query(view.getController().getEntityClass()).in(view.create().getIdField(),
					idList);
			List<? extends Entity> selection = q.find();

			// delete selected entities
			db.remove(selection);
			msg = new ScreenMessage("REMOVED " + selection.size() + " records", null, true);
		}
		catch (Exception e)
		{
			msg = new ScreenMessage("REMOVE SELECTION FAILED: " + e.getMessage(), null, false);
		}
		view.getMessages().add(msg);

		// **make sure the user sees a record**/
		if (msg.isSuccess())
		{
			view.getPager().prev(db);
			// resetChildren();
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