/**
 * File: invengine.screen.MenuView <br>
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

//jdk
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class describes the functionality needed for a menu-screen. The
 * functionality of this class is used in the scripts generating the html-code
 * for this screen-variant.
 * 
 * @author MA Swertz
 * @version 2.0.0
 */
public class MenuModel extends SimpleScreenModel
{
	public enum Position
	{
		/* put on top of subform, float to right */
		TOP_RIGHT,
		/* put on top of subform, float to left */
		TOP_LEFT,
		/* left with contents next to it */
		LEFT,
		/* leave to layout script */
		DEFAULT
	};

	List<String> hiddenScreenNames = new ArrayList<String>();

	// member variables
	/* */
	private static final long serialVersionUID = -1211550529820121110L;

	private Position position = Position.DEFAULT;

	/**
	 * @param name
	 *            The name of this menu-screen (must be unique in the tree)
	 * @param parent
	 *            The parent of this screen
	 */
	public MenuModel(ScreenController<MenuModel> controller)
	{
		super(controller);
	}

	/**
	 * Hide an element on the menu.
	 * 
	 * @param name
	 *            of the subscreen to be hidden.
	 */
	public void hide(String name)
	{
		if (this.getController().get(name) != null) hiddenScreenNames.add(name);

	}

	/**
	 * Make an element on the menu that has been hidden visible again.
	 * 
	 * @param name
	 *            of the subscreen that needs to be unhidden.
	 */
	public void show(String name)
	{
		hiddenScreenNames.remove(name);
	}

	/**
	 * Only return childeren that are not hidden
	 */
	public List<ScreenModel> getVisibleChildren()
	{
		Vector<ScreenController<?>> subscreens = this.getController().getChildren();
		List<ScreenModel> result = new ArrayList<ScreenModel>();

		// remove hidden children from the list, and also commands
		for (ScreenController<?> s : subscreens)
		{
			if (this.hiddenScreenNames.indexOf(s.getName()) < 0 && s.getModel().isVisible())
			{
				result.add(s.getModel());
			}
		}

		return result;
	}

	// public Login getLogin()
	// {
	// return getController().getRootController().getLogin();
	// }

	@Override
	public ScreenModel getSelected()
	{

		List<ScreenModel> subscreens = getVisibleChildren();
		if (subscreens.contains(super.getSelected()))
		{
			return super.getSelected();
		}
		else if (subscreens.size() > 0)
		{
			return subscreens.get(0);
		}
		else
		{
			logger.error("Menu " + this.getName() + " doesn't have any subforms attached!");
			return null;
		}
	}

	@Override
	public boolean isVisible()
	{
		// the menu is visible if one its children is visible.
		if (this.getVisibleChildren().size() > 0) return true;
		return false;
	}

	public Position getPosition()
	{
		return position;
	}

	public void setPosition(Position position)
	{
		this.position = position;
	}

	@Override
	public void reset()
	{
	}

	@Override
	public String toString()
	{
		return "MenuModel(name=" + getName() + ")";
	}
}
