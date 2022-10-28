package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorStatusRequest.class)
public abstract class NegotiatorStatusRequest {

    /** Entity type ID for the negotiator config */
    public abstract String getNegotiatorConfigId();

    /** The endpoint to query for status */
    public abstract String getStatusEndpoint();

    public static NegotiatorStatusRequest create(
            String negotiatorEntityId,
            String statusEndpointUrl) {
        return new AutoValue_NegotiatorStatusRequest(
                negotiatorEntityId, statusEndpointUrl);
    }
}

