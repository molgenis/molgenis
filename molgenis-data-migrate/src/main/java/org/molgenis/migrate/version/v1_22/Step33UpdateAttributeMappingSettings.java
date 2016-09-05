package org.molgenis.migrate.version.v1_22;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class Step33UpdateAttributeMappingSettings extends MolgenisUpgrade
{
	private final Logger LOG = getLogger(Step33UpdateAttributeMappingSettings.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public Step33UpdateAttributeMappingSettings(DataSource dataSource)
	{
		super(31, 33);
		this.jdbcTemplate = new JdbcTemplate(requireNonNull(dataSource));
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating AttributeMapping metadata, changing sourceAttributeMetaDatas from [STRING] to [TEXT]");
		updateDataType("AttributeMapping", "sourceAttributeMetaDatas", "text");
		LOG.debug("Updated AttributeMapping metadata");

	}

	private void updateDataType(String entityFullName, String attributeName, String newDataType)
	{
		LOG.info("Update data type of {}.{} to {}...", entityFullName, attributeName, newDataType);
		String attributeId = jdbcTemplate.queryForObject(
				"SELECT a.identifier FROM entities_attributes ea JOIN attributes a ON ea.attributes = a.identifier WHERE ea.fullName = '"
						+ entityFullName + "' AND a.name='" + attributeName + "'", String.class);
		jdbcTemplate
				.update("UPDATE attributes SET dataType = '" + newDataType + "', refEntity = NULL WHERE identifier = '"
						+ attributeId + "'");
	}
}
