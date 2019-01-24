package org.molgenis.jobs.model;

import static org.molgenis.jobs.model.ScheduledJobMetadata.ACTIVE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.CRON_EXPRESSION;
import static org.molgenis.jobs.model.ScheduledJobMetadata.DESCRIPTION;
import static org.molgenis.jobs.model.ScheduledJobMetadata.FAILURE_EMAIL;
import static org.molgenis.jobs.model.ScheduledJobMetadata.ID;
import static org.molgenis.jobs.model.ScheduledJobMetadata.NAME;
import static org.molgenis.jobs.model.ScheduledJobMetadata.PARAMETERS;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SUCCESS_EMAIL;
import static org.molgenis.jobs.model.ScheduledJobMetadata.TYPE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.USER;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class ScheduledJob extends StaticEntity {
  public ScheduledJob(Entity entity) {
    super(entity);
  }

  public ScheduledJob(EntityType entityType) {
    super(entityType);
  }

  public ScheduledJob(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String identifier) {
    set(ID, identifier);
  }

  public String getId() {
    return getString(ID);
  }

  public String getName() {
    return getString(NAME);
  }

  @Nullable
  @CheckForNull
  public String getDescription() {
    return getString(DESCRIPTION);
  }

  public String getCronExpression() {
    return getString(CRON_EXPRESSION);
  }

  public boolean isActive() {
    Boolean active = getBoolean(ACTIVE);
    return active != null && active;
  }

  @Nullable
  @CheckForNull
  public String getFailureEmail() {
    return getString(FAILURE_EMAIL);
  }

  @Nullable
  @CheckForNull
  public String getSuccessEmail() {
    return getString(SUCCESS_EMAIL);
  }

  @Nullable
  @CheckForNull
  public String getUser() {
    return getString(USER);
  }

  public void setUser(@Nullable @CheckForNull String username) {
    set(USER, username);
  }

  public String getParameters() {
    return getString(PARAMETERS);
  }

  public ScheduledJobType getType() {
    return getEntity(TYPE, ScheduledJobType.class);
  }

  public void setType(ScheduledJobType type) {
    set(TYPE, type);
  }
}
