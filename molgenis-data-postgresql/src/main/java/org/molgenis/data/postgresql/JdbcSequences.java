package org.molgenis.data.postgresql;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.management.openmbean.InvalidKeyException;
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
    validateSequenceName(sequenceName);
    jdbcOperations.queryForMap("SELECT setval(?, ?)", sequenceName, value);
  }

  public long getValue(String sequenceName) {
    validateSequenceName(sequenceName);
    return (Long)
        jdbcOperations
            .queryForMap(format("SELECT last_value FROM \"%s\"", sequenceName))
            .get("last_value");
  }

  @Override
  public void deleteSequence(String sequenceName) {
    validateSequenceName(sequenceName);
    jdbcOperations.execute(format("DROP SEQUENCE \"%s\"", sequenceName));
  }

  @Override
  public long generateId(Attribute attribute) {
    var sequenceName = idGenerator.generateSequenceName(attribute);
    jdbcOperations.execute(format("CREATE SEQUENCE IF NOT EXISTS \"%s\"", sequenceName));
    return (Long) jdbcOperations.queryForMap("select nextval(?)", sequenceName).get("nextval");
  }

  /**
   * Checks if a sequence exists, throws an InvalidKeyException otherwise.
   *
   * <p>Sequence names (like table names) can't be escaped in a prepared statement so to protect
   * against SQL injection we check if the sequence exists beforehand.
   *
   * <p>Also see:
   * https://stackoverflow.com/questions/11312155/how-to-use-a-tablename-variable-for-a-java-prepared-statement-insert
   */
  private void validateSequenceName(String sequenceName) {
    var sequences = getSequences();
    if (!sequences.contains(sequenceName)) {
      throw new InvalidKeyException("Sequence does not exist");
    }
  }
}
