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

class Step40AddRoleSystemTest extends AbstractMockitoTest {
  @Mock private JdbcTemplate jdbcTemplate;
  private Step40AddRoleSystem step40AddRoleSystem;

  @BeforeEach
  void setUpBeforeEach() {
    step40AddRoleSystem = new Step40AddRoleSystem(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step40AddRoleSystem.upgrade();
    verify(jdbcTemplate).execute(any(String.class));
  }

  @Test
  void upgradeException() {
    DataAccessException dataAccessException = mock(DataAccessException.class);
    doThrow(dataAccessException).when(jdbcTemplate).execute(any(String.class));
    assertThrows(DataAccessException.class, () -> step40AddRoleSystem.upgrade());
  }
}
