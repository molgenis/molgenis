package org.molgenis.migrate.version.v1_17;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.jdbc.core.RowCallbackHandler;
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

		// replace idAttribute/labelAttribute attribute names with attribute identifiers
		sql = "SELECT `fullName`,`identifier`,`name` FROM `entities_attributes` LEFT JOIN `attributes` ON `attributes`.`identifier` = `entities_attributes`.`attributes`";
		List<Triple<String, String, String>> triples = jdbcTemplate.query(sql,
				new RowMapper<Triple<String, String, String>>()
				{
					@Override
					public Triple<String, String, String> mapRow(ResultSet rs, int rowNum) throws SQLException
					{
						return new Triple<String, String, String>(rs.getString("fullName"), rs.getString("identifier"),
								rs.getString("name"));
					}
				});
		Map<String, Map<String, String>> entityAttrMap = new HashMap<>();
		triples.forEach(triple -> {
			String entityName = triple.getA();
			Map<String, String> attrMap = entityAttrMap.get(entityName);
			if (attrMap == null)
			{
				attrMap = new HashMap<>();
				entityAttrMap.put(entityName, attrMap);
			}
			attrMap.put(triple.getC(), triple.getB());
		});

		sql = "SELECT `fullName`,`idAttribute`,`labelAttribute` FROM `entities`";
		jdbcTemplate.query(sql, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet rs) throws SQLException
			{
				String entityName = rs.getString("fullName");
				String idAttrName = rs.getString("idAttribute");
				String labelAttrName = rs.getString("labelAttribute");
				String updateSql = "UPDATE `entities` SET `idAttribute` = ?, `labelAttribute` = ? WHERE `fullName` = ?";
				jdbcTemplate.update(updateSql, new Object[]
				{ entityAttrMap.get(entityName).get(idAttrName), entityAttrMap.get(entityName).get(labelAttrName),
						entityName });
			}
		});
		// convert idAttribute from string to xref
		jdbcTemplate.execute("ALTER TABLE `entities` MODIFY `idAttribute` varchar(255) DEFAULT NULL");
		jdbcTemplate.execute("ALTER TABLE `entities` ADD KEY `idAttribute` (`idAttribute`)");
		jdbcTemplate.execute(
				"ALTER TABLE `entities` ADD CONSTRAINT `entities_ibfk_3` FOREIGN KEY (`idAttribute`) REFERENCES `attributes` (`identifier`)");

		// convert labelAttribute from string to xref
		jdbcTemplate.execute("ALTER TABLE `entities` MODIFY `labelAttribute` varchar(255) DEFAULT NULL");
		jdbcTemplate.execute("ALTER TABLE `entities` ADD KEY `labelAttribute` (`labelAttribute`)");
		jdbcTemplate.execute(
				"ALTER TABLE `entities` ADD CONSTRAINT `entities_ibfk_4` FOREIGN KEY (`labelAttribute`) REFERENCES `attributes` (`identifier`)");

		jdbcTemplate.execute(
				"ALTER TABLE `attributes` MODIFY COLUMN `dataType` ENUM('bool', 'categorical', 'categoricalmref', 'compound', 'date', 'datetime', 'decimal', 'email', 'enum', 'file', 'html', 'hyperlink', 'image', 'int', 'long', 'mref', 'script', 'string', 'text', 'xref')");

		LOG.info("Updated metadata from version 26 to 27");
	}

	private static class Triple<T1, T2, T3>
	{
		private final T1 a;
		private final T2 b;
		private final T3 c;

		public Triple(T1 a, T2 b, T3 c)
		{
			this.a = a;
			this.b = b;
			this.c = c;
		}

		public T1 getA()
		{
			return a;
		}

		public T2 getB()
		{
			return b;
		}

		public T3 getC()
		{
			return c;
		}
	}
}
