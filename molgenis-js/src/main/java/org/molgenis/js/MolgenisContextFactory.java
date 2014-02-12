package org.molgenis.js;

import java.lang.reflect.Method;
import java.util.Set;

import org.molgenis.js.methods.GlobalMethods;
import org.molgenis.js.sandbox.SandboxNativeJavaObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptableObject;

/**
 * {@link ContextFactory} for Molgenis js.
 * 
 * Creates MolgenisContexts that are sandboxed.
 * 
 * Creates a shared scope that contains the global functions and javascript prototypes.
 * 
 * You should always call MolgenisContextFactory.initSharedScope() first.
 * 
 */
public class MolgenisContextFactory extends ContextFactory
{
	// The global scope shared by all evaluated scripts. Should contain top-level functions and prototypes.
	private ScriptableObject sharedScope;

	public ScriptableObject getSharedScope()
	{
		return sharedScope;
	}

	@Override
	protected Context makeContext()
	{
		return new MolgenisContext(this);
	}

	public void initSharedScope()
	{
		sharedScope = (ScriptableObject) ContextFactory.getGlobal().call(new ContextAction()
		{
			@Override
			public Object run(Context cx)
			{
				ScriptableObject scriptableObject = cx.initStandardObjects(null, true);

				// Add global methods
				Set<Method> globalMethods = SandboxNativeJavaObject.getJavascriptMethods(GlobalMethods.class);
				for (Method method : globalMethods)
				{
					FunctionObject functionObject = new FunctionObject(method.getName(), method, scriptableObject);
					scriptableObject.defineProperty(method.getName(), functionObject, ScriptableObject.DONTENUM);
				}

				ScriptableValue scriptableValueProto = ScriptableValuePrototypeFactory.buildPrototype();
				ScriptableObject.putProperty(scriptableObject, scriptableValueProto.getClassName(),
						scriptableValueProto);

				scriptableObject.sealObject();

				return scriptableObject;
			}
		});
	}
}
