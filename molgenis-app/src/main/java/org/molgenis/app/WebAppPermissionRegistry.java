package org.molgenis.app;

import static org.molgenis.beacon.config.BeaconPackage.PACKAGE_BEACON;
import static org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetadata.FREEMARKER_TEMPLATE;
import static org.molgenis.core.ui.settings.FormSettings.FORM_SETTINGS;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.i18n.model.L10nStringMetadata.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.importer.ImportRunMetadata.IMPORT_RUN;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.plugin.model.PluginIdentity.PLUGIN;
import static org.molgenis.data.security.EntityTypeIdentity.ENTITY_TYPE;
import static org.molgenis.data.security.PackageIdentity.PACKAGE;
import static org.molgenis.data.security.auth.GroupService.AUTHORITY_EDITOR;
import static org.molgenis.data.security.auth.GroupService.AUTHORITY_MANAGER;
import static org.molgenis.data.security.auth.GroupService.AUTHORITY_VIEWER;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorPackage.PACKAGE_NEGOTIATOR;
import static org.molgenis.genomebrowser.meta.GenomeBrowserPackage.PACKAGE_GENOME_BROWSER;
import static org.molgenis.navigator.download.job.ResourceDownloadJobExecutionMetadata.RESOURCE_DOWNLOAD_JOB_EXECUTION;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.ONE_CLICK_IMPORTER;
import static org.molgenis.oneclickimporter.job.OneClickImportJobExecutionMetadata.ONE_CLICK_IMPORT_JOB_EXECUTION;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData.MATCHING_TASK_CONTENT;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData.ONTOLOGY_TERM_HIT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.SidUtils.createAnonymousSid;
import static org.molgenis.security.core.SidUtils.createAuthoritySid;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;
import static org.molgenis.settings.entity.DataExplorerEntitySettings.DATA_EXPLORER_ENTITY_SETTINGS;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.app.controller.HomeController;
import org.molgenis.app.manager.controller.AppController;
import org.molgenis.bootstrap.populate.PermissionRegistry;
import org.molgenis.core.ui.admin.user.UserAccountController;
import org.molgenis.core.ui.controller.FeedbackController;
import org.molgenis.core.ui.controller.RedirectController;
import org.molgenis.core.ui.data.importer.wizard.ImportWizardController;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.dataexplorer.negotiator.NegotiatorController;
import org.molgenis.datarowedit.controller.DataRowEditController;
import org.molgenis.metadata.manager.controller.MetadataManagerController;
import org.molgenis.navigator.NavigatorController;
import org.molgenis.navigator.copy.job.ResourceCopyJobExecutionMetadata;
import org.molgenis.navigator.delete.job.ResourceDeleteJobExecutionMetadata;
import org.molgenis.questionnaires.controller.QuestionnaireController;
import org.molgenis.searchall.controller.SearchAllPluginController;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.securityui.controller.SecurityUiController;
import org.molgenis.util.Pair;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

/** Registry of permissions specific for this web application. */
@Component
public class WebAppPermissionRegistry implements PermissionRegistry {

  private ImmutableMultimap.Builder<ObjectIdentity, Pair<PermissionSet, Sid>> builder =
      new ImmutableMultimap.Builder<>();

  WebAppPermissionRegistry() {
    Sid anonymousRole = createAnonymousSid();
    Sid user = createAuthoritySid(AUTHORITY_USER);
    Sid viewer = createAuthoritySid(AUTHORITY_VIEWER);
    Sid editor = createAuthoritySid(AUTHORITY_EDITOR);
    Sid manager = createAuthoritySid(AUTHORITY_MANAGER);

    register(PLUGIN, HomeController.ID, anonymousRole, READ);
    register(PLUGIN, UserAccountController.ID, user, READ);
    register(PLUGIN, AppController.ID, anonymousRole, READ);
    register(PACKAGE, PACKAGE_BEACON, anonymousRole, READ);
    register(PLUGIN, FeedbackController.ID, user, READ);
    register(PLUGIN, RedirectController.ID, anonymousRole, READ);
    register(PLUGIN, ImportWizardController.ID, editor, READ);
    register(ENTITY_TYPE, FREEMARKER_TEMPLATE, anonymousRole, READ);
    register(ENTITY_TYPE, FREEMARKER_TEMPLATE, editor, WRITE);
    register(PLUGIN, DataExplorerController.ID, viewer, READ);
    register(
        ENTITY_TYPE,
        PACKAGE_SETTINGS + PACKAGE_SEPARATOR + DataExplorerController.ID,
        viewer,
        READ);
    register(PLUGIN, NegotiatorController.ID, viewer, READ);
    register(PACKAGE, PACKAGE_NEGOTIATOR, viewer, READ);
    register(PLUGIN, SearchAllPluginController.ID, viewer, READ);
    register(PLUGIN, DataRowEditController.ID, editor, READ);
    register(PLUGIN, JobsController.ID, editor, READ);
    register(ENTITY_TYPE, IMPORT_RUN, editor, WRITE);
    register(ENTITY_TYPE, MATCHING_TASK_CONTENT, editor, WRITE);
    register(ENTITY_TYPE, ONTOLOGY_TERM_HIT, editor, WRITE);
    register(PACKAGE, PACKAGE_ONTOLOGY, user, READ);
    register(PLUGIN, QuestionnaireController.ID, editor, READ);
    register(PACKAGE, PACKAGE_GENOME_BROWSER, viewer, READ);
    register(ENTITY_TYPE, LANGUAGE, anonymousRole, READ);
    register(ENTITY_TYPE, L10N_STRING, anonymousRole, READ);
    register(PACKAGE, PACKAGE_META, user, READ);
    register(ENTITY_TYPE, DECORATOR_CONFIGURATION, anonymousRole, READ);
    register(PACKAGE, PACKAGE_META, manager, WRITE);
    register(PLUGIN, NavigatorController.ID, viewer, READ);
    register(ENTITY_TYPE, RESOURCE_DOWNLOAD_JOB_EXECUTION, editor, WRITE);
    register(ENTITY_TYPE, FILE_META, editor, WRITE);
    register(PLUGIN, ONE_CLICK_IMPORTER, manager, READ);
    register(ENTITY_TYPE, ONE_CLICK_IMPORT_JOB_EXECUTION, manager, WRITE);
    register(PACKAGE, PACKAGE_SYSTEM, user, READMETA);
    register(PLUGIN, SecurityUiController.ID, manager, READ);
    register(PLUGIN, MetadataManagerController.METADATA_MANAGER, manager, READ);
    register(ENTITY_TYPE, ResourceCopyJobExecutionMetadata.COPY_JOB_EXECUTION, manager, WRITE);
    register(ENTITY_TYPE, ResourceDeleteJobExecutionMetadata.DELETE_JOB_EXECUTION, manager, WRITE);
    // When role_ANONYMOUS role includes role_VIEWER, it skips role_USER.
    // So also add the role_USER permissions to role_VIEWER,
    // except for the account plugin which makes no sense when the user is not
    // authenticated.
    register(PLUGIN, FeedbackController.ID, viewer, READ);
    register(PACKAGE, PACKAGE_ONTOLOGY, viewer, READ);
    register(PACKAGE, PACKAGE_META, viewer, READ);
    register(PACKAGE, PACKAGE_SYSTEM, viewer, READMETA);
    register(ENTITY_TYPE, DATA_EXPLORER_ENTITY_SETTINGS, anonymousRole, READ);
    register(ENTITY_TYPE, FORM_SETTINGS, anonymousRole, READ);
  }

  @Override
  public Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> getPermissions() {
    return builder.build();
  }

  private void register(String objectType, String objectId, Sid sid, PermissionSet permissionSet) {
    builder.put(new ObjectIdentityImpl(objectType, objectId), new Pair<>(permissionSet, sid));
  }
}
