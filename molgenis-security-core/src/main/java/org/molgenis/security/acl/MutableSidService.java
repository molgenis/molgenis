package org.molgenis.security.acl;

import org.springframework.security.acls.model.Sid;

/** Provides support for deleting {@link Sid}s. */
public interface MutableSidService {

  /**
   * Deletes a SID and associated ACEs.
   *
   * @param sid the security identity
   */
  void deleteSid(Sid sid);
}
