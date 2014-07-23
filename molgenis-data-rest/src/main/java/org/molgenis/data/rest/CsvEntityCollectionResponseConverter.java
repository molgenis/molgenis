package org.molgenis.data.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataConverter;
import org.molgenis.util.BaseHttpMessageConverter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvEntityCollectionResponseConverter extends
		BaseHttpMessageConverter<EntityCollectionResponse> {
	
	public CsvEntityCollectionResponseConverter() {
		super(new MediaType("text", "csv"));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(EntityCollectionResponse.class);
	}

	@Override
	protected EntityCollectionResponse readInternal(
			Class<? extends EntityCollectionResponse> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void writeInternal(EntityCollectionResponse t,
			HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {

		CSVWriter writer = new CSVWriter(new OutputStreamWriter(
				outputMessage.getBody()));
		List<Map<String, Object>> items = t.getItems();
		// TODO: figure out metadata
		writer.writeNext(new String[] {"href", "name" });
		for (Map<String, Object> item : items) {
			List<String> row = new ArrayList<String>();
			serializeMap(item, row);
			writer.writeNext(row.toArray(new String[0]));
		}
		writer.close();
	}

	@SuppressWarnings("unchecked")
	private void serializeMap(Map<String, Object> item, List<String> row) {
		for (Object value : item.values()) {
			if (value instanceof Map<?, ?>) {
				serializeMap((Map<String, Object>) value, row);
			} else {
				row.add(DataConverter.toString(value));
			}
		}
	}

}
