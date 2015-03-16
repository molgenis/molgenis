package org.molgenis.data.version;

import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;

public abstract class MetaDataUpgrade
{
	private final int fromVersion;
	private final int toVersion;
	protected DataService dataService;

	public MetaDataUpgrade(int fromVersion, int toVersion, DataService dataService)
	{
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.dataService = dataService;
	}

	public int getFromVersion()
	{
		return fromVersion;
	}

	public int getToVersion()
	{
		return toVersion;
	}

	public abstract void upgrade(ManageableRepositoryCollection defaultBackend);

}
