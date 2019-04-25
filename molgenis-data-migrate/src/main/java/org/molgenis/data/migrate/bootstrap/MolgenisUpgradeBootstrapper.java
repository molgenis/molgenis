package org.molgenis.data.migrate.bootstrap;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.data.migrate.framework.MolgenisUpgradeService;
import org.molgenis.data.migrate.version.Step33UpdateForeignKeyDeferred;
import org.molgenis.data.migrate.version.Step34AddRoleMetrics;
import org.molgenis.data.migrate.version.Step35UpdateAclSystemSid;
import org.molgenis.data.migrate.version.Step36EnableDataRowEdit;
import org.springframework.stereotype.Component;

/** Registers and executes {@link MolgenisUpgrade upgrades} during application bootstrapping. */
@Component
public class MolgenisUpgradeBootstrapper {
  private final MolgenisUpgradeService upgradeService;
  private final DataSource dataSource;

  public MolgenisUpgradeBootstrapper(MolgenisUpgradeService upgradeService, DataSource dataSource) {
    this.upgradeService = requireNonNull(upgradeService);
    this.dataSource = requireNonNull(dataSource);
  }

  public void bootstrap() {
    upgradeService.addUpgrade(new Step33UpdateForeignKeyDeferred(dataSource));
    upgradeService.addUpgrade(new Step34AddRoleMetrics(dataSource));
    upgradeService.addUpgrade(new Step35UpdateAclSystemSid(dataSource));
    upgradeService.addUpgrade(new Step36EnableDataRowEdit(dataSource));

    upgradeService.upgrade();
  }
}
