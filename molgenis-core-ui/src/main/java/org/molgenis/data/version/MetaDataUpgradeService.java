package org.molgenis.data.version;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MetaDataUpgradeService
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final MetaDataVersionService metaDataVersionService;
	private final DataService dataService;
	private final List<MetaDataUpgrade> upgrades = new ArrayList<>();
	private final RepositoryCollection jpaRepositoryCollection;
	private final DataSource dataSource;
	private final SearchService searchService;

	@Autowired
	public MetaDataUpgradeService(MetaDataVersionService metaDataVersionService, DataService dataService,
			@Qualifier("JpaRepositoryCollection") RepositoryCollection jpaRepositoryCollection, DataSource dataSource,
			SearchService searchService)
	{
		this.metaDataVersionService = metaDataVersionService;
		this.dataService = dataService;
		this.jpaRepositoryCollection = jpaRepositoryCollection;
		this.dataSource = dataSource;
		this.searchService = searchService;
	}

	@PostConstruct
	public void addUpgrades()
	{
		upgrades.add(new UpgradeFrom0To1(dataService, jpaRepositoryCollection, dataSource, searchService));
	}

	public void upgrade()
	{
		if (metaDataVersionService.getDatabaseMetaDataVersion() < MetaDataVersionService.CURRENT_META_DATA_VERSION)
		{
			LOG.info("MetaData version:{}, current version:{} upgrade needed",
					metaDataVersionService.getDatabaseMetaDataVersion(),
					MetaDataVersionService.CURRENT_META_DATA_VERSION);

			upgrades.stream()
					.filter(upgrade -> upgrade.getFromVersion() >= metaDataVersionService.getDatabaseMetaDataVersion())
					.forEach(upgrade -> upgrade.upgrade());

			metaDataVersionService.updateToCurrentVersion();

			LOG.info("MetaData upgrade done.");
		}
		else
		{
			LOG.info("MetaData version:{}, current version:{} upgrade not needed",
					metaDataVersionService.getDatabaseMetaDataVersion(),
					MetaDataVersionService.CURRENT_META_DATA_VERSION);
		}
	}
}
