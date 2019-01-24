package org.molgenis.data.plugin.model;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.exception.PermissionDeniedException;

/** Thrown when user has insufficient PluginPermission. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class PluginPermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DP01";

  private final String pluginId;
  private final PluginPermission pluginPermission;

  public PluginPermissionDeniedException(String pluginId, PluginPermission pluginPermission) {
    super(ERROR_CODE);
    this.pluginId = requireNonNull(pluginId);
    this.pluginPermission = requireNonNull(pluginPermission);
  }

  public PluginPermissionDeniedException(
      String pluginId, PluginPermission pluginPermission, Throwable cause) {
    super(ERROR_CODE, cause);
    this.pluginId = requireNonNull(pluginId);
    this.pluginPermission = requireNonNull(pluginPermission);
  }

  @Override
  public String getMessage() {
    return format("pluginPermission: %s, pluginId:%s", pluginPermission, pluginId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {pluginPermission.getName(), pluginId};
  }
}
