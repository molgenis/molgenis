package org.molgenis.framework.ui;

import java.util.Vector;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.html.HtmlInputException;

/**
 * Easyplugin controller delegates all common stuff to the controller (label,
 * name, visibility, error / succes messages, etc; you can then add your
 * application specific state if needed
 */
public class EasyPluginModel implements ScreenModel
{
	private static final long serialVersionUID = 4866399456367824712L;

	private EasyPluginController<?> controller;

	public EasyPluginModel(EasyPluginController<?> controller)
	{
		this.controller = controller;
	}

	@Override
	public boolean isVisible()
	{
		try
		{
			return this.getController().getApplicationController().getLogin().canRead(this.getController());
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void reset()
	{
		controller.reset();
	}

	@Override
	public void setLabel(String label)
	{
		controller.setLabel(label);

	}

	@Override
	public String getLabel()
	{
		return controller.getLabel();
	}

	@Override
	public ScreenController<?> getController()
	{
		return controller;
	}

	@Override
	public void setController(ScreenController<? extends ScreenModel> controller)
	{
		this.controller = (EasyPluginController<?>) controller;
	}

	@Override
	public Vector<ScreenMessage> getMessages()
	{
		return this.controller.getMessages();
	}

	@Override
	public ScreenModel getSelected()
	{
		return this.controller.getSelected();
	}

	@Override
	public void setMessages(Vector<ScreenMessage> messages)
	{
		controller.setMessages(messages);

	}

	@Override
	public void setMessages(ScreenMessage... messages)
	{
		controller.setMessages(messages);
	}

	@Override
	public void setSuccess(String message)
	{
		controller.setSuccess(message);
	}

	@Override
	public void setError(String message)
	{
		controller.setError(message);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "(name=" + getName() + ")";
	}

	@Override
	public String getName()
	{
		return getController().getName();
	}

	@Override
	public String render() throws HtmlInputException
	{
		return this.getController().render();
	}
}
