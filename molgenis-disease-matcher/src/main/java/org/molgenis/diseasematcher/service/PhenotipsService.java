package org.molgenis.diseasematcher.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Lists;

@Service
public class PhenotipsService
{
	public static final String FORMAT = "html";
	public static final int LIMIT = 1000;
	public static final int Q = 1;
	public static final String URL = "http://playground.phenotips.org/bin/get/PhenoTips/OmimPredictService";

	public static void main(String[] args)
	{
		List<String> hpoTerms = Lists.newArrayList();
		hpoTerms.add("HPO:00038313");
		hpoTerms.add("HPO:00018234");
		hpoTerms.add("HPO:00111338");

		OutputStream out = new ByteArrayOutputStream();

		PhenotipsService ps = new PhenotipsService();
		URL phenotipsRequest;
		try
		{
			// phenotipsRequest = new URL(ps.buildQueryURIString(hpoTerms));
			phenotipsRequest = new URL(
					"http://playground.phenotips.org/bin/get/PhenoTips/OmimPredictService?q=1&format=html&limit=30&symptom=HP%3A0009914&symptom=HP%3A0004325");
			FileCopyUtils.copy(phenotipsRequest.openStream(), out);

			out.toString();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
			System.out.println(phenotipsRequest.toString());

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
