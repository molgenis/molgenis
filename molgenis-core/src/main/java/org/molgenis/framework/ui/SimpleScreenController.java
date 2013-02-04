/**
 * File: invengine.screen.Controller <br>
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

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.util.EmailService;
import org.molgenis.util.FileLink;
import org.molgenis.util.SimpleTree;

/**
 * Base-class for a screen displaying information from the invengine system to
 * the user.
 */
public abstract class SimpleScreenController<MODEL extends ScreenModel> extends SimpleTree<ScreenController<?>>
		implements ScreenController<MODEL>, Serializable
{
	// member variables
	/** */
	private MODEL model;

	/** */
	protected static final Logger logger = Logger.getLogger(SimpleScreenController.class);
	/** */
	static final long serialVersionUID = 5286068849305140609L;
	/** Determines which of the subscreens should be shown */
	protected String selectedId;

	// /** The name of the view to be used. */
	// private String viewName;

	// constructor
	public SimpleScreenController(String name, MODEL model, ScreenController<?> parent)
	{
		super(name, parent);
		this.model = model;
		// logger = Logger.getLogger(this.getClass().getSimpleName() + ":" +
		// view.getName());
	}

	// overloadable methods
	/**
	 * This method (re)loads the view, making persistant data actual again. This
	 * method needs to be called when the screen operates on, for instance, a
	 * recordset.
	 * 
	 * @throws Exception
	 */
	@Override
	public abstract void reload(Database db) throws Exception;

	/**
	 * This is the actual control-method, which changes the view. The view
	 * itself may delegate requests to other objects (e.g. delegate insert to
	 * the persistance layer). TODO: A superclass method can delegate all
	 * requests using reflection.
	 * 
	 * @param request
	 *            The http-request in a convenient map.
	 * @throws HandleRequestDelegationException
	 * @throws Exception
	 */
	// public abstract void handleRequest(Database db, MolgenisRequest request) throws
	// Exception, HandleRequestDelegationException;

	// public String getFromRequest(Tuple request, String name)
	// {
	// return request.getString(name);
	// }

	/**
	 * This method calls the reset-function this instance and all the children
	 * of this instance. After this call the screens attached to this screen
	 * should be in a pre-defined valid state.
	 */
	public void doResetChildren()
	{
		for (ScreenController<?> subform : this.getChildren())
		{
			subform.getModel().reset();
		}
	}

	// public Templateable getScreen()
	// {
	// return this.model;
	// }

	@Override
	public EmailService getEmailService()
	{
		return ((ApplicationController) this.getRoot()).getEmailService();
	}

	@Override
	public FileLink getTempFile() throws IOException
	{
		return getApplicationController().getTempFile();
	}

	/**
	 * @param model
	 *            A sub-screen of this menu.
	 * @return Whether the given sub-screen has been selected.
	 */
	public boolean isSelected(ScreenModel model)
	{
		ScreenModel selected = getModel().getSelected();

		if (selected != null && model != null)
		{
			return selected.getController().getName().equals(this.getName());
		}
		else
		{
			return false;
		}
	}

	@Override
	public ApplicationController getApplicationController()
	{
		return (ApplicationController) this.getRoot();
	}

	@Override
	public MODEL getModel()
	{
		return model;
	}

	public void setModel(MODEL model)
	{
		this.model = model;
	}

	@Override
	public String getViewTemplate()
	{
		return null;
		// throw new UnsupportedOperationException(
		// "viewTemplate not set for this screen "
		// + this.getClass().getSimpleName() + "(name="
		// + this.getName() + ")");
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder resultBuilder = new StringBuilder();
		for (ScreenController<?> c : this.getChildren())
		{
			resultBuilder.append("<!--custom html headers: ").append(c.getName()).append("-->");
			resultBuilder.append(c.getCustomHtmlHeaders());
		}
		return resultBuilder.toString();
	}

	@Override
	public String getLabel()
	{
		return this.getModel().getLabel();
	}

	@Override
	public String getCustomHtmlBodyOnLoad()
	{
		StringBuilder resultBuilder = new StringBuilder();
		for (ScreenController<?> c : this.getChildren())
		{
			resultBuilder.append(c.getCustomHtmlBodyOnLoad());
		}
		return resultBuilder.toString();
	}

	/**
	 * @param viewid
	 */
	@Override
	public void setSelected(String viewid)
	{
		// check if the path to this is also selected
		if (this.getParent() != null)
		{
			logger.debug("call setselected on parent");
			this.getParent().setSelected(this.getName());
		}

		logger.debug("Screen " + this.getName() + " selected " + viewid);
		this.selectedId = viewid;
	}

	@Override
	public ScreenModel getSelected()
	{
		if (getChild(selectedId) != null)
		{
			return getChild(selectedId).getModel();
		}
		return null;
	}

	@Override
	public String render() throws HtmlInputException
	{
		String result = this.getView().render();
		if (result == null || result.isEmpty())
		{
			throw new HtmlInputException("render showed nothing for " + this);
		}
		return result;
	}

	@Override
	public Database getDatabase()
	{
		return this.getApplicationController().getDatabase();
	}

}
