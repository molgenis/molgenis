package org.molgenis.data.processor;

import java.io.Serializable;

public interface CellProcessor extends Serializable
{
	String process(String value);

	boolean processHeader();

	boolean processData();
}
