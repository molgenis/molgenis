package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisUpgradeServiceImplTest extends AbstractMockitoTest {
  @Mock private MolgenisVersionService molgenisVersionService;
  private MolgenisUpgradeServiceImpl molgenisUpgradeServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    molgenisUpgradeServiceImpl = new MolgenisUpgradeServiceImpl(molgenisVersionService);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUpgradeFromVersion30() {
    when(molgenisVersionService.getSchemaVersion()).thenReturn(30);
    molgenisUpgradeServiceImpl.upgrade();
  }

  @Test
  public void testUpgradeFromVersion31() {
    when(molgenisVersionService.getSchemaVersion()).thenReturn(31);
    assertFalse(molgenisUpgradeServiceImpl.upgrade());
  }
}
