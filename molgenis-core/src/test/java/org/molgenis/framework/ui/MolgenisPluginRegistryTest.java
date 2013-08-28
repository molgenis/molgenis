package org.molgenis.framework.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

public class MolgenisPluginRegistryTest
{
	@Test
	public void getInstance()
	{
		MolgenisPlugin molgenisPlugin = mock(MolgenisPlugin.class);
		when(molgenisPlugin.getId()).thenReturn("id");
		MolgenisPluginRegistry.getInstance().register(molgenisPlugin);
		Iterator<Class<? extends MolgenisPlugin>> it = MolgenisPluginRegistry.getInstance().getPluginClasses()
				.iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), molgenisPlugin.getClass());
		assertFalse(it.hasNext());
	}
}
