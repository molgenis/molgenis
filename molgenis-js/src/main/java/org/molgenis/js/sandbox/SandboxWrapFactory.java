package org.molgenis.js.sandbox;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * A {@link WrapFactory} that ensures {@link org.mozilla.javascript.NativeJavaObject} instances are of the
 * {@link SandboxNativeJavaObject} variety.
 */
public class SandboxWrapFactory extends WrapFactory
{

	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType)
	{
		return new SandboxNativeJavaObject(scope, javaObject, staticType);
	}
}
