package org.molgenis.js;

import java.lang.reflect.Method;
import java.util.Set;

import org.molgenis.js.methods.NumericMethods;
import org.molgenis.js.sandbox.SandboxNativeJavaObject;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import com.google.common.collect.ImmutableSet;

/**
 * Creates ScriptableValue javascript prototype.
 * 
 * The javascript methods are provided by static methods of the given java classes. These methods should have this
 * signature:
 * 
 * public static Object functionName (Context ctx, Scriptable thisObj, Object[] args, Function funObj)
 * 
 */
public class ScriptableValuePrototypeFactory
{
	private static final Set<Class<?>> methodProvidingClasses = ImmutableSet.<Class<?>> of(NumericMethods.class);

	public static ScriptableValue buildPrototype()
	{
		ScriptableValue ctor = new ScriptableValue();
		ScriptableObject valuePrototype = new NativeObject();

		for (Class<?> clazz : methodProvidingClasses)
		{
			Set<Method> valuePrototypeMethods = SandboxNativeJavaObject.getJavascriptMethods(clazz);
			for (Method method : valuePrototypeMethods)
			{
				FunctionObject functionObject = new FunctionObject(method.getName(), method, valuePrototype);
				valuePrototype.defineProperty(method.getName(), functionObject, ScriptableObject.DONTENUM);
			}
			ScriptableObject.putConstProperty(ctor, "prototype", valuePrototype);
		}

		return ctor;
	}
}
