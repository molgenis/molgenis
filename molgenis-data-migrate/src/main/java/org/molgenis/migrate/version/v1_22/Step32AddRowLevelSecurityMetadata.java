package org.molgenis.migrate.version.v1_22;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Step32AddRowLevelSecurityMetadata extends MolgenisUpgrade
{
	public static final String UPDATE = "_UPDATE";
	public static final MolgenisUserMetaData MOLGENIS_USER_META_DATA = new MolgenisUserMetaData();
	private final Logger LOG = LoggerFactory.getLogger(Step32AddRowLevelSecurityMetadata.class);

	private final JdbcTemplate jdbcTemplate;
	private final IdGenerator idGenerator;

	private List<String> entitiesToSecure = Collections.EMPTY_LIST;

	private static final String VARCHAR = "VARCHAR(255)";

	@Autowired
	public Step32AddRowLevelSecurityMetadata(DataSource dataSource, IdGenerator idGenerator)
	{
		super(31, 32);
		this.jdbcTemplate = new JdbcTemplate(requireNonNull(dataSource));
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating entities table ...");

		// update existing settings table
		jdbcTemplate.execute("ALTER TABLE entities ADD COLUMN `rowLevelSecured` boolean");

		LOG.debug("Updated application settings");

		LOG.info("Updating entities to secure with row level security...");
		for (String fullname : entitiesToSecure)
		{
			try
			{
				LOG.info("Updating [{}] with row level security...", fullname);
				String rowLevelSecurityId = idGenerator.generateId();
				jdbcTemplate.update(
						"INSERT INTO attributes (`identifier`,`name`,`dataType`,`refEntity`,`expression`,`nillable`,`auto`,`visible`,`label`,`description`,`aggregateable`,`enumOptions`,`rangeMin`,`rangeMax`,`readOnly`,`unique`,`visibleExpression`,`validationExpression`,`defaultValue`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						rowLevelSecurityId, UPDATE, "mref", new MolgenisUserMetaData().getName(), null, true, false,
						true, "name", "desc", false, null, null, null, false, false, null, null, "");

				jdbcTemplate.update(
						"INSERT INTO entities_attributes (`order`, `fullName`, `attributes`) VALUES (?, ?, ?)", 0,
						fullname, rowLevelSecurityId);

				AttributeMetaData idAttribute = null;// get idattribute from attributes table
				StringBuilder sql = new StringBuilder();

				// mysql keys cannot have TEXT value, so change it to VARCHAR when needed
				String idAttrMysqlType = (idAttribute.getDataType() instanceof StringField ? VARCHAR
						: idAttribute.getDataType().getMysqlType());

				String refAttrMysqlType = (MOLGENIS_USER_META_DATA.getIdAttribute().getDataType() instanceof StringField
						? VARCHAR : MOLGENIS_USER_META_DATA.getIdAttribute().getDataType().getMysqlType());

				sql.append(" CREATE TABLE ").append('`').append(fullname).append('_').append(UPDATE).append('`')
						.append("(`order` INT,`").append(idAttribute.getName()).append('`').append(' ')
						.append(idAttrMysqlType).append(" NOT NULL, ").append('`').append(UPDATE).append('`')
						.append(' ').append(refAttrMysqlType).append(" NOT NULL, FOREIGN KEY (").append('`')
						.append(idAttribute.getName()).append('`').append(") REFERENCES ").append('`').append(fullname)
						.append('`').append('(').append('`').append(idAttribute.getName())
						.append("`) ON DELETE CASCADE");

				sql.append(", FOREIGN KEY (").append('`').append(UPDATE).append('`').append(") REFERENCES ").append('`')
						.append(MOLGENIS_USER_META_DATA.getName()).append('`').append('(').append('`').append("ID")
						.append("`) ON DELETE CASCADE");

				sql.append(") ENGINE=InnoDB;");
			}
			catch (Exception e)
			{
				LOG.error("", e);
			}

		}
		LOG.info("Updated entities to secure with row level security...");
	}

	public void setEntitiesToSecure(List<String> entitiesToSecure)
	{
		this.entitiesToSecure = entitiesToSecure;
	}
}
