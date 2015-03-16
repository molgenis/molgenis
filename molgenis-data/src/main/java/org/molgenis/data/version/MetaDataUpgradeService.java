package org.molgenis.data.version;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaDataUpgradeService
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final MetaDataVersionService metaDataVersionService;
	private final DataService dataService;
	private final List<MetaDataUpgrade> upgrades = new ArrayList<>();

	@Autowired
	public MetaDataUpgradeService(MetaDataVersionService metaDataVersionService, DataService dataService)
	{
		this.metaDataVersionService = metaDataVersionService;
		this.dataService = dataService;
	}

	@PostConstruct
	public void addUpgrades()
	{
		upgrades.add(new UpgradeFrom0To1(dataService));
	}

	public void upgrade(ManageableRepositoryCollection defaultBackend)
	{
		if (metaDataVersionService.getDatabaseMetaDataVersion() < MetaDataVersionService.CURRENT_META_DATA_VERSION)
		{
			LOG.info("MetaData version:{}, current version:{} upgrade needed",
					metaDataVersionService.getDatabaseMetaDataVersion(),
					MetaDataVersionService.CURRENT_META_DATA_VERSION);

			upgrades.stream()
					.filter(upgrade -> upgrade.getFromVersion() >= metaDataVersionService.getDatabaseMetaDataVersion())
					.forEach(upgrade -> upgrade.upgrade(defaultBackend));

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
