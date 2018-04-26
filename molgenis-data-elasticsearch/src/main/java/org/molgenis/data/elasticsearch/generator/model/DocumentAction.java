package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DocumentAction
{
	public enum Operation
	{
		INDEX, DELETE
	}

	public abstract Index getIndex();

	public abstract Document getDocument();

	public abstract Operation getOperation();

	public static DocumentAction create(Index newIndex, Document newDocument, Operation newOperation)
	{
		return builder().setIndex(newIndex).setDocument(newDocument).setOperation(newOperation).build();
	}

	public static Builder builder()
	{
		return new AutoValue_DocumentAction.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setIndex(Index newIndex);

		public abstract Builder setDocument(Document newDocument);

		public abstract Builder setOperation(Operation newOperation);

		public abstract DocumentAction build();
	}
}
