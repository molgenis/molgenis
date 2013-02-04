package org.molgenis.framework.ui.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;

/**
 * A command defines a button on a screen, including the logic when the command
 * is executed. In architecture it behaves the same as any screen, i.e. it is
 * Templateable to allow for customized layouts. However, the default layout is
 * usually sufficient listing inputs (from getInputs) and actions (from
 * getActions).
 * <ul>
 * <li>handleRequest() defines how the action should be processed and how the
 * result should be shown</li>
 * <li>getInputs() lists the inputs to be shown</li>
 * <li>getActions() lists the pushbuttons to be shown</li>
 * </ul>
 */
public interface ScreenCommand extends Serializable, ScreenModel
{
	/**
	 * Retrieve the name of the icon to be shown. No icon will be shown if null.
	 * 
	 * @return path to the icon
	 */
	public String getIcon();

	/**
	 * Set the name of the icon to be shown. No icon will be if null.
	 * 
	 * @param icon
	 *            name
	 */
	public void setIcon(String icon);

	/**
	 * @return descriptive label of this command
	 */
	@Override
	public String getLabel();

	/**
	 * @param label
	 *            descriptive label of this command
	 */
	@Override
	public void setLabel(String label);

	/**
	 * Override the default javascript for this command by your own. This is
	 * usally not necessary, @see 'isDialog', but can be used for example if you
	 * want to do a linkout. Note: this is actually 'onClick'.
	 * 
	 * @param action
	 *            a string with javascript that should be executed onClick.
	 */
	public void setJavaScriptAction(String action);

	/**
	 * Returns the javascript that is run when this action is clicked.
	 * 
	 * @return the javascript that should be executed onClick.
	 */
	public String getJavaScriptAction();

	/**
	 * @param name
	 *            unique action name of this command (unique within the parent
	 *            screen). This is used by the controller to target this
	 *            command. If you would like to link just use
	 *            ../molgenis.do?__target
	 *            =getController().getName()&__action=this.getName()
	 */
	public void setName(String name);

	/**
	 * @return unique action name of this command (unique within one screen)
	 */
	@Override
	public String getName();

	/**
	 * @return Unique name of the target screen that should handle this command.
	 *         Default this equals getScreen(), i.e. the target is the same as
	 *         the screen this command is part of. Translates to
	 *         getController().getName().
	 */
	public String getTarget();

	/**
	 * @param target
	 *            Unique name of the target screen that should handle this
	 *            command
	 * 
	 *            DEPRECATED: you should use setControler() instead.
	 */
	@Deprecated
	public void setTargetController(String target);

	/**
	 * @return The screen this command is a part of
	 */
	@Override
	public ScreenController<?> getController();

	/**
	 * Helper method to reduce casting
	 */
	public FormModel<?> getFormScreen();

	/**
	 * @param screen
	 *            The screen this command belongs to
	 */
	@Override
	public void setController(ScreenController<? extends ScreenModel> screen);

	/**
	 * @return true if this command should be shown as a dialog. This will
	 *         result in a popup being shown that only shows the result of
	 *         render(). @see #setDialog
	 */
	public boolean isDialog();

	/**
	 * @param dialog
	 *            set to true if MOLGENIS should show this command via a dialog.
	 *            This results in javascript that pop-ups a dialog when pushed.
	 *            This will not work if you are using a custom database action.
	 */
	public void setDialog(boolean dialog);

	/**
	 * @return the unique name of the menu this command is part of. This will
	 *         only work if your screen has a menu (such as the generated
	 *         FormScreen). Null indicates it will not be shown on the menu of
	 *         the containing screen.
	 */
	public String getMenu();

	/**
	 * @param menu
	 *            unique name of the screenmenu this command is part of
	 *            (typically shown on top of each screen). Not to be confused
	 *            with the parent ScreenMenu.
	 */
	public void setMenu(String menu);

	/**
	 * When using the standard layout this will result in a dialog showing first
	 * inputs and then actions. If you override 'render()' this behavior depends
	 * on your own programming.
	 * 
	 * TODO move to DialogCommand subclass
	 * 
	 * @return a list of input boxes to show
	 * @throws DatabaseException
	 */
	public List<HtmlInput<?>> getInputs() throws DatabaseException;

	/**
	 * Set what action buttons to show at the bottom of your dialog.When using
	 * the standard layout this will result in a dialog showing first inputs and
	 * then actions. If you override 'render()' this behavior depends on your
	 * own programming.
	 * 
	 * TODO move to DialogCommand subclass.
	 * 
	 * @return list of HtmlInput that should be shown in the action area
	 */
	public List<ActionInput> getActions();

	/**
	 * Handle the request thrown by this command. The return type specifies how
	 * MOLGENIS should return this result, i.e., as download or as part of a
	 * popup or as part of the main screen.
	 * 
	 * @param db
	 *            provides access to the database
	 * @param request
	 *            provides access to the filled in inputs and actions
	 * @param downloadStream
	 *            this the request can use to write results as download (in
	 *            combination with Show.VIEW_DOWNLOAD
	 * @return a Show.XYZ that indicates if next action should be shown as
	 *         popup, back to main screen or as download. @see ScreenModel.Show
	 * @throws IOException
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception;

	/**
	 * @return boolean whether this action should be treated as a download
	 *         button.
	 * 
	 *         TODO do we still need this one???
	 */
	public boolean isDownload();

	/**
	 * @param download
	 *            true indicates that this action should be treated as a
	 *            download action. @see handleRequest() where the download
	 *            target is passed as stream and ScreenModel. Show can return
	 *            Show.VIEW_DOWNLOAD.
	 */
	public void setDownload(boolean download);

	/**
	 * @return whether this command should be shown on the command toolbar or
	 *         not. Standard MOLGENIS screens have a toolbar on top (just below
	 *         the menus). This behavior is not enabled if you customize
	 *         render().
	 */
	public boolean isToolbar();

	/**
	 * @param showOnToolbar
	 *            indicates whether this command should be shown on the command
	 *            menu.
	 */
	public void setToolbar(boolean showOnToolbar);

	/**
	 * @return the layout macro to be used if this command has a dialog.
	 *         Defaults to the default layout showing inputs and actions.
	 * 
	 *         DEPRECATED. You can use the FreemarkerView instead.
	 * 
	 *         TODO: create setView() analogous to ScreenController.
	 */
	@Deprecated
	public String getMacro();

	/**
	 * @return the layout macro to be used if this command has a dialog.
	 *         Defaults to the default layout template for commands,
	 *         ScreenCommand.ftl
	 * 
	 *         DEPRECATED. You can use the FreemarkerView instead.
	 * 
	 *         TODO: create setView() analogous to ScreenController.
	 */
	@Deprecated
	public String getTemplate();

	/**
	 * 
	 * @return false if this command should be hidden. For example because the
	 *         menu option is only available if particular rights are met.
	 */
	@Override
	public boolean isVisible();

	/**
	 * Render this command plugin.
	 */
	@Override
	public String render();

}
