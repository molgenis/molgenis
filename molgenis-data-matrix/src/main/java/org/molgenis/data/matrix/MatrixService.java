package org.molgenis.data.matrix;

import org.molgenis.data.matrix.model.Score;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

public interface MatrixService {
    Object getValueByIndex(String entityName, int row, int column);
    List<Score> getValueByNames(String entityName, String row, String column);
}
