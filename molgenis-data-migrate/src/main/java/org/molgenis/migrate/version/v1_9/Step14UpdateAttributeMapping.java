package org.molgenis.migrate.version.v1_9;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Sets the data type of the attribute 'algorithm' of entity 'AttributeMapping' to text.
 */
public class Step14UpdateAttributeMapping extends MolgenisUpgrade
{

	private JdbcTemplate template;

	private static final Logger LOG = LoggerFactory.getLogger(Step14UpdateAttributeMapping.class);

	public Step14UpdateAttributeMapping(DataSource dataSource)
	{
		super(13, 14);
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Changing dataType of AttributeMapping.algorithm to text");
		template.execute("update attributes set dataType='text' where name='algorithm';");
	}

}
