package org.molgenis.data.matrix;

import org.molgenis.data.matrix.model.Score;

import java.util.List;

public interface MatrixService
{
	Object getValueByIndex(String entityName, int row, int column);

	List<Score> getValueByNames(String entityName, String row, String column);
}
