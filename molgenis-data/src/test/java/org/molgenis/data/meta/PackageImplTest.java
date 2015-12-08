package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.PackageChangeListener;
import org.testng.annotations.Test;

public class PackageImplTest
{
	@Test
	public void addChangeListenerAddEntity()
	{
		PackageImpl package_ = new PackageImpl("package");
		PackageChangeListener changeListener = mock(PackageChangeListener.class);
		package_.addChangeListener(changeListener);
		package_.addEntity(mock(EntityMetaData.class));
		verify(changeListener, times(1)).onChange(package_);
	}

	@Test
	public void addChangeListenerAddSubPackage()
	{
		PackageImpl package_ = new PackageImpl("package");
		PackageChangeListener changeListener = mock(PackageChangeListener.class);
		package_.addChangeListener(changeListener);
		package_.addSubPackage(new PackageImpl("subPackage"));
		verify(changeListener, times(1)).onChange(package_);
	}

	@Test
	public void removeChangeListener()
	{
		PackageImpl package_ = new PackageImpl("package");
		PackageChangeListener changeListener = when(mock(PackageChangeListener.class).getId()).thenReturn("id0")
				.getMock();
		package_.addChangeListener(changeListener);
		package_.removeChangeListener("id0");
		package_.addEntity(mock(EntityMetaData.class));
		verify(changeListener, times(0)).onChange(package_);
	}

	@Test
	public void removeChangeListenerNoListeners()
	{
		PackageImpl package_ = new PackageImpl("package");
		package_.removeChangeListener("entityName");
	}

	@Test
	public void removeChangeListenerListenerDoesNotExist()
	{
		PackageImpl package_ = new PackageImpl("package");
		PackageChangeListener changeListener = mock(PackageChangeListener.class);
		package_.addChangeListener(changeListener);
		package_.removeChangeListener("entityName");
	}
}
