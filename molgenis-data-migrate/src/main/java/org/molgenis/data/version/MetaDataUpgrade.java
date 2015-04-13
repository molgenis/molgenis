package org.molgenis.data.version;

/**
 * Base class for meta data upgrade classes
 */
public abstract class MetaDataUpgrade
{
	private final int fromVersion;
	private final int toVersion;

	public MetaDataUpgrade(int fromVersion, int toVersion)
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
