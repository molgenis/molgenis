package org.molgenis.data.version;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.migrate.version.MolgenisUpgradeServiceImpl;
import org.molgenis.migrate.version.MolgenisVersionService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MetaDataUpgradeServiceTest
{
	private MolgenisUpgradeServiceImpl metaDataUpgradeService;
	private MolgenisVersionService metaDataVersionService;
	private MolgenisUpgrade upgradeFrom0;

	@BeforeMethod
	public void beforeMethod()
	{
		metaDataVersionService = mock(MolgenisVersionService.class);
		metaDataUpgradeService = new MolgenisUpgradeServiceImpl(metaDataVersionService);
		upgradeFrom0 = mock(MolgenisUpgrade.class);
		when(upgradeFrom0.getFromVersion()).thenReturn(0);
		metaDataUpgradeService.addUpgrade(upgradeFrom0);
	}

	@Test
	public void upgradeNotNeeded()
	{
		when(metaDataVersionService.getMolgenisVersionFromServerProperties())
				.thenReturn(MolgenisVersionService.CURRENT_VERSION);
		metaDataUpgradeService.upgrade();
		verify(metaDataVersionService, never()).updateToCurrentVersion();
		verify(upgradeFrom0, never()).upgrade();
	}

	@Test
	public void upgradeNeeded()
	{
		when(metaDataVersionService.getMolgenisVersionFromServerProperties()).thenReturn(0);
		metaDataUpgradeService.upgrade();
		verify(metaDataVersionService, times(1)).updateToCurrentVersion();
		verify(upgradeFrom0, times(1)).upgrade();
	}
}
