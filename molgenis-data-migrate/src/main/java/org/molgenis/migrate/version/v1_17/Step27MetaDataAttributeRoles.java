package org.molgenis.migrate.version.v1_17;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class Step27MetaDataAttributeRoles extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step27MetaDataAttributeRoles.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public Step27MetaDataAttributeRoles(DataSource dataSource)
	{
		super(26, 27);
		this.jdbcTemplate = new JdbcTemplate(requireNonNull(dataSource));
	}

	@Override
	public void upgrade()
	{
		LOG.debug("Updating metadata from version 26 to 27 ...");

		// Collect data
		String sql = "SELECT `fullName`, `identifier` FROM `attributes` LEFT JOIN `entities_attributes` ON `attributes`.`identifier`=`entities_attributes`.`attributes` WHERE `lookupAttribute` = ?";
		List<Pair<String, String>> entityAttrPairs = jdbcTemplate.query(sql, new Object[]
		{ Boolean.TRUE }, new RowMapper<Pair<String, String>>()
		{
			@Override
			public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				String entityName = rs.getString("fullName");
				String attrName = rs.getString("identifier");
				return new Pair<String, String>(entityName, attrName);
			}
		});

		Map<String, List<String>> entityAttrsMap = new LinkedHashMap<>();
		entityAttrPairs.forEach(entityAttrPair -> {
			String entityName = entityAttrPair.getA();
			List<String> attrs = entityAttrsMap.get(entityName);
			if (attrs == null)
			{
				attrs = new ArrayList<>();
				entityAttrsMap.put(entityName, attrs);
			}
			attrs.add(entityAttrPair.getB());
		});

		// Create entities_lookupattributes table
		jdbcTemplate.execute(
				"CREATE TABLE `entities_lookupattributes` (`order` int(11) DEFAULT NULL, `fullName` varchar(255) NOT NULL, `lookupAttributes` varchar(255) NOT NULL, KEY `fullName` (`fullName`), KEY `lookupAttributes` (`lookupAttributes`), CONSTRAINT `entities_lookupattributes_ibfk_1` FOREIGN KEY (`fullName`) REFERENCES `entities` (`fullName`) ON DELETE CASCADE, CONSTRAINT `entities_lookupattributes_ibfk_2` FOREIGN KEY (`lookupAttributes`) REFERENCES `attributes` (`identifier`) ON DELETE CASCADE) ENGINE=InnoDB");

		// Fill entities_lookupattributes table with data
		entityAttrsMap.entrySet().forEach(entry -> {
			AtomicInteger order = new AtomicInteger();
			String entityName = entry.getKey();
			entry.getValue().forEach(attrName -> {
				int currentOrder = order.getAndIncrement();
				jdbcTemplate.update(
						"INSERT INTO `entities_lookupattributes` (`order`,`fullName`,`lookupAttributes`) VALUES (?, ?, ?)",
						new Object[]
				{ currentOrder, entityName, attrName });

			});

		});

		// remove idAttribute, labelAttribute and lookupAttribute column from attributes table
		jdbcTemplate.execute(
				"ALTER TABLE `attributes` DROP COLUMN `idAttribute`, DROP COLUMN `labelAttribute`, DROP COLUMN `lookupAttribute`");

		LOG.info("Updated metadata from version 26 to 27");
	}
}
