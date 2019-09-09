package org.molgenis.data.migrate.version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

class MolgenisUpgradeServiceImplTest extends AbstractMockitoTest {
  @Mock private MolgenisVersionService molgenisVersionService;
  private MolgenisUpgradeServiceImpl molgenisUpgradeServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    molgenisUpgradeServiceImpl = new MolgenisUpgradeServiceImpl(molgenisVersionService);
  }

  @Test
  void testUpgradeFromVersion30() {
    when(molgenisVersionService.getSchemaVersion()).thenReturn(30);
    assertThrows(UnsupportedOperationException.class, () -> molgenisUpgradeServiceImpl.upgrade());
  }

  @Test
  void testUpgradeFromVersion31() {
    when(molgenisVersionService.getSchemaVersion()).thenReturn(31);
    assertFalse(molgenisUpgradeServiceImpl.upgrade());
  }
}
