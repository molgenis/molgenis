package org.molgenis.data.matrix.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Score.class)
public abstract class Score
{
	public abstract String getColumn();

	public abstract String getRow();

	public abstract Object getValue();

	public static Score createScore(String column, String row, Object value)
	{
		return new AutoValue_Score(column, row, value);
	}
}