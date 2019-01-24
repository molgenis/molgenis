package org.molgenis.core.ui.controller;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.settings.StaticContentMetadata.STATIC_CONTENT;
import static org.molgenis.data.security.EntityTypePermission.ADD_DATA;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.data.security.EntityTypePermission.UPDATE_DATA;

import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Controller that handles static content pages requests. */
@Service
public class StaticContentServiceImpl implements StaticContentService {
  private static final Logger LOG = LoggerFactory.getLogger(StaticContentServiceImpl.class);

  private final DataService dataService;
  private final StaticContentFactory staticContentFactory;

  private final UserPermissionEvaluator permissionService;

  StaticContentServiceImpl(
      DataService dataService,
      StaticContentFactory staticContentFactory,
      UserPermissionEvaluator permissionService) {
    this.permissionService = requireNonNull(permissionService);
    this.dataService = requireNonNull(dataService);
    this.staticContentFactory = staticContentFactory;
  }

  @Override
  @Transactional
  public boolean submitContent(String key, String content) {
    try {
      StaticContent staticContent =
          dataService.findOneById(STATIC_CONTENT, key, StaticContent.class);
      if (staticContent == null) {
        staticContent = staticContentFactory.create(key);
        staticContent.setContent(content);
        dataService.add(STATIC_CONTENT, staticContent);
      } else {
        staticContent.setContent(content);
        dataService.update(STATIC_CONTENT, staticContent);
      }
      return true;
    } catch (RuntimeException e) {
      LOG.error("", e);
      return false;
    }
  }

  @Override
  public boolean isCurrentUserCanEdit(String pluginId) {
    if (!permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), READ_DATA)) {
      return false;
    }
    StaticContent staticContent =
        dataService.findOneById(STATIC_CONTENT, pluginId, StaticContent.class);
    if (staticContent == null) {
      return permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), ADD_DATA);
    } else {
      return permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), UPDATE_DATA);
    }
  }

  @Override
  public String getContent(String key) {
    StaticContent staticContent =
        RunAsSystemAspect.runAsSystem(
            () -> dataService.findOneById(STATIC_CONTENT, key, StaticContent.class));
    return staticContent != null ? staticContent.getContent() : null;
  }

  public void checkPermissions(String pluginId) {
    StaticContent staticContent =
        dataService.findOneById(STATIC_CONTENT, pluginId, StaticContent.class);
    if (staticContent == null) {
      if (!permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), UPDATE_DATA)) {
        throw new EntityTypePermissionDeniedException(UPDATE_DATA, STATIC_CONTENT);
      }
    } else {
      if (!permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), ADD_DATA)) {
        throw new EntityTypePermissionDeniedException(ADD_DATA, STATIC_CONTENT);
      }
    }
  }
}
