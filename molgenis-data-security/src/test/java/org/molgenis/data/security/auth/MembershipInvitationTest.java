package org.molgenis.data.security.auth;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.auth.MembershipInvitationMetadata.Status;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class MembershipInvitationTest extends AbstractSystemEntityTest {

  @Autowired MembershipInvitationMetadata metadata;
  @Autowired MembershipInvitationFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> status = new Pair<>();
    status.setA(Status.class);
    status.setB(Status.ACCEPTED);
    map.put(MembershipInvitationMetadata.STATUS, status);

    return map;
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        MembershipInvitation.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
