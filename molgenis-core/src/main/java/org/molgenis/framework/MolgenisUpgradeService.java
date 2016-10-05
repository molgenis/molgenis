package org.molgenis.framework;

public interface MolgenisUpgradeService
{
	boolean upgrade();

	void addUpgrade(MolgenisUpgrade upgrade);
}
