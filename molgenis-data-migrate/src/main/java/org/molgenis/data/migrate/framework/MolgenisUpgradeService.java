package org.molgenis.data.migrate.framework;

public interface MolgenisUpgradeService
{
	boolean upgrade();

	void addUpgrade(MolgenisUpgrade upgrade);
}
