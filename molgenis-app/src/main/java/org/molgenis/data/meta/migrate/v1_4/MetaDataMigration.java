package org.molgenis.data.meta.migrate.v1_4;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaDataMigration implements InitializingBean
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaDataMigration.class);

	@Autowired
	private SearchService elasticSearchService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private AsyncJdbcTemplate asyncJdbcTemplate;

	@Autowired
	private MysqlRepositoryCollection mysqlRepositoryCollection;

	// private datacontext
	private DataService dataService = new DataServiceImpl();

	@Override
	public void afterPropertiesSet() throws Exception
	{
		new MysqlRepository(dataService, dataSource, asyncJdbcTemplate);
	}
}
