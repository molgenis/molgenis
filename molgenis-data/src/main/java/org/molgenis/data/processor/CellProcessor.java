package org.molgenis.data.processor;

import java.io.Serializable;

public interface CellProcessor extends Serializable
{
	public String process(String value);

	public boolean processHeader();

	public boolean processData();
}
