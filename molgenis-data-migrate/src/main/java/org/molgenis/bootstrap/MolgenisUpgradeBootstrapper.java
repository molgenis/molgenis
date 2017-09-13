package org.molgenis.bootstrap;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.framework.MolgenisUpgradeService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Registers and executes {@link MolgenisUpgrade upgrades} during application bootstrapping.
 */
@Component
public class MolgenisUpgradeBootstrapper
{
	private final MolgenisUpgradeService upgradeService;

	public MolgenisUpgradeBootstrapper(MolgenisUpgradeService upgradeService)
	{
		this.upgradeService = requireNonNull(upgradeService);
	}

	void bootstrap()
	{
		// add upgrade steps here
		// upgradeService.addUpgrade(new Step1Xxx(...));
		// upgradeService.addUpgrade(new Step2Yyy(...));
		// upgradeService.addUpgrade(new Step3Zzz(...));

		upgradeService.upgrade();
	}
}
