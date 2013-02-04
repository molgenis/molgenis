/**
 * 
 */
package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.FormModel.Mode;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;

public class ViewEditViewCommand extends SimpleCommand
{
	private static final long serialVersionUID = -4074166087593025109L;

	public ViewEditViewCommand(String name, ScreenController<?> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("View/Edit Record");
		this.setIcon("img/editview.gif");
		this.setMenu("View");
		this.setToolbar(true);
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream out)
	{
		getFormScreen().setMode(Mode.EDIT_VIEW);
		Integer offset = request.getInt(FormModel.INPUT_OFFSET);
		if (offset != null) getFormScreen().getPager().setOffset(offset - 1);
		return ScreenModel.Show.SHOW_MAIN;
	}

	@Override
	public boolean isVisible()
	{
		// show add button when not in recordview
		// show add button if the screen is not readonly
		return !this.getFormScreen().getMode().equals(Mode.EDIT_VIEW) && !this.getFormScreen().isReadonly()
				&& this.getFormScreen().getRecordInputs().size() != 0
				&& !this.getFormScreen().getRecords().get(0).isReadonly();
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