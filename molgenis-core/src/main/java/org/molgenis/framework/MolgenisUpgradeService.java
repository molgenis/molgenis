package org.molgenis.framework;

public interface MolgenisUpgradeService
{
	boolean upgrade();

	public void addUpgrade(MolgenisUpgrade upgrade);
}
