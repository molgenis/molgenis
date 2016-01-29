package org.molgenis.migrate.version.v1_16;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.data.IdGenerator;
import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step26migrateJpaBackend extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step26migrateJpaBackend.class);

	private final JdbcTemplate jdbcTemplate;
	private final IdGenerator idGenerator;
	private final String backend;

	@Autowired
	public Step26migrateJpaBackend(DataSource dataSource, String backend, IdGenerator idGenerator)
	{
		super(25, 26);
		requireNonNull(dataSource);
		this.idGenerator = requireNonNull(idGenerator);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.backend = backend;
	}

	@Override
	public void upgrade()
	{
		// Fix https://github.com/molgenis/molgenis/issues/4357: add MolgenisUser.googleAccountId
		String googleAccountId = idGenerator.generateId();
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`idAttribute`,`lookupAttribute`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`labelAttribute`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				googleAccountId, "googleAccountId", "string", null, null, true, false, false, false, true,
				"Google account ID", "An identifier for the user, unique among all Google accounts and never reused.",
				false, null, null, null, false, false, false, null, null, null);

		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 23,
				"MolgenisUser", googleAccountId);

		// Fix https://github.com/molgenis/molgenis/issues/4357: add MolgenisUser.languageCode
		String languageCode = idGenerator.generateId();
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`idAttribute`,`lookupAttribute`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`labelAttribute`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				languageCode, "languageCode", "string", null, null, true, false, false, false, true, "Language code",
				"Selected language for this site.", false, null, null, null, false, false, false, null, null, null);

		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 22,
				"MolgenisUser", languageCode);

		//Fix #4397 migration from 1.8.1 to 1.16 fails because of changes in the JPA entities
		jdbcTemplate.update(
				"UPDATE `attributes` set `description`='This is the hashed password, enter a new plaintext password to update.' WHERE `name`='password_'");

		LOG.info("update entities with jpa backend, new backend: " + backend);
		jdbcTemplate.execute("update entities set backend='" + backend + "' where backend = 'JPA'");
	}
}
