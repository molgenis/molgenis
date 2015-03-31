package org.molgenis.data.version;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Upgrades the data backends to the current meta data version.
 * 
 * The version the database is generated with should be defined in the molgenis-server.properties with the key
 * 'meta.data.version'
 */
@Service
public class MetaDataUpgradeService
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final MetaDataVersionService metaDataVersionService;
	private final List<MetaDataUpgrade> upgrades = new ArrayList<>();

	@Autowired
	public MetaDataUpgradeService(MetaDataVersionService metaDataVersionService)
	{
		this.metaDataVersionService = metaDataVersionService;
	}

	public void addUpgrade(MetaDataUpgrade upgrade)
	{
		upgrades.add(upgrade);
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
					.forEach(this::runUpgrade);

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

	private void runUpgrade(MetaDataUpgrade upgrade)
	{
		LOG.info("Upgrading from {} to {}...", upgrade.getFromVersion(), upgrade.getToVersion());
		upgrade.upgrade();
		LOG.debug("Upgraded from {} to {}.", upgrade.getFromVersion(), upgrade.getToVersion());
		metaDataVersionService.updateToVersion(upgrade.getToVersion());
	}
}
