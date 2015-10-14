package org.molgenis.migrate.version.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Step16RuntimePropertyToSettings extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step16RuntimePropertyToSettings.class);

	private final RuntimePropertyToAppSettingsMigrator runtimePropertyToAppSettingsMigrator;
	private final RuntimePropertyToGenomicDataSettingsMigrator runtimePropertyToGenomicDataSettingsMigrator;
	private final RuntimePropertyToDataExplorerSettingsMigrator runtimePropertyToDataExplorerSettingsMigrator;
	private final RuntimePropertyToStaticContentMigrator runtimePropertyToStaticContentMigrator;

	public Step16RuntimePropertyToSettings(RuntimePropertyToAppSettingsMigrator runtimePropertyToAppSettingsMigrator,
			RuntimePropertyToGenomicDataSettingsMigrator runtimePropertyToGenomicDataSettingsMigrator,
			RuntimePropertyToDataExplorerSettingsMigrator runtimePropertyToDataExplorerSettingsMigrator,
			RuntimePropertyToStaticContentMigrator runtimePropertyToStaticContentMigrator)
	{
		super(15, 16);
		this.runtimePropertyToAppSettingsMigrator = checkNotNull(runtimePropertyToAppSettingsMigrator);
		this.runtimePropertyToGenomicDataSettingsMigrator = checkNotNull(runtimePropertyToGenomicDataSettingsMigrator);
		this.runtimePropertyToDataExplorerSettingsMigrator = checkNotNull(
				runtimePropertyToDataExplorerSettingsMigrator);
		this.runtimePropertyToStaticContentMigrator = checkNotNull(runtimePropertyToStaticContentMigrator);
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 15 to 16");
		runtimePropertyToAppSettingsMigrator.enableMigrator();
		runtimePropertyToGenomicDataSettingsMigrator.enableMigrator();
		runtimePropertyToDataExplorerSettingsMigrator.enableMigrator();
		runtimePropertyToStaticContentMigrator.enableMigrator();
	}
}
