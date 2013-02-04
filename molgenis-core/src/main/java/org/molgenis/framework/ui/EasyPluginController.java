package org.molgenis.framework.ui;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Vector;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

/**
 * Simplified controller that handles a lot of the hard stuff in handleRequest.
 */
public abstract class EasyPluginController<M extends ScreenModel> extends SimpleScreenController<M> implements
		ScreenModel, ScreenView
{
	private static final long serialVersionUID = 1L;

	// hack to be able to 'correctly' handle redirects (do not continue handling
	// this request after HandleRequest in AbstMolgServlet is done - contrary to
	// usual response serving which is 'fall through' and therefore wrong) and
	// at the same time allow EasyPlugins to throw exceptions which are all
	// thrown as InvocationTargetException due to reflection, while being able
	// to render the resulting page + the exception on screen
	public static Boolean HTML_WAS_ALREADY_SERVED;

	private Vector<ScreenMessage> messages = new Vector<ScreenMessage>();

	private String label = null;

	@SuppressWarnings("unchecked")
	public EasyPluginController(String name, ScreenController<?> parent)
	{
		super(name, null, parent);
		this.setModel((M) this);
	}

	/**
	 * If a user sends a request it can be handled here. Default, it will be
	 * automatically mapped to methods based request.getAction();
	 * 
	 * @throws HandleRequestDelegationException
	 */
	public void handleRequest(Database db, MolgenisRequest request) throws HandleRequestDelegationException
	{
		// automatically calls functions with same name as action
		delegate(request.getAction(), db, request, null);
	}

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream out)
			throws HandleRequestDelegationException
	{
		// automatically calls functions with same name as action
		delegate(request.getAction(), db, request, out);

		// default show
		return Show.SHOW_MAIN;
	}

	@Deprecated
	public void delegate(String action, Database db, MolgenisRequest request) throws HandleRequestDelegationException
	{
		this.delegate(action, db, request, null);
	}

	public void delegate(String action, Database db, MolgenisRequest request, OutputStream out)
			throws HandleRequestDelegationException
	{
		// try/catch for db.rollbackTx
		try
		{
			// try/catch for method calling
			try
			{
				db.beginTx();
				logger.debug("trying to use reflection to call " + this.getClass().getName() + "." + action);
				Method m = this.getClass().getMethod(action, Database.class, MolgenisRequest.class);
				m.invoke(this, db, request);
				logger.debug("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
						+ " completed");
				if (db.inTx()) db.commitTx();
			}
			catch (NoSuchMethodException e1)
			{
				logger.warn(e1);

				if (out != null) try
				{
					// db.beginTx();
					logger.debug("trying to use reflection to call " + this.getClass().getName() + "." + action);
					Method m = this.getClass().getMethod(action, Database.class, MolgenisRequest.class,
							OutputStream.class);
					m.invoke(this, db, request, out);
					logger.debug("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
							+ " completed");
					if (db.inTx()) db.commitTx();
				}
				catch (Exception e)
				{
					this.getModel().setMessages(new ScreenMessage("Unknown action: " + action, false));
					logger.error("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
							+ "(db,tuple) failed: " + e1.getMessage());
					e.printStackTrace();
					db.rollbackTx();
				}
			}
			catch (Exception e)
			{
				logger.error("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
						+ " failed: " + e.getMessage());
				e.printStackTrace();
				this.getModel().setMessages(new ScreenMessage(e.getCause().getMessage(), false));
				db.rollbackTx();
			}
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity> FormModel<E> getParentForm(Class<E> entityClass)
	{
		// here we gonna put the parent
		ScreenController<?> parent = getParent();
		while (parent != null)
		{
			if (parent instanceof FormController && ((FormController<?>) parent).getEntityClass().equals(entityClass))
			{
				return (FormModel<E>) parent.getModel();
			}
			else
			{
				parent = (ScreenController<?>) parent.getParent();
			}
		}
		throw new RuntimeException("Parent form of class " + entityClass.getName() + " is unknown in plugin name="
				+ getName());
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public ScreenController<?> getController()
	{
		return this;
	}

	@Override
	public Vector<ScreenMessage> getMessages()
	{
		return this.messages;
	}

	@Override
	public void setMessages(Vector<ScreenMessage> messages)
	{
		assert (messages != null);
		this.messages = messages;
	}

	@Override
	public void setMessages(ScreenMessage... messages)
	{
		this.messages.clear();
		for (ScreenMessage m : messages)
			this.messages.add(m);
	}

	@Override
	public void setSuccess(String message)
	{
		this.setMessages(new ScreenMessage(message, true));
	}

	@Override
	public void setError(String message)
	{
		this.setMessages(new ScreenMessage(message, false));
	}

	@Override
	public void setController(ScreenController<? extends ScreenModel> controller)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVisible()
	{
		if (this.getApplicationController().getLogin().isAuthenticated())
		{
			try
			{
				if (this.getApplicationController().getLogin().canRead(this))
				{
					return true;
				}
			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public String getLabel()
	{
		return this.label;
	}

	@Override
	public abstract ScreenView getView();
}
