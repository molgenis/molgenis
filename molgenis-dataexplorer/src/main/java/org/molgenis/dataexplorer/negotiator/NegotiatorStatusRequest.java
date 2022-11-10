package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorStatusRequest.class)
public abstract class NegotiatorStatusRequest {

  /** The endpoint to query for status */
  public abstract String getTestEndpointUrl();

  public static NegotiatorStatusRequest create(String testEndpointUrl) {
    return new AutoValue_NegotiatorStatusRequest(testEndpointUrl);
  }
}
