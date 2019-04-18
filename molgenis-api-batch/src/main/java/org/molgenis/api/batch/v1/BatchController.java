package org.molgenis.api.batch.v1;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.api.batch.BatchApiNamespace.API_BATCH_ID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.molgenis.api.ApiController;
import org.molgenis.api.batch.BatchApiNamespace;
import org.molgenis.api.batch.v1.Response.Builder;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Api("Batch")
@RestController
@RequestMapping(BatchApiNamespace.API_BATCH_PATH)
public class BatchController extends ApiController {
  private final CloseableHttpClient httpClient;

  BatchController(CloseableHttpClient httpClient) {
    super(API_BATCH_ID, 1);
    this.httpClient = requireNonNull(httpClient);
  }

  @ApiOperation("Batch request")
  @PostMapping
  public BatchResponse handleBatchRequest(
      @RequestBody BatchRequest batchRequest, HttpServletRequest httpServletRequest) {
    List<Response> responseList = new ArrayList<>();
    batchRequest
        .getRequests()
        .forEach(
            request -> {
              Builder builder = Response.builder().setId(request.getId());

              HttpUriRequest httpUriRequest = createHttpUriRequest(request);
              httpUriRequest.setHeader(
                  "x-molgenis-token", httpServletRequest.getHeader("x-molgenis-token"));
              try (CloseableHttpResponse response = httpClient.execute(httpUriRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                builder.setStatus(statusCode);

                Map<String, String> headers = new LinkedHashMap<>();
                stream(response.getAllHeaders())
                    .forEach(header -> headers.put(header.getName(), header.getValue()));

                builder.setHeaders(headers);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                  JsonObject jsonObject =
                      (JsonObject)
                          new JsonParser().parse(new InputStreamReader(entity.getContent()));
                  builder.setBody(jsonObject);
                  responseList.add(builder.build());
                  EntityUtils.consume(entity);
                }
              } catch (ClientProtocolException e) {
                throw new RuntimeException(e);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });

    return BatchResponse.builder().setResponses(responseList).build();
  }

  private HttpUriRequest createHttpUriRequest(Request request) {
    URI uri =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .replacePath(null)
            .replaceQuery(null)
            .uriComponents(ServletUriComponentsBuilder.fromUri(request.getUrl()).build())
            .build()
            .toUri();
    HttpUriRequest httpUriRequest;
    switch (request.getMethod()) {
      case GET:
        httpUriRequest = new HttpGet(uri);
        break;
      case HEAD:
        httpUriRequest = new HttpHead(uri);
        break;
      case POST:
        httpUriRequest = new HttpPost(uri);
        break;
      case PUT:
        httpUriRequest = new HttpPut(uri);
        break;
      case PATCH:
        httpUriRequest = new HttpPatch(uri);
        break;
      case DELETE:
        httpUriRequest = new HttpDelete(uri);
        break;
      case OPTIONS:
        httpUriRequest = new HttpOptions(uri);
        break;
      case TRACE:
        httpUriRequest = new HttpTrace(uri);
        break;
      default:
        throw new UnexpectedEnumException(request.getMethod());
    }

    Map<String, String> headers = request.getHeaders();
    if (headers != null) {
      headers.forEach(httpUriRequest::setHeader);
    }

    return httpUriRequest;
  }
}
