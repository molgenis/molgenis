package org.molgenis.data.migrate.version;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

class Step41RemoveSecurityUiKeysTest extends AbstractMockitoTest {
  @Mock private JdbcTemplate jdbcTemplate;
  private Step41RemoveSecurityUiKeys step41RemoveSecurityUiKeys;

  @BeforeEach
  void setUpBeforeEach() {
    step41RemoveSecurityUiKeys = new Step41RemoveSecurityUiKeys(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step41RemoveSecurityUiKeys.upgrade();
    verify(jdbcTemplate).execute(any(String.class));
  }

  @Test
  void upgradeException() {
    DataAccessException dataAccessException = mock(DataAccessException.class);
    doThrow(dataAccessException).when(jdbcTemplate).execute(any(String.class));
    assertThrows(DataAccessException.class, () -> step41RemoveSecurityUiKeys.upgrade());
  }
}
