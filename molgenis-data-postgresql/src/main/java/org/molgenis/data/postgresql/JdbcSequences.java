package org.molgenis.data.postgresql;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.populate.Sequences;
import org.springframework.jdbc.core.JdbcOperations;

public class JdbcSequences implements Sequences {
  private final JdbcOperations jdbcOperations;
  private final PostgreSqlIdGenerator idGenerator;

  public JdbcSequences(PostgreSqlIdGenerator idGenerator, JdbcOperations jdbcOperations) {
    this.idGenerator = idGenerator;
    this.jdbcOperations = requireNonNull(jdbcOperations);
  }

  @Override
  public List<String> getSequences() {
    return jdbcOperations.queryForList("SELECT sequence_name FROM information_schema.sequences")
        .stream()
        .map(row -> (String) row.get("sequence_name"))
        .toList();
  }

  @Override
  public void setValue(String sequenceName, long value) {
    jdbcOperations.queryForMap("SELECT setval(?, ?)", sequenceName, value);
  }

  @Override
  public long generateId(Attribute attribute) {
    var sequenceName = idGenerator.generateSequenceName(attribute);
    jdbcOperations.execute(format("CREATE SEQUENCE IF NOT EXISTS \"%s\"", sequenceName));
    return (Long) jdbcOperations.queryForMap("select nextval(?)", sequenceName).get("nextval");
  }
}
