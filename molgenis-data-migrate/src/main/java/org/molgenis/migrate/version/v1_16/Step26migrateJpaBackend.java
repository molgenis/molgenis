package org.molgenis.migrate.version.v1_16;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step26migrateJpaBackend extends MolgenisUpgrade
{
	private final JdbcTemplate template;
	private final Logger LOG = LoggerFactory.getLogger(Step26migrateJpaBackend.class);
	private final String backend;

	@Autowired
	public Step26migrateJpaBackend(DataSource dataSource, String backend)
	{
		super(25, 26);
		requireNonNull(dataSource);
		this.template = new JdbcTemplate(dataSource);
		this.backend = backend;
	}

	@Override
	public void upgrade()
	{
		LOG.info("update entities with jpa backend, new backend: " + backend);
		template.execute("update entities set backend='" + backend + "' where backend = 'JPA'");
	}
}
