package org.molgenis.migrate.version.v1_11;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import static java.util.Objects.requireNonNull;

/**
 * Migration step needed after fix for #3913 "LoggingEvents aren't stored"
 */
public class Step21SetLoggingEventBackend extends MolgenisUpgrade{
	
	private final JdbcTemplate template;
	
	private static final Logger LOG = LoggerFactory.getLogger(Step21SetLoggingEventBackend.class);
	
	public Step21SetLoggingEventBackend(DataSource dataSource) {
		super(20, 21);
		
		requireNonNull(dataSource);
		this.template = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void upgrade()
	{
		LOG.info("Setting LoggingEvent's backend to ElasticSearch...");
		template.update("UPDATE entities SET backend = ? WHERE fullName = ?", "ElasticSearch", "LoggingEvent");
		
		LOG.info("Dropping LoggingEvent MySQL table...");
		template.execute("DROP TABLE LoggingEvent");
	}
}
