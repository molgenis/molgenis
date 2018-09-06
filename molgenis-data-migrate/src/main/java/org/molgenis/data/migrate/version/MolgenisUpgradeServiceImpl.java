package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.data.migrate.framework.MolgenisUpgradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Upgrades the data backends to the current version.
 *
 * <p>Gets the current version from the {@link MolgenisVersionService}.
 */
@Service
public class MolgenisUpgradeServiceImpl implements MolgenisUpgradeService {
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private final MolgenisVersionService versionService;
  private final List<MolgenisUpgrade> upgrades = new ArrayList<>();

  public MolgenisUpgradeServiceImpl(MolgenisVersionService versionService) {
    this.versionService = requireNonNull(versionService);
  }

  public void addUpgrade(MolgenisUpgrade upgrade) {
    upgrades.add(upgrade);
  }

  /**
   * Executes MOLGENIS MetaData version upgrades.
   *
   * @return true if an upgrade was necessary, false if not
   */
  @Override
  public boolean upgrade() {
    int schemaVersion = versionService.getSchemaVersion();
    if (schemaVersion < 19) {
      throw new UnsupportedOperationException(
          "Upgrading from versions below 1.10 (metadataversion 19) is not supported, please update to 1.10 first.");
    }
    if (schemaVersion < versionService.getAppVersion()) {
      LOG.info(
          "MetaData version:{}, current version:{} upgrade needed",
          schemaVersion,
          versionService.getAppVersion());

      upgrades
          .stream()
          .filter(upgrade -> upgrade.getFromVersion() >= schemaVersion)
          .forEach(this::runUpgrade);

      versionService.setSchemaVersion(versionService.getAppVersion());

      LOG.info("MetaData upgrade done.");
      return true;
    } else {
      LOG.debug(
          "MetaData version:{}, current version:{} upgrade not needed",
          schemaVersion,
          versionService.getAppVersion());
      return false;
    }
  }

  private void runUpgrade(MolgenisUpgrade upgrade) {
    LOG.info("Upgrading from {} to {}...", upgrade.getFromVersion(), upgrade.getToVersion());
    upgrade.upgrade();
    LOG.debug("Upgraded from {} to {}.", upgrade.getFromVersion(), upgrade.getToVersion());
    versionService.setSchemaVersion(upgrade.getToVersion());
  }
}
