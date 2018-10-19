package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_JobDetail.class)
public abstract class JobDetail {
  public enum Type {
    COPY,
    DOWNLOAD
  }

  public abstract Type getType();

  public abstract String getId();

  public static JobDetail create(Type type, String id) {
    return new AutoValue_JobDetail(type, id);
  }
}
