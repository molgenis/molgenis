package org.molgenis.data.version.v1_8;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;

import org.molgenis.data.support.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step11cAttributeMappingAddSourceAttributeMetaDatas
{
	private static final Logger LOG = LoggerFactory.getLogger(Step11cAttributeMappingAddSourceAttributeMetaDatas.class);

	private final DataSource dataSource;

	public Step11cAttributeMappingAddSourceAttributeMetaDatas(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	public void upgrade()
	{
		LOG.info("Updating metadata from version 11.1 to 11.2 ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.execute("ALTER TABLE AttributeMapping ADD sourceAttributeMetaDatas TEXT");

		String attrId = new UuidGenerator().generateId();
		jdbcTemplate.update(
				"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`idAttribute`,`lookupAttribute`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`labelAttribute`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				attrId, "sourceAttributeMetaDatas", "string", null, null, true, false, false, false, true,
				"sourceAttributeMetaDatas", null, false, null, null, null, false, false, false, null, null);
		jdbcTemplate.update("INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 3,
				"AttributeMapping", attrId);

		LOG.info("Added AttributeMapping.sourceAttributeMetaDatas");
	}
}
