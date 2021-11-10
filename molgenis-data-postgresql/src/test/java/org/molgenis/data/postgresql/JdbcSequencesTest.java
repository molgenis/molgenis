package org.molgenis.data.postgresql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.meta.model.Attribute;
import org.springframework.jdbc.core.JdbcOperations;

@ExtendWith(MockitoExtension.class)
class JdbcSequencesTest {

  @InjectMocks JdbcSequences jdbcSequences;

  @Mock PostgreSqlIdGenerator postgreSqlIdGenerator;
  @Mock JdbcOperations jdbcOperations;
  @Mock Attribute attribute;

  @Test
  void testGetSequences() {
    when(jdbcOperations.queryForList("SELECT sequence_name FROM information_schema.sequences"))
        .thenReturn(List.of(Map.of("sequence_name", "seq1"), Map.of("sequence_name", "seq2")));
    assertEquals(List.of("seq1", "seq2"), jdbcSequences.getSequences());
  }

  @Test
  void testSetValue() {
    jdbcSequences.setValue("abc", 123);
    verify(jdbcOperations).queryForMap("SELECT setval(?, ?)", "abc", 123L);
  }

  @Test
  void testGenerateId() {
    when(postgreSqlIdGenerator.generateSequenceName(attribute)).thenReturn("seq");
    when(jdbcOperations.queryForMap("select nextval(?)", "seq"))
        .thenReturn(Map.of("nextval", 123L));

    assertEquals(123L, jdbcSequences.generateId(attribute));

    verify(jdbcOperations).execute("CREATE SEQUENCE IF NOT EXISTS \"seq\"");
  }
}
