/**
 * File: invengine.screen.View <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li> 2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li> 2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li> 2006-5-14; 1.1.0; MA Swertz; refactored to separate controller and view
 * </ul>
 */

package org.molgenis.framework.ui;

// jdk
import java.io.Serializable;
import java.util.Vector;

import org.molgenis.framework.ui.html.HtmlInputException;

// jdk

/**
 * A screen contains the <i>state</i> of a part of the user-interface.
 * <p>
 * The idea is that the user-interface is partitioned in a
 * {@link org.molgenis.util.Tree} of visible areas. A screen is a bean that only
 * holds the state of such user-interface area. State changes because of
 * internal or external events (user requests and reloads respectively). The
 * handeling of such events should be delegated to the controller, see
 * {@link #getController()}.
 * <p>
 * This ensures that both screen and controller are easy to understand.
 * Furthermore, this also ensures that no "logic" becomes part of the layouting
 * of such screens (i.e., ends up in templates). This helps clear separation of
 * state, logic and presentation.
 */
public interface ScreenModel extends Serializable
{
	/**
	 * Bind parameter name for screen target. This parameter can be used by
	 * layout renderer as well as for building events targetting this Screen.
	 */
	public static final String INPUT_TARGET = "__target";

	/** Bind parameter name for screen action (to be used by layout renderer) */
	public static final String INPUT_ACTION = "__action";

	/**
	 * Parameter to indicate how the results should be shown (as download, as
	 * popup, inline with the rest of the GUI (Default)
	 */
	public enum Show
	{
		/**
		 * Show the current screen as JSON
		 */
		SHOW_JSON("json"),

		/**
		 * Show the jqGrid
		 */
		SHOW_JQGRID("jqGrid"),
		/**
		 * Show the current screen as part of its parent. This means the layout
		 * template is called on the root of the user interface.
		 */
		SHOW_MAIN("inline"),
		/**
		 * Show the current screen or command as popup. This means the layout
		 * template is applied on this element only.
		 */
		SHOW_DIALOG("popup"),
		/**
		 * Don't layout the screen. Instead pass use the outputstream in
		 * handleRequest so it can be downloaded. This result in a download
		 * file.
		 */
		SHOW_DOWNLOAD("download"),
		/**
		 * Don't show anything
		 */
		SHOW_CLOSE("close");

		private String TAG;

		Show(String name)
		{
			this.TAG = name;
		}

		@Override
		public String toString()
		{
			return this.TAG;
		}

		public boolean equals(String str)
		{
			if (str != null && str.equals(this.TAG)) return true;
			return false;
		}
	}

	/**
	 * Reset the view to construction defaults, resetting all user set changes.
	 */
	public void reset();

	public String getName();

	/**
	 * Set the label of this screen. The label is a pretty name that makes sense
	 * to a user.
	 * 
	 * @param label
	 *            a new pretty label
	 */
	public void setLabel(String label);

	/**
	 * Retrieve the label of this screen. The label is a pretty name that makes
	 * sense to a user.
	 * 
	 * @return the label
	 */
	public String getLabel();

	/**
	 * Retrieve the database that is used by this screen.
	 * <p>
	 * QUESTION: this seems to specific!
	 * 
	 * @return database used by this screen
	 */
	// public Database getDatabase();
	/**
	 * Retrieve the controller for this screen.
	 * 
	 * The controller holds all the manipulation methods of the screen. (while
	 * the screen intself only contains state).
	 * 
	 * @return the ScreenController
	 */
	public ScreenController<?> getController();

	/**
	 * Set the controller for this screen.
	 * 
	 * @param controller
	 *            to handle events on this screen
	 */
	public void setController(ScreenController<? extends ScreenModel> controller);

	/**
	 * Method to indicate whether the screen should be shown. E.g. because the
	 * user doesn't have rights to see the underlying entity.
	 * 
	 * @return true if the screen should be shown
	 */
	public boolean isVisible();

	/**
	 * Get the screen messages
	 * 
	 * @return
	 */
	public Vector<ScreenMessage> getMessages();

	/**
	 * Get selected other models to be shown. This is a shorthand for
	 * getController().getSelected().getModel();
	 * 
	 * @return
	 */
	public ScreenModel getSelected();

	/**
	 * Set the screen messages
	 * 
	 * @param messages
	 */
	public void setMessages(Vector<ScreenMessage> messages);

	public void setMessages(ScreenMessage... messages);

	/** Shorthand for setMessages(new ScreenMessage("success message",true)); */
	public void setSuccess(String message);

	/** Shorthand for setMessages(new ScreenMessage("succes message",false)); */
	public void setError(String message);

	public String render() throws HtmlInputException;
}
