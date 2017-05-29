package org.molgenis.data.semanticsearch.semantic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Hit<T> implements Comparable<Hit<T>>
{
	public abstract T getResult();

	public abstract int getScoreInt();

	public float getScore()
	{
		return getScoreInt() / 100000.0f;
	}

	public static <T> Hit<T> create(T result, float score)
	{
		return new AutoValue_Hit<T>(result, Math.round(score * 100000));
	}

	@Override
	public int compareTo(Hit<T> o)
	{
		return Float.compare(getScore(), o.getScore());
	}
}