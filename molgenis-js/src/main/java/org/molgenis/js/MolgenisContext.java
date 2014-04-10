package org.molgenis.js;

import org.molgenis.data.Entity;
import org.molgenis.js.sandbox.SandboxedContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * {@link Context} to be used for running molgenis javacripts in.
 * 
 * Used to create local scopes, and holds the {@link Entity} where to evaluate the script against
 */
public class MolgenisContext extends SandboxedContext
{
	public MolgenisContext(MolgenisContextFactory factory)
	{
		super(factory);
	}

	/**
	 * Casts the Context to the MolgenisContext
	 * 
	 * @throws MolgenisJsException
	 *             if the Context is not a MolgenisContext, this happens if RhinoConfig.init() isn't called
	 * @param ctx
	 * @return ctx as MolgenisContext
	 */
	public static MolgenisContext asMolgenisContext(Context ctx)
	{
		try
		{
			return (MolgenisContext) ctx;
		}
		catch (ClassCastException e)
		{
			throw new MolgenisJsException("No MolgenisContex available, please call RhinoCongig.init() first.");
		}
	}

	public ScriptableObject getSharedScope()
	{
		return getMolgenisContextFactory().getSharedScope();
	}

	/**
	 * Creates a new {@code Scriptable} instance for use as a transient scope. The returned {@code Scriptable} has no
	 * parent scope and has the {@code sharedScope} as prototype.
	 * <p/>
	 * The purpose of this method is to obtain a scope instance that extends the global scope and into which new objects
	 * and properties can be defined without polluting the global scope.
	 * 
	 * @return a new instance of {@code Scriptable} for use as a top-level scope.
	 */
	public Scriptable newLocalScope()
	{
		// Create a new object within the sharedScope
		Scriptable scope = newObject(getSharedScope());

		// Set its prototype
		scope.setPrototype(getSharedScope());

		// Remove its parent scope (makes it a top-level scope)
		scope.setParentScope(null);

		return scope;
	}

	/**
	 * The {@link Entity} to evaluate the script against.
	 * 
	 * @param entity
	 */
	public void setEntity(Entity entity)
	{
		putThreadLocal(Entity.class, entity);
	}

	public Entity getEntity()
	{
		return (Entity) getThreadLocal(Entity.class);
	}

	protected MolgenisContextFactory getMolgenisContextFactory()
	{
		return (MolgenisContextFactory) getFactory();
	}
}
