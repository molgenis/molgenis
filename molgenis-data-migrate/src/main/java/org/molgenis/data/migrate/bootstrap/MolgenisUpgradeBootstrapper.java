package org.molgenis.data.migrate.bootstrap;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.data.migrate.framework.MolgenisUpgradeService;
import org.springframework.stereotype.Component;

/** Registers and executes {@link MolgenisUpgrade upgrades} during application bootstrapping. */
@Component
public class MolgenisUpgradeBootstrapper {
  private final MolgenisUpgradeService upgradeService;

  public MolgenisUpgradeBootstrapper(MolgenisUpgradeService upgradeService) {
    this.upgradeService = requireNonNull(upgradeService);
  }

  public void bootstrap() {
    // add upgrade steps here
    // upgradeService.addUpgrade(new Step1Xxx(...));
    // upgradeService.addUpgrade(new Step2Yyy(...));
    // upgradeService.addUpgrade(new Step3Zzz(...));

    upgradeService.upgrade();
  }
}
