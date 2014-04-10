package org.molgenis.js.sandbox;

import java.lang.reflect.Method;
import java.util.Set;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import com.google.common.collect.ImmutableSet;

/**
 * A sandboxed {@link NativeJavaObject} that prevents using reflection to escape a sandbox.
 */
public class SandboxNativeJavaObject extends NativeJavaObject
{
	private static final long serialVersionUID = 7690142963749803499L;
	private static final Set<String> EXCLUDED_METHODS = ImmutableSet.of("wait", "toString", "getClass", "equals",
			"hashCode", "notify", "notifyAll");

	public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType)
	{
		super(scope, javaObject, staticType);
	}

	@Override
	public Object get(String name, Scriptable start)
	{
		if (EXCLUDED_METHODS.contains(name))
		{
			return NOT_FOUND;
		}

		return super.get(name, start);
	}

	public static Set<Method> getJavascriptMethods(Class<?> clazz)
	{
		ImmutableSet.Builder<Method> builder = ImmutableSet.builder();

		for (Method method : clazz.getMethods())
		{
			String methodName = method.getName();

			if (!EXCLUDED_METHODS.contains(methodName))
			{
				builder.add(method);
			}
		}

		return builder.build();
	}
}
