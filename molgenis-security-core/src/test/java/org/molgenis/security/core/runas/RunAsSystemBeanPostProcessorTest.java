package org.molgenis.security.core.runas;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.testng.annotations.Test;

public class RunAsSystemBeanPostProcessorTest
{

	@Test
	public void postProcessAfterInitialization()
	{
		// Should return a proxy so should not be equal to this
		assertNotEquals(new RunAsSystemBeanPostProcessor().postProcessAfterInitialization(this, "Test"), this);

		Object o = new Object();
		// Object does not have a method with the RunAsSystem annotation so should not return a proxy
		assertEquals(new RunAsSystemBeanPostProcessor().postProcessAfterInitialization(o, "Test"), o);
	}

	@Test
	public void postProcessBeforeInitialization()
	{
		assertEquals(new RunAsSystemBeanPostProcessor().postProcessBeforeInitialization(this, "Test"), this);
	}

	@RunAsSystem
	public void test()
	{
	}
}
