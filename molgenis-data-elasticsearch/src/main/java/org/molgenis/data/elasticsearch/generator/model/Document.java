package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;
import org.elasticsearch.common.xcontent.XContentBuilder;

import javax.annotation.Nullable;

@AutoValue
public abstract class Document
{
	public abstract String getId();

	@Nullable
	public abstract XContentBuilder getContent();

	public static Document create(String newId, XContentBuilder newContent)
	{
		return builder().setId(newId).setContent(newContent).build();
	}

	public static Builder builder()
	{
		return new AutoValue_Document.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setId(String newId);

		public abstract Builder setContent(XContentBuilder newContent);

		public abstract Document build();
	}
}
