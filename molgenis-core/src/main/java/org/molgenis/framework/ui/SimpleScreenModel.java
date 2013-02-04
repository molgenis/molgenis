/**
 * File: invengine.screen.View <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li>2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li>2006-5-14; 1.1.0; MA Swertz; refactored to separate controller and view
 * </ul>
 */

package org.molgenis.framework.ui;

// jdk
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.commands.CommandMenu;
import org.molgenis.framework.ui.commands.ScreenCommand;
import org.molgenis.framework.ui.html.HtmlInputException;

/**
 * Base-class for a screen displaying information from the invengine system to
 * the user.
 */
public abstract class SimpleScreenModel implements ScreenModel, Serializable
{
	// member variables
	private static final long serialVersionUID = 3764151203967037515L;
	/** Logger */
	protected static final Logger logger = Logger.getLogger(SimpleScreenModel.class);
	/**
	 * constant parameter name for screen target controller (to be used when
	 * rendering actions)
	 */
	public static final String INPUT_TARGET = "__target";
	/**
	 * contant parameter name for screen target action (to be used when
	 * rendering actions)
	 */
	public static final String INPUT_ACTION = "__action";
	/** Label that is typically rendered on top of the screen view */
	private String label;
	/** The controller that handles requests on this screen */
	private ScreenController<?> controller;
	/**
	 * Menu is a two-dimensional map: first dimension is menu's, second is the
	 * menuitems. Submenu's are not yet supported. Option: make this a class
	 * structure with special "submenu" commands to allow submenu's.
	 */
	private Map<String, CommandMenu> menubar = new LinkedHashMap<String, CommandMenu>();
	/** messages to show to the user */
	private Vector<ScreenMessage> messages = new Vector<ScreenMessage>();
	/** Change visibility of this screen */
	private boolean visible = true;

	/**
	 * @param name
	 *            The name of this screen (needs to be unique in the
	 *            tree-container).
	 * @param parent
	 *            The parent of this screen.
	 */
	public SimpleScreenModel(ScreenController<?> controller)
	{
		// super(name, parent);
		this.controller = controller;
		reset();
	}

	/**
	 * Reset all model properties to default.
	 */
	@Override
	public void reset()
	{

	}

	@Override
	public String render() throws HtmlInputException
	{
		String result = this.getController().render();
		if (result == null) System.out.println("error with render of " + this);
		return this.getController().render();
	}

	/**
	 * Readonly name, inherited from controller
	 */
	@Override
	public String getName()
	{
		return this.getController().getName();
	}

	// public UserInterface<?> getRootScreen()
	// {
	// return (UserInterface<?>) this.getRoot();
	// }

	// @Override
	// public EmailService getEmailService()
	// {
	// return ((UserInterface<?>) this.getRoot()).getEmailService();
	// }
	//
	// public FileLink getTempFile() throws IOException
	// {
	// return getRootScreen().getTempFile();
	// }

	/**
	 * Set the pretty label to show on screen.
	 */
	@Override
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @return Pretty label to show on screen
	 */
	@Override
	public String getLabel()
	{
		return label;
	}

	/**
	 * The controller for this screen. The controller holds all the manipulation
	 * methods of the screen. (while the ScreenModel intself only contains
	 * state).
	 */
	@Override
	public ScreenController<? extends ScreenModel> getController()
	{
		return controller;
	}

	@Override
	public void setController(ScreenController<? extends ScreenModel> controller)
	{
		this.controller = controller;
	}

	// /**
	// * @return The sub-screen of this menu, which has been selected with the
	// * http-request.
	// */
	// public ScreenModel<?> getSelected()
	// {
	// if (getChild(selectedId) != null)
	// {
	// return getChild(selectedId);
	// }
	// if (getChildren().size() > 0)
	// {
	// if (getChildren().firstElement() instanceof ScreenModel<?>) return
	// getChildren().firstElement();
	// }
	// return null;
	// }

	// /**
	// * @param viewid
	// */
	// @Override
	// public void setSelected(String viewid)
	// {
	// // check if the path to this is also selected
	// if (this.getParent() != null)
	// {
	// logger.debug("call setselected on parent");
	// this.getParent().setSelected(this.getName());
	// }
	//
	// logger.debug("Screen " + this.getName() + " selected " + viewid);
	// this.selectedId = viewid;
	// }
	//
	// /**
	// * @param view
	// * A sub-screen of this menu.
	// * @return Whether the given sub-screen has been selected.
	// */
	// public boolean isSelected(ScreenModel<?> view)
	// {
	// ScreenModel<?> selected = getSelected();
	//
	// if (selected != null && view != null)
	// {
	// return selected.getName().equals(view.getName());
	// }
	// else
	// {
	// return false;
	// }
	// }

	/**
	 * COMMANDS for on the menu bar. Each command is also a controller and will
	 * be added to the appropriate controller target.*
	 */
	public void addCommand(ScreenCommand command)
	{
		// link the command to the view
		command.setController(this.getController());
		command.setTargetController(this.getController().getName());

		// commands must have a unique id
		if (getCommand(command.getName()) != null)
		{
			logger.warn("command with name '" + command.getName() + "' already exists; replaced");
		}

		// create new menu if not exists
		if (menubar.containsKey(command.getMenu()) == false)
		{
			menubar.put(command.getMenu(), new CommandMenu(command.getMenu(), this.getController(), command.getMenu(),
					"", ""));
		}

		// put the command in the menu
		menubar.get(command.getMenu()).addCommand(command);
		logger.debug("added action " + command.getName());
	}

	public ScreenCommand getCommand(String commandID)
	{
		for (CommandMenu menu : menubar.values())
		{
			if (menu.getCommand(commandID) != null) return menu.getCommand(commandID);
		}
		return null;
	}

	public Collection<CommandMenu> getMenus()
	{
		return menubar.values();
	}

	// @Override
	// public String getViewTemplate()
	// {
	// return null;
	// }

	/**
	 * Messages to inform the user of state changes and succes.
	 * 
	 * @param messages
	 */
	@Override
	public void setMessages(Vector<ScreenMessage> messages)
	{
		this.messages = messages;
	}

	@Override
	public void setMessages(ScreenMessage... messages)
	{
		this.messages = new Vector<ScreenMessage>(Arrays.asList(messages));
	}

	/**
	 * @return Messages to inform the user of state changes and succes.
	 */
	@Override
	public Vector<ScreenMessage> getMessages()
	{
		return this.messages;
	}

	@Override
	public ScreenModel getSelected()
	{
		return this.getController().getSelected();
	}

	//
	// @Override
	// public void setSelected(String viewid)
	// {
	// // check if the path to this is also selected
	// if (this.getController().getParent() != null)
	// {
	// logger.debug("call setselected on parent");
	// this.getController().getParent().setSelected(this.getName());
	// }
	//
	// logger.debug("Screen " + this.getName() + " selected " + viewid);
	// this.selectedId = viewid;
	// }

	public List<ScreenModel> getChildren()
	{
		List<ScreenModel> result = new ArrayList<ScreenModel>();
		for (ScreenController<? extends ScreenModel> childController : getController().getChildren())
		{
			result.add(childController.getModel());
		}

		return result;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/** Shorthand for setMessages(new ScreenMessage("success message",true)); */
	@Override
	public void setSuccess(String message)
	{
		this.setMessages(new ScreenMessage(message, true));
	}

	/** Shorthand for setMessages(new ScreenMessage("succes message",false)); */
	@Override
	public void setError(String message)
	{
		this.setMessages(new ScreenMessage(message, false));
	}

	public String getCustomHtmlHeaders()
	{
		return null;
	}

	public String getCustomHtmlBodyOnLoad()
	{
		return null;
	}
}
