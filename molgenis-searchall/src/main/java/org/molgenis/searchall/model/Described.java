package org.molgenis.searchall.model;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import javax.annotation.CheckForNull;

public interface Described {
  String getLabel();

  @CheckForNull
  String getDescription();

  default boolean isLabelOrDescriptionMatch(String searchterm) {
    return isLabelMatch(searchterm) || isDescriptionMatch(searchterm);
  }

  default boolean isLabelMatch(String searchterm) {
    return containsIgnoreCase(getLabel(), searchterm);
  }

  default boolean isDescriptionMatch(String searchterm) {
    return containsIgnoreCase(getDescription(), searchterm);
  }
}
