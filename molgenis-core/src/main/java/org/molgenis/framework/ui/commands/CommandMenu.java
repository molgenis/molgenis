package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;

public class CommandMenu extends SimpleCommand
{
	private static final long serialVersionUID = 7869046696648113688L;

	private static final Logger logger = Logger.getLogger(CommandMenu.class);

	/** menu items with order as entered */
	private Map<String, ScreenCommand> menu_items = new LinkedHashMap<String, ScreenCommand>();

	public CommandMenu(String id, ScreenController<?> screen, String label, String icon, String action)
	{
		super(id, screen);
		this.setLabel(label);
		this.setIcon(icon);
		this.setJavaScriptAction(action);
	}

	/**
	 * Add a menu item.
	 * 
	 * @param command
	 */
	@Override
	public void addCommand(ScreenCommand command)
	{
		if (menu_items.containsKey(command.getName()))
		{
			logger.warn("addCommand: command with id '" + command.getName() + "' already exists; replaced");
		}
		menu_items.put(command.getName(), command);
	}

	/**
	 * Return the values as list.
	 * 
	 */
	public Collection<ScreenCommand> getCommands()
	{
		// Logger.getLogger("test").debug("returning commands "+menu_items.values().size());
		return menu_items.values();
	}

	/**
	 * Find a specific command.
	 * 
	 * @param name
	 */
	@Override
	public ScreenCommand getCommand(String name)
	{
		return menu_items.get(name);
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

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream) throws Exception
	{
		return ScreenModel.Show.SHOW_DIALOG;
	}

}
