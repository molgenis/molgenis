package org.molgenis.app;

import static org.molgenis.beacon.config.BeaconPackage.PACKAGE_BEACON;
import static org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetaData.FREEMARKER_TEMPLATE;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.importer.ImportRunMetaData.IMPORT_RUN;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.plugin.model.PluginIdentity.PLUGIN;
import static org.molgenis.data.security.EntityTypeIdentity.ENTITY_TYPE;
import static org.molgenis.data.security.PackageIdentity.PACKAGE;
import static org.molgenis.data.security.auth.GroupService.*;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorPackage.PACKAGE_NEGOTIATOR;
import static org.molgenis.genomebrowser.meta.GenomeBrowserPackage.PACKAGE_GENOME_BROWSER;
import static org.molgenis.oneclickimporter.controller.OneClickImporterController.ONE_CLICK_IMPORTER;
import static org.molgenis.oneclickimporter.job.OneClickImportJobExecutionMetadata.ONE_CLICK_IMPORT_JOB_EXECUTION;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData.MATCHING_TASK_CONTENT;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData.ONTOLOGY_TERM_HIT;
import static org.molgenis.security.core.PermissionSet.*;
import static org.molgenis.security.core.SidUtils.createAuthoritySid;
import static org.molgenis.security.core.SidUtils.createUserSid;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;

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
import org.molgenis.navigator.NavigatorController;
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
    Sid anonymousUser = createUserSid(ANONYMOUS_USERNAME);
    Sid user = createAuthoritySid(AUTHORITY_USER);
    Sid viewer = createAuthoritySid(AUTHORITY_VIEWER);
    Sid editor = createAuthoritySid(AUTHORITY_EDITOR);
    Sid manager = createAuthoritySid(AUTHORITY_MANAGER);

    register(PLUGIN, HomeController.ID, anonymousUser, READ);
    register(PLUGIN, HomeController.ID, user, READ);
    register(PLUGIN, UserAccountController.ID, user, READ);
    register(PLUGIN, AppController.ID, anonymousUser, READ);
    register(PLUGIN, AppController.ID, user, READ);
    register(PACKAGE, PACKAGE_BEACON, anonymousUser, READ);
    register(PACKAGE, PACKAGE_BEACON, user, READ);
    register(PLUGIN, FeedbackController.ID, user, READ);
    register(PLUGIN, RedirectController.ID, anonymousUser, READ);
    register(PLUGIN, RedirectController.ID, user, READ);
    register(PLUGIN, ImportWizardController.ID, editor, READ);
    register(ENTITY_TYPE, FREEMARKER_TEMPLATE, anonymousUser, READ);
    register(ENTITY_TYPE, FREEMARKER_TEMPLATE, user, READ);
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
    register(ENTITY_TYPE, LANGUAGE, anonymousUser, READ);
    register(ENTITY_TYPE, LANGUAGE, user, READ);
    register(ENTITY_TYPE, L10N_STRING, anonymousUser, READ);
    register(ENTITY_TYPE, L10N_STRING, user, READ);
    register(PACKAGE, PACKAGE_META, user, READ);
    register(ENTITY_TYPE, DECORATOR_CONFIGURATION, anonymousUser, READ);
    register(ENTITY_TYPE, DECORATOR_CONFIGURATION, user, READ);
    register(PACKAGE, PACKAGE_META, manager, WRITE);
    register(PLUGIN, NavigatorController.ID, viewer, READ);
    register(PLUGIN, ONE_CLICK_IMPORTER, manager, READ);
    register(ENTITY_TYPE, ONE_CLICK_IMPORT_JOB_EXECUTION, manager, WRITE);
    register(PACKAGE, PACKAGE_SYSTEM, user, READMETA);
    register(PLUGIN, SecurityUiController.ID, manager, READ);
  }

  @Override
  public Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> getPermissions() {
    return builder.build();
  }

  private void register(String objectType, String objectId, Sid sid, PermissionSet permissionSet) {
    builder.put(new ObjectIdentityImpl(objectType, objectId), new Pair<>(permissionSet, sid));
  }
}
