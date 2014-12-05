package org.molgenis.diseasematcher.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class PhenotipsService
{
	public static final String FORMAT = "html";
	public static final int LIMIT = 500;
	public static final int Q = 1;
	public static final String URL = "http://playground.phenotips.org/bin/get/PhenoTips/OmimPredictService";

	protected String buildQueryURIString(List<String> hpoTerms)
	{
		System.out.println(hpoTerms.toString());
		StringBuilder sb = new StringBuilder();

		sb.append(URL);
		sb.append("?q=" + Integer.toString(Q));
		sb.append("&format=" + FORMAT);
		sb.append("&limit=" + LIMIT);

		for (String term : hpoTerms)
		{
			sb.append("&symptom=" + term);
		}
		return sb.toString();
	}

	public void getRanking(List<String> hpoTerms, ServletOutputStream out)
	{
		URL phenotipsRequest;
		try
		{
			phenotipsRequest = new URL(buildQueryURIString(hpoTerms));

			OutputStream testOut = new ByteArrayOutputStream();
			FileCopyUtils.copy(phenotipsRequest.openStream(), testOut);

			FileCopyUtils.copy(phenotipsRequest.openStream(), out);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
