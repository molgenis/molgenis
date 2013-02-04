/**
 * File: invengine.screen.MenuController <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li> 2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li> 2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li> 2006-04-15; 1.0.0; MA Swertz; Documentation.
 * <li>2006-5-14; 1.1.0; MA Swertz; refactored to separate controller and view
 * </ul>
 */

package org.molgenis.framework.ui;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ScreenModel.Show;
import org.molgenis.util.tuple.Tuple;

public class MenuController extends SimpleScreenController<MenuModel>
{
	private static final Logger logger = Logger.getLogger(MenuController.class);
	private static final long serialVersionUID = -7579424157884595183L;

	public MenuController(String name, ScreenController<?> parent)
	{
		super(name, null, parent);
		this.setModel(new MenuModel(this));
		this.getModel().setLabel(name);
	}

	@Override
	public ScreenView getView()
	{
		return new FreemarkerView("MenuView.ftl", this.getModel());
	}

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream out)
	{
		this.doSelect(request);

		return Show.SHOW_MAIN;
	}

	@Override
	public void reload(Database db)
	{
		logger.debug("reloading Menu(" + getModel().getName() + ")");
		ScreenModel selected = getModel().getSelected();
		if (selected == null)
		{
			logger.error(getModel().getName() + " has no children");
			return;
		}
		try
		{
			selected.getController().reload(db);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// access methods
	/**
	 * Request to choose which subscreen is whosed.
	 * 
	 * @param request
	 *            containing all the data from the http-request.
	 */
	public boolean doSelect(Tuple request)
	{
		if (request.getString("select") != null)
		{
			setSelected(request.getString("select"));
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public MenuModel getModel()
	{
		return super.getModel();
	}

	@Override
	public ScreenModel getSelected()
	{
		if (getChild(selectedId) != null)
		{
			return getChild(selectedId).getModel();
		}
		if (getChildren().size() > 0)
		{
			if (getChildren().firstElement() instanceof ScreenModel) return getChildren().firstElement().getModel();
		}
		return null;
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder strBuilder = new StringBuilder("<!--custom html headers: ").append(this.getName()).append("-->");

		if (this.getModel() != null && this.getModel().getSelected() != null)
		{
			strBuilder.append(this.getModel().getSelected().getController().getCustomHtmlHeaders());
		}
		else
		{
			for (ScreenController<?> c : this.getChildren())
			{
				strBuilder.append(c.getCustomHtmlHeaders());
			}
		}

		return strBuilder.toString();
	}
}
