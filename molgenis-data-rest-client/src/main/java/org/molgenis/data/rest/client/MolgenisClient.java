package org.molgenis.data.rest.client;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

import java.util.Map;

import org.molgenis.data.rest.client.bean.LoginRequest;
import org.molgenis.data.rest.client.bean.LoginResponse;
import org.molgenis.data.rest.client.bean.MetaDataRequest;
import org.molgenis.data.rest.client.bean.MetaDataResponse;
import org.molgenis.data.rest.client.bean.QueryResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableMap;

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
	 * @param token
	 *            token to put in the molgenis header
	 * @param body
	 *            body to put in the {@link HttpEntity}
	 * @return the {@link HttpEntity}
	 * @param T
	 *            The type of the body
	 */
	private static <T> HttpEntity<T> createHttpEntity(String token, T body)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.set("x-molgenis-token", token);
		return new HttpEntity<T>(body, headers);
	}

	/**
	 * Logs in the user
	 * 
	 * @param uid
	 *            id of the user
	 * @param pwd
	 *            password of the user
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

	/**
	 * Retrieves an entity's metadata
	 * 
	 * @param entityName
	 *            fully qualified name of the entity
	 * @param token
	 *            molgenis session token
	 * @return {@link ResponseEntity} with the {@link MetaDataResponse}
	 */
	public ResponseEntity<MetaDataResponse> getMeta(String token, String entityName)
	{
		return template.exchange("{apiHref}/{entityName}/meta?_method=GET", POST,
				createHttpEntity(token, MetaDataRequest.create()), MetaDataResponse.class, apiHref, entityName);
	}

	/**
	 * Queries an attribute for entities which have one specific attribute set to a specific value.
	 * 
	 * @param entityName
	 *            name of the entity
	 * @param attributeName
	 *            name of the attribute
	 * @param value
	 *            value of the attribute
	 * @param token
	 *            molgenis session token
	 * @return {@link ResponseEntity} with a {@link QueryResponse} body containing the entities which have attributeName
	 *         set to value
	 */
	public QueryResponse queryEquals(String token, String entityName, String attributeName, Object value)
	{
		Map<String, Object> query = of("q",
				singletonList(of("field", attributeName, "operator", "EQUALS", "value", value)));
		ResponseEntity<QueryResponse> response = template.exchange("{apiHref}/{entityName}?_method=GET", POST,
				createHttpEntity(token, query), QueryResponse.class, apiHref, entityName);
		return response.getBody();
	}

	/**
	 * Updates an entity.
	 * 
	 * @param token
	 *            molgenis session token
	 * @param entityName
	 *            name of the entity
	 * @param id
	 *            id of the entity
	 * @param newEntity
	 *            new values for the entity
	 */
	public void update(String token, String entityName, String id, ImmutableMap<String, Object> newEntity)
	{
		template.exchange("{apiHref}/{entityName}/{id}", PUT, createHttpEntity(token, newEntity), Object.class,
				apiHref, entityName, id);
	}

	/**
	 * Updates an entity's attribute value.
	 * 
	 * @param token
	 *            molgenis session token
	 * @param entityName
	 *            name of the entity
	 * @param id
	 *            id of the entity
	 * @param attributeName
	 *            name of the attribute to update
	 * @param value
	 *            new value for the attribute
	 */
	public void update(String token, String entityName, String id, String attributeName, Object value)
	{
		template.exchange("{apiHref}/{entityName}/{id}/{attributeName}", PUT, createHttpEntity(token, value),
				Object.class, apiHref, entityName, id, attributeName);
	}

	/**
	 * Deletes an entity's metadata including all of its rows.
	 * 
	 * @param entityName
	 *            name of the entity
	 */
	public void deleteMetadata(String entityName)
	{
		template.delete("{apiHref}/{entityName}/meta", apiHref, entityName);
	}
}
