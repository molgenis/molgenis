package org.molgenis.migrate.version;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.framework.MolgenisUpgradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Upgrades the data backends to the current version.
 * 
 * Gets the current version from the {@link MolgenisVersionService}.
 */
@Service
public class MolgenisUpgradeServiceImpl implements MolgenisUpgradeService
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final MolgenisVersionService versionService;
	private final List<MolgenisUpgrade> upgrades = new ArrayList<>();

	@Autowired
	public MolgenisUpgradeServiceImpl(MolgenisVersionService versionService)
	{
		this.versionService = checkNotNull(versionService);
	}

	public void addUpgrade(MolgenisUpgrade upgrade)
	{
		upgrades.add(upgrade);
	}

	/**
	 * Executes MOLGENIS MetaData version upgrades.
	 * 
	 * @return true if an upgrade was necessary, false if not
	 */
	@Override
	public boolean upgrade()
	{
		if (versionService.getMolgenisVersionFromServerProperties() < MolgenisVersionService.CURRENT_VERSION)
		{
			LOG.info("MetaData version:{}, current version:{} upgrade needed",
					versionService.getMolgenisVersionFromServerProperties(), MolgenisVersionService.CURRENT_VERSION);

			upgrades.stream().filter(
					upgrade -> upgrade.getFromVersion() >= versionService.getMolgenisVersionFromServerProperties())
					.forEach(this::runUpgrade);

			versionService.updateToCurrentVersion();

			LOG.info("MetaData upgrade done.");
			return true;
		}
		else
		{
			LOG.debug("MetaData version:{}, current version:{} upgrade not needed",
					versionService.getMolgenisVersionFromServerProperties(), MolgenisVersionService.CURRENT_VERSION);
			return false;
		}
	}

	private void runUpgrade(MolgenisUpgrade upgrade)
	{
		LOG.info("Upgrading from {} to {}...", upgrade.getFromVersion(), upgrade.getToVersion());
		upgrade.upgrade();
		LOG.debug("Upgraded from {} to {}.", upgrade.getFromVersion(), upgrade.getToVersion());
		versionService.updateToVersion(upgrade.getToVersion());
	}
}
