package org.molgenis.js.sandbox;

import org.mozilla.javascript.ClassShutter;

/**
 * A {@link ClassShutter} that locks out access to all native classes.
 */
public class SandboxClassShutter implements ClassShutter
{
	@Override
	public boolean visibleToScripts(String fullClassName)
	{
		return false;
	}

}
