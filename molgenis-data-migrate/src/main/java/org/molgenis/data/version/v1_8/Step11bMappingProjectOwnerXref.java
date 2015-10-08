package org.molgenis.data.version.v1_8;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step11bMappingProjectOwnerXref
{
	private static final Logger LOG = LoggerFactory.getLogger(Step11bMappingProjectOwnerXref.class);

	private final DataSource dataSource;

	public Step11bMappingProjectOwnerXref(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	public void upgrade()
	{
		LOG.info("Updating metadata from version 11 to 11.1 ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> mappingProjects = jdbcTemplate
				.queryForList("SELECT identifier,owner FROM MappingProject");
		mappingProjects.forEach(map -> {
			String mappingProjectId = map.get("identifier").toString();
			String username = map.get("owner").toString();
			Map<String, Object> molgenisUser = jdbcTemplate
					.queryForMap("SELECT id,username FROM MolgenisUser WHERE username = ?", username);
			String molgenisUserId = molgenisUser.get("id").toString();
			jdbcTemplate.update("UPDATE MappingProject SET owner = ? WHERE identifier = ?", molgenisUserId,
					mappingProjectId);
		});

		// Change column type from text to varchar so that so that a foreign key can be created
		jdbcTemplate.execute("ALTER TABLE MappingProject MODIFY owner VARCHAR(255)");

		// MolgenisUser exists in the same backend, create a foreign key
		jdbcTemplate.execute(
				"ALTER TABLE MappingProject ADD CONSTRAINT FK_MappingProject_owner FOREIGN KEY (owner) REFERENCES MolgenisUser(id)");

		// Update attributes table
		String attrIdentifier = jdbcTemplate.queryForObject(
				"SELECT identifier FROM attributes JOIN entities_attributes ON attributes.identifier = entities_attributes.attributes WHERE fullName = ? and name = ?",
				new Object[]
		{ "MappingProject", "owner" }, String.class);

		jdbcTemplate.update("UPDATE attributes SET dataType = ?, refEntity = ? WHERE identifier = ?", "xref",
				"MolgenisUser", attrIdentifier);

		LOG.info("Updated MappingProject owners string -> xref");

		// Injecting migration steps is not possible, after a release was performed future versions will use the next
		// available step number. As a workaround a required step in a previous release is called from an existing step.
		new Step11cAttributeMappingAddSourceAttributeMetaDatas(dataSource).upgrade();
	}
}
