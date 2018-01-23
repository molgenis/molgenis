package org.molgenis.data.migrate.framework;

/**
 * Base class for Molgenis upgrade steps.
 */
public abstract class MolgenisUpgrade
{
	private final int fromVersion;
	private final int toVersion;

	public MolgenisUpgrade(int fromVersion, int toVersion)
	{
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
	}

	public int getFromVersion()
	{
		return fromVersion;
	}

	public int getToVersion()
	{
		return toVersion;
	}

	public abstract void upgrade();

}
