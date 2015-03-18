package org.molgenis.data.version;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MetaDataUpgradeServiceTest
{
	private MetaDataUpgradeService metaDataUpgradeService;
	private MetaDataVersionService metaDataVersionService;
	private MetaDataUpgrade upgradeFrom0;

	@BeforeMethod
	public void beforeMethod()
	{
		metaDataVersionService = mock(MetaDataVersionService.class);
		metaDataUpgradeService = new MetaDataUpgradeService(metaDataVersionService, null, null, null, null);
		upgradeFrom0 = mock(MetaDataUpgrade.class);
		when(upgradeFrom0.getFromVersion()).thenReturn(0);
		metaDataUpgradeService.addUpgrade(upgradeFrom0);
	}

	@Test
	public void upgradeNotNeeded()
	{
		when(metaDataVersionService.getDatabaseMetaDataVersion()).thenReturn(1);
		metaDataUpgradeService.upgrade();
		verify(metaDataVersionService, never()).updateToCurrentVersion();
		verify(upgradeFrom0, never()).upgrade();
	}

	@Test
	public void upgradeNeeded()
	{
		when(metaDataVersionService.getDatabaseMetaDataVersion()).thenReturn(0);
		metaDataUpgradeService.upgrade();
		verify(metaDataVersionService, times(1)).updateToCurrentVersion();
		verify(upgradeFrom0, times(1)).upgrade();
	}
}
