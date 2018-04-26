package org.molgenis.data.rest.client;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.rest.client.bean.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * Java client for the REST api.
 */
public class MolgenisClient
{
	private final RestTemplate template;
	private final String apiHref;

	public MolgenisClient(RestTemplate template, String apiHref)
	{
		this.template = template;
		this.apiHref = apiHref;
	}

	/**
	 * Creates a HTTP entity with the given body and a molgenis header
	 *
	 * @param token token to put in the molgenis header
	 * @param body  body to put in the {@link HttpEntity}
	 * @return the {@link HttpEntity}
	 */
	private static <T> HttpEntity<T> createHttpEntity(String token, T body)
	{
		HttpHeaders headers = new HttpHeaders();
		if (token != null)
		{
			headers.set("x-molgenis-token", token);
		}
		return new HttpEntity<>(body, headers);
	}

	/**
	 * Creates a HTTP entity with an empty body and a molgenis header
	 *
	 * @param token token to put in the molgenis header
	 * @return the {@link HttpEntity}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static HttpEntity createHttpEntity(String token)
	{
		HttpHeaders headers = new HttpHeaders();
		if (token != null)
		{
			headers.set("x-molgenis-token", token);
		}
		return new HttpEntity(headers);
	}

	/**
	 * Logs in the user
	 *
	 * @param uid id of the user
	 * @param pwd password of the user
	 * @return molgenis session token
	 */
	public LoginResponse login(String uid, String pwd)
	{
		ResponseEntity<LoginResponse> result = template.postForEntity("{apiHref}/login", LoginRequest.create(uid, pwd),
				LoginResponse.class, apiHref);
		if (result.getStatusCode() == OK)
		{
			return result.getBody();
		}
		throw new RestClientException("Not authenticated");
	}

	public void logout(String token)
	{
		template.exchange("{apiHref}/logout", POST, createHttpEntity(token), Map.class, apiHref);
	}

	/**
	 * Retrieves an entity's metadata
	 *
	 * @param entityTypeId fully qualified name of the entity
	 * @param token        molgenis session token
	 * @return {@link ResponseEntity} with the {@link MetaDataResponse}
	 */
	public ResponseEntity<MetaDataResponse> getMeta(String token, String entityTypeId)
	{
		return template.exchange("{apiHref}/{entityTypeId}/meta?_method=GET", POST,
				createHttpEntity(token, MetaDataRequest.create()), MetaDataResponse.class, apiHref, entityTypeId);
	}

	/**
	 * Queries an attribute for entities which have one specific attribute set to a specific value.
	 *
	 * @param entityTypeId  name of the entity
	 * @param attributeName name of the attribute
	 * @param value         value of the attribute
	 * @param token         molgenis session token
	 * @return {@link ResponseEntity} with a {@link QueryResponse} body containing the entities which have attributeName
	 * set to value
	 */
	public QueryResponse queryEquals(String token, String entityTypeId, String attributeName, Object value)
	{
		Map<String, Object> query = of("q",
				singletonList(of("field", attributeName, "operator", "EQUALS", "value", value)));
		ResponseEntity<QueryResponse> response = template.exchange("{apiHref}/{entityTypeId}?_method=GET", POST,
				createHttpEntity(token, query), QueryResponse.class, apiHref, entityTypeId);
		return response.getBody();
	}

	/**
	 * Updates an entity.
	 *
	 * @param token        molgenis session token
	 * @param entityTypeId name of the entity
	 * @param id           id of the entity
	 * @param newEntity    new values for the entity
	 */
	public void update(String token, String entityTypeId, String id, ImmutableMap<String, Object> newEntity)
	{
		template.exchange("{apiHref}/{entityTypeId}/{id}", PUT, createHttpEntity(token, newEntity), Object.class,
				apiHref, entityTypeId, id);
	}

	/**
	 * Updates an entity's attribute value.
	 *
	 * @param token         molgenis session token
	 * @param entityTypeId  name of the entity
	 * @param id            id of the entity
	 * @param attributeName name of the attribute to update
	 * @param value         new value for the attribute
	 */
	public void update(String token, String entityTypeId, String id, String attributeName, Object value)
	{
		template.exchange("{apiHref}/{entityTypeId}/{id}/{attributeName}", PUT, createHttpEntity(token, value),
				Object.class, apiHref, entityTypeId, id, attributeName);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> get(String token, String entityTypeId, Object id)
	{
		ResponseEntity<Map> responseEntity = template.exchange("{apiHref}/{entityTypeId}/{id}", GET,
				createHttpEntity(token), Map.class, apiHref, entityTypeId, id);
		return responseEntity.getBody();
	}

	public QueryResponse get(String token, String entityTypeId)
	{
		ResponseEntity<QueryResponse> responseEntity = template.exchange("{apiHref}/{entityTypeId}", GET,
				createHttpEntity(token), QueryResponse.class, apiHref, entityTypeId);
		return responseEntity.getBody();
	}

	public void create(String token, String entityTypeId, Map<String, Object> entity)
	{
		template.exchange("{apiHref}/{entityTypeId}/", POST, createHttpEntity(token, entity), Object.class, apiHref,
				entityTypeId);
	}

	public void delete(String token, String entityTypeId, Object id)
	{
		template.exchange("{apiHref}/{entityTypeId}/{id}", DELETE, createHttpEntity(token), Object.class, apiHref,
				entityTypeId, id);
	}

	/**
	 * Deletes an entity's metadata including all of its rows.
	 *
	 * @param entityTypeId name of the entity
	 */
	public void deleteData(String token, String entityTypeId)
	{
		template.exchange("{apiHref}/{entityTypeId}", DELETE, createHttpEntity(token), Object.class, apiHref,
				entityTypeId);
	}

	/**
	 * Deletes an entity's metadata including all of its rows.
	 *
	 * @param entityTypeId name of the entity
	 */
	public void deleteMetadata(String token, String entityTypeId)
	{
		template.exchange("{apiHref}/{entityTypeId}/meta", DELETE, createHttpEntity(token), Object.class, apiHref,
				entityTypeId);
	}
}
