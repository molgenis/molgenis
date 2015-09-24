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
		LOG.info("Updating metadata from version 15 to 15.1 ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> mappingProjects = jdbcTemplate
				.queryForList("SELECT identifier,owner from MappingProject");
		mappingProjects.forEach(map -> {
			String mappingProjectId = map.get("identifier").toString();
			String username = map.get("owner").toString();
			Map<String, Object> molgenisUser = jdbcTemplate
					.queryForMap("SELECT id,username from MolgenisUser WHERE username = ?", username);
			String molgenisUserId = molgenisUser.get("id").toString();
			jdbcTemplate.update("UPDATE MappingProject SET owner = ? WHERE identifier = ?", molgenisUserId,
					mappingProjectId);
		});

		LOG.info("Updated MappingProject owners string -> xref");
	}
}
