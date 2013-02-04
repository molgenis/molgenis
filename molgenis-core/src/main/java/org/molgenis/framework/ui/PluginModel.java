package org.molgenis.framework.ui;

import java.io.OutputStream;
import java.util.Vector;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.TokenFactory;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

public abstract class PluginModel<E extends Entity> extends SimpleScreenController<ScreenModel> implements ScreenModel
{
	private static final long serialVersionUID = -6748634936592503575L;
	private String label;
	private String selected;
	private Vector<ScreenMessage> messages = new Vector<ScreenMessage>();

	public PluginModel(String name, ScreenController<?> parent)
	{
		super(name, null, parent);
		// label is the last part of the name
		this.setModel(this);
		this.setLabel(this.getName().substring(this.getName().lastIndexOf("_") + 1));
	}

	@Override
	public ScreenView getView()
	{
		throw new UnsupportedOperationException();
	}

	public Login getLogin()
	{
		return this.getApplicationController().getLogin();
	}

	public TokenFactory getTokenFactory()
	{
		return this.getController().getApplicationController().getMolgenisContext().getTokenFactory();
	}

	public void handleRequest(Database db, MolgenisRequest request) throws HandleRequestDelegationException, Exception
	{

	}

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream out)
			throws HandleRequestDelegationException, Exception
	{
		this.handleRequest(db, request);
		return Show.SHOW_MAIN;
	}

	/**
	 * A plugin is actually a model-view-controller structure. The extension of
	 * plugin is the controller. The freemarker template is the view, see
	 * getFreemarker... methods A 'model' of the screen must be provided to be
	 * used by the.
	 */

	/**
	 * Show plugin or not, depending on whether the user is authenticated. Note:
	 * at the moment you can still override this method in your plugin to bypass
	 * security (evil).
	 */
	@Override
	public boolean isVisible()
	{
		try
		{
			return this.getLogin().canRead(this.getController());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ScreenController<? extends ScreenModel> getController()
	{
		return this;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public void setMessages(Vector<ScreenMessage> messages)
	{
		this.messages = messages;
	}

	@Override
	public void setMessages(ScreenMessage... messages)
	{
		Vector<ScreenMessage> messageVector = new Vector<ScreenMessage>();
		for (ScreenMessage m : messages)
			messageVector.add(m);
		this.messages = messageVector;
	}

	@Override
	public Vector<ScreenMessage> getMessages()
	{
		return messages;
	}

	@Override
	public void setController(ScreenController<? extends ScreenModel> controller)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelected(String viewid)
	{
		this.selected = viewid;
	}

	@Override
	public ScreenModel getSelected()
	{
		if (this.getChildren().size() > 0)
		{
			if (get(this.selected) == null)
			{
				return this.getChildren().firstElement().getModel();
			}
			else
			{
				return get(this.selected).getModel();
			}
		}
		return null;
	}

	/**
	 * Path to the Template from within the source tree. It is good practice to
	 * give the template the same name. E.g. if the PluginScreen is
	 * myplugins.MyPlugin then the template could be myplugins/MyPlugin.ftl.
	 */
	@Override
	public abstract String getViewTemplate();

	/**
	 * Name of the main Freemarker macro inside the template
	 * 
	 * @see #getViewTemplate(). It is good practice to give this macro the same
	 *      name as the template file. This macro should have as first parameter
	 *      the screen.
	 * 
	 *      For example: <#macro MyPlugin screen> Hello World </#macro>
	 */
	public abstract String getViewName();

	@Override
	public abstract void reload(Database db);

	@Override
	public void reset()
	{

	}

	@Deprecated
	// will be removed
	public ScreenController<?> getScreen()
	{
		return getController();
	}

	@Deprecated
	// will be removed
	public ApplicationController getRootScreen()
	{
		return this.getApplicationController();
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
}
