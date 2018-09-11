package org.molgenis.data.security.owned;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.SidUtils.createUserSid;

import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;

/**
 * Decorator that assigns ownership based on the owner attribute.
 *
 * <p>Only newly added entities get their ownership updated. User needs to have permission to update
 * ownership.
 */
public class OwnershipDecorator extends AbstractRepositoryDecorator<Entity> {

  private final MutableAclService mutableAclService;
  private final String ownerAttributeName;

  private static final Logger LOG = LoggerFactory.getLogger(OwnershipDecorator.class);

  OwnershipDecorator(
      Repository<Entity> delegateRepository,
      MutableAclService mutableAclService,
      String ownerAttributeName) {
    super(delegateRepository);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.ownerAttributeName = requireNonNull(ownerAttributeName);
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    List<Entity> batch = entities.collect(toList());
    Integer result = super.add(batch.stream());
    batch.forEach(this::assignToOwner);
    return result;
  }

  @Override
  public void add(Entity entity) {
    super.add(entity);
    assignToOwner(entity);
  }

  private void assignToOwner(Entity entity) {
    EntityIdentity entityIdentity = new EntityIdentity(entity);
    String ownerName = entity.getString(ownerAttributeName);
    LOG.debug("Assigning entity {} to owner {}...", entityIdentity, ownerName);
    Sid ownerSid = createUserSid(ownerName);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(entityIdentity);
    acl.setOwner(ownerSid);
    removeAllEntries(acl);
    acl.insertAce(0, WRITE, ownerSid, true);

    mutableAclService.updateAcl(acl);
    LOG.info("Assigned entity {} to owner {}.", entityIdentity, ownerName);
  }

  private void removeAllEntries(MutableAcl acl) {
    while (!acl.getEntries().isEmpty()) {
      acl.deleteAce(0);
    }
  }
}
