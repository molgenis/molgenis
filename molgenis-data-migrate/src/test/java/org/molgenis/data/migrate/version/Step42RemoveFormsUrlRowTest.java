package org.molgenis.data.migrate.version;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class Step42RemoveFormsUrlRowTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step42RemoveFormsUrlRow step42;

  @BeforeEach
  void setup() {
    step42 = new Step42RemoveFormsUrlRow(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step42.upgrade();
    verify(jdbcTemplate).execute(any(String.class));
  }

  @Test
  void upgradeException() {
    DataAccessException dataAccessException = mock(DataAccessException.class);
    doThrow(dataAccessException).when(jdbcTemplate).execute(any(String.class));
    assertThrows(DataAccessException.class, () -> step42.upgrade());
  }
}
