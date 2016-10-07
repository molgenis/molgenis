package org.molgenis.ontology.ic;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PubMedTermFrequencyService
{
	private static final Logger LOG = LoggerFactory.getLogger(PubMedTermFrequencyService.class);
	private static final String BASE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?term=";
	private static final Pattern PATTERN_REGEX = Pattern.compile("<Count>(\\d*)</Count>");
	private static final int TIME_OUT = 20000;
	private static final long TOTAL_NUMBER_PUBLICATION = 24000000;
	private static final int THRESHOLD_VALUE = 30000;
	private static final PubMedTFEntity DEFAULT_TF_ENTITY = new PubMedTFEntity(THRESHOLD_VALUE, 1);

	public PubMedTFEntity getTermFrequency(String term)
	{
		if (term.length() < 4) return DEFAULT_TF_ENTITY;

		String response = httpGet(BASE_URL + "\"" + term + "\"");
		return parseResponse(response);
	}

	public PubMedTFEntity parseResponse(String response)
	{
		Matcher matcher = PATTERN_REGEX.matcher(response);
		if (matcher.find())
		{
			String countString = matcher.group(1);
			if (StringUtils.isNotEmpty(countString))
			{
				int occurrence = Integer.parseInt(countString);
				if (occurrence >= THRESHOLD_VALUE)
				{
					if (occurrence != 0)
					{
						double frequency = Math
								.pow(Math.log10((double) occurrence / TOTAL_NUMBER_PUBLICATION) * 1.5, 2);
						return new PubMedTFEntity(occurrence, frequency);
					}
				}
				else
				{
					return DEFAULT_TF_ENTITY;
				}
			}
		}
		return null;
	}

	public String httpGet(String targetURL)
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setConnectTimeout(TIME_OUT);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		}
		catch (Exception e)
		{
			if (LOG.isTraceEnabled())
			{
				LOG.error(e.getMessage());
			}
			return StringUtils.EMPTY;
		}
	}
}
