package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates Elasticsearch bulk processors that log bulk updates. */
class BulkProcessorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(BulkProcessorFactory.class);

  BulkProcessor create(RestHighLevelClient client) {
    var listener =
        new BulkProcessor.Listener() {
          @Override
          public void beforeBulk(long executionId, BulkRequest request) {
            LOG.trace(
                "Going to execute new bulk composed of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            if (response.hasFailures() && LOG.isErrorEnabled()) {
              LOG.error("Error executing bulk: {}", response.buildFailureMessage());
            }
            LOG.trace("Executed bulk composed of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            LOG.warn("Error executing bulk", failure);
          }
        };
    return BulkProcessor.builder(
            (request, bulklistener) ->
                client.bulkAsync(request, RequestOptions.DEFAULT, bulklistener),
            listener,
            "bulk-processor")
        .build();
  }
}
