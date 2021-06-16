package org.molgenis.data.migrate.bootstrap;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;
import org.molgenis.data.elasticsearch.client.ClientFacade;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.data.migrate.framework.MolgenisUpgradeService;
import org.molgenis.data.migrate.version.Step33UpdateForeignKeyDeferred;
import org.molgenis.data.migrate.version.Step34AddRoleMetrics;
import org.molgenis.data.migrate.version.Step35UpdateAclSystemSid;
import org.molgenis.data.migrate.version.Step36EnableDataRowEdit;
import org.molgenis.data.migrate.version.Step37AddSettingsPluginToMenu;
import org.molgenis.data.migrate.version.Step38AddAnonymouseRoleToDb;
import org.molgenis.data.migrate.version.Step39CreateRootPackageGroups;
import org.molgenis.data.migrate.version.Step40AddRoleSystem;
import org.molgenis.data.migrate.version.Step41Reindex;
import org.molgenis.data.migrate.version.Step42RemoveFormsUrlRow;
import org.molgenis.data.migrate.version.Step43SetUsernameAttributeName;
import org.molgenis.data.migrate.version.Step44CascadeDeleteSids;
import org.molgenis.data.migrate.version.Step45RemoveDanglingSids;
import org.molgenis.data.migrate.version.Step46DisableInactiveOidcClients;
import org.molgenis.data.migrate.version.Step47AddMaxLength;
import org.molgenis.data.migrate.version.Step48RemoveMagmaPlaceholderLocalization;
import org.springframework.stereotype.Component;

/** Registers and executes {@link MolgenisUpgrade upgrades} during application bootstrapping. */
@Component
public class MolgenisUpgradeBootstrapper {

  private final MolgenisUpgradeService upgradeService;
  private final DataSource dataSource;
  private final ClientFacade clientFacade;

  public MolgenisUpgradeBootstrapper(
      MolgenisUpgradeService upgradeService, DataSource dataSource, ClientFacade clientFacade) {
    this.upgradeService = requireNonNull(upgradeService);
    this.dataSource = requireNonNull(dataSource);
    this.clientFacade = requireNonNull(clientFacade);
  }

  public void bootstrap() {
    upgradeService.addUpgrade(new Step33UpdateForeignKeyDeferred(dataSource));
    upgradeService.addUpgrade(new Step34AddRoleMetrics(dataSource));
    upgradeService.addUpgrade(new Step35UpdateAclSystemSid(dataSource));
    upgradeService.addUpgrade(new Step36EnableDataRowEdit(dataSource));
    upgradeService.addUpgrade(new Step37AddSettingsPluginToMenu(dataSource));
    upgradeService.addUpgrade(new Step38AddAnonymouseRoleToDb(dataSource));
    upgradeService.addUpgrade(new Step39CreateRootPackageGroups(dataSource));
    upgradeService.addUpgrade(new Step40AddRoleSystem(dataSource));
    upgradeService.addUpgrade(new Step41Reindex(clientFacade));
    upgradeService.addUpgrade(new Step42RemoveFormsUrlRow(dataSource));
    upgradeService.addUpgrade(new Step43SetUsernameAttributeName(dataSource));
    upgradeService.addUpgrade(new Step44CascadeDeleteSids(dataSource));
    upgradeService.addUpgrade(new Step45RemoveDanglingSids(dataSource));
    upgradeService.addUpgrade(new Step46DisableInactiveOidcClients(dataSource));
    upgradeService.addUpgrade(new Step47AddMaxLength(dataSource));
    upgradeService.addUpgrade(new Step48RemoveMagmaPlaceholderLocalization(dataSource));
    upgradeService.upgrade();
  }
}
