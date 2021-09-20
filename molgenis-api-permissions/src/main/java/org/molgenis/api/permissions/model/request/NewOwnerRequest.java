package org.molgenis.api.permissions.model.request;

public interface NewOwnerRequest {
  String getOwnedByUser();

  String getOwnedByRole();
}
