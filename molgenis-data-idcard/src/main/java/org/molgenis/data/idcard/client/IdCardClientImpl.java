package org.molgenis.data.idcard.client;

import com.google.common.primitives.Ints;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.idcard.mapper.IdCardBiobankMapper;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardOrganization;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@Service
public class IdCardClientImpl implements IdCardClient
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardClientImpl.class);

	private final HttpClient httpClient;
	private final IdCardIndexerSettings idCardIndexerSettings;
	private final IdCardBiobankMapper idCardBiobankMapper;

	@Autowired
	public IdCardClientImpl(HttpClient httpClient, IdCardIndexerSettings idCardIndexerSettings,
			IdCardBiobankMapper idCardBiobankMapper)
	{
		this.httpClient = requireNonNull(httpClient);
		this.idCardIndexerSettings = requireNonNull(idCardIndexerSettings);
		this.idCardBiobankMapper = requireNonNull(idCardBiobankMapper);
	}

	@Override
	public Entity getIdCardBiobank(String id)
	{
		return getIdCardBiobank(id, idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Entity getIdCardBiobank(String id, long timeout)
	{
		// Construct uri
		StringBuilder uriBuilder = new StringBuilder().append(idCardIndexerSettings.getApiBaseUri()).append('/')
				.append(idCardIndexerSettings.getBiobankResource()).append('/').append(id);

		return getIdCardResource(uriBuilder.toString(), new JsonResponseHandler<IdCardBiobank>()
		{
			@Override
			public IdCardBiobank deserialize(JsonReader jsonReader) throws IOException
			{
				return idCardBiobankMapper.toIdCardBiobank(jsonReader);
			}
		}, timeout);
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		return getIdCardBiobanks(ids, idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids, long timeout)
	{
		String value = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining(",", "[", "]"));
		try
		{
			value = URLEncoder.encode(value, UTF_8.name());
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new RuntimeException(e1);
		}
		StringBuilder uriBuilder = new StringBuilder().append(idCardIndexerSettings.getApiBaseUri()).append('/')
				.append(idCardIndexerSettings.getBiobankCollectionSelectionResource()).append('/').append(value);

		return getIdCardResource(uriBuilder.toString(), new JsonResponseHandler<Iterable<Entity>>()
		{
			@Override
			public Iterable<Entity> deserialize(JsonReader jsonReader) throws IOException
			{
				return idCardBiobankMapper.toIdCardBiobanks(jsonReader);
			}
		}, timeout);
	}

	private <T> T getIdCardResource(String url, ResponseHandler<T> responseHandler, long timeout)
	{

		HttpGet request = new HttpGet(url);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		if (timeout != -1)
		{
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Ints.checkedCast(timeout))
					.setConnectionRequestTimeout(Ints.checkedCast(timeout)).setSocketTimeout(Ints.checkedCast(timeout))
					.build();
			request.setConfig(requestConfig);
		}
		try
		{
			LOG.info("Retrieving [" + url + "]");
			return httpClient.execute(request, responseHandler);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks()
	{
		return getIdCardBiobanks(idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(long timeout)
	{
		// Construct uri
		StringBuilder uriBuilder = new StringBuilder().append(idCardIndexerSettings.getApiBaseUri()).append('/')
				.append(idCardIndexerSettings.getBiobankCollectionResource());

		// Retrieve biobank ids
		Iterable<IdCardOrganization> idCardOrganizations = getIdCardResource(uriBuilder.toString(),
				new JsonResponseHandler<Iterable<IdCardOrganization>>()
				{
					@Override
					public Iterable<IdCardOrganization> deserialize(JsonReader jsonReader) throws IOException
					{
						return idCardBiobankMapper.toIdCardOrganizations(jsonReader);
					}
				}, timeout);

		// Retrieve biobanks
		return this.getIdCardBiobanks(new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return StreamSupport.stream(idCardOrganizations.spliterator(), false)
						.map(IdCardOrganization::getOrganizationId).iterator();
			}
		}, timeout);
	}

	private static abstract class JsonResponseHandler<T> implements ResponseHandler<T>
	{
		@Override
		public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
		{
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() < 100 || statusLine.getStatusCode() >= 300)
			{
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity == null)
			{
				throw new ClientProtocolException("Response contains no content");
			}

			JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
			try
			{
				return deserialize(jsonReader);
			}
			finally
			{
				jsonReader.close();
			}
		}

		public abstract T deserialize(JsonReader jsonReader) throws IOException;
	}
}