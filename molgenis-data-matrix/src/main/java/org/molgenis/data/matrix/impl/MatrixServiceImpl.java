package org.molgenis.data.matrix.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.matrix.MatrixMapper;
import org.molgenis.data.matrix.MatrixService;
import org.molgenis.data.matrix.meta.MatrixMetadata;
import org.molgenis.data.matrix.model.Score;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Service
@RequestMapping("/api/matrix")
public class MatrixServiceImpl implements MatrixService {
    private DataService dataService;
    private FileStore fileStore;

    private Map<String, DoubleMatrix> matrixMap = new HashMap<>();
    private Map<String, MatrixMapper> columnMappingMap = new HashMap<>();
    private Map<String, MatrixMapper> rowMappingMap = new HashMap<>();

    @Autowired
    public MatrixServiceImpl(DataService dataService, FileStore fileStore) {
        this.dataService = dataService;
        this.fileStore = fileStore;
    }

    private DoubleMatrix getMatrixByEntityTypeId(String entityId) {
        DoubleMatrix matrix = null;
        Entity entity = dataService.findOneById(MatrixMetadata.PACKAGE + "_" + MatrixMetadata.SIMPLE_NAME, entityId);
        if (entity != null) {
            return getMatrix(entityId, entity);
        } else {
            throw new MolgenisDataException("Unknown Matrix Metadata EntityID [" + entityId + "]");
        }
    }

    private DoubleMatrix getMatrix(String entityId, Entity entity) {
        DoubleMatrix matrix;
        if (matrixMap.get(entityId) == null) {
            String fileLocation = entity.getString(MatrixMetadata.FILE_LOCATION);
            char seperator = MatrixMetadata.getSeparatorValue(entity.getString(MatrixMetadata.SEPERATOR));
            matrix = new DoubleMatrix(new File(fileLocation), seperator);
            matrixMap.put(entityId, matrix);
        } else {
            matrix = matrixMap.get(entityId);
        }
        getMappers(entityId, entity);
        return matrix;
    }

    private void getMappers(String entityId, Entity entity) {
        if (entity.getEntity(MatrixMetadata.COLUMNMAPPINGFILE) != null && columnMappingMap.get(entityId) == null) {
            FileMeta meta = entity.getEntity(MatrixMetadata.COLUMNMAPPINGFILE, FileMeta.class);
            createMapper(entityId, meta, columnMappingMap);
        }
        if (entity.getEntity(MatrixMetadata.ROWMAPPINGFILE) != null && rowMappingMap.get(entityId) == null) {
            FileMeta meta = entity.getEntity(MatrixMetadata.ROWMAPPINGFILE, FileMeta.class);
            createMapper(entityId, meta, rowMappingMap);
        }
    }

    private void createMapper(String entityId, FileMeta meta, Map<String, MatrixMapper> mapperMap) {
        File file = fileStore.getFile(meta.getId());
        MatrixMapper mapper = new MatrixMapperImpl(file);
        mapperMap.put(entityId, mapper);
    }

    @Override
    @RequestMapping(value = "{entityId}/valueByIndex", method = GET)
    @ResponseBody
    public Object getValueByIndex(@PathVariable("entityId") String entityName, @RequestParam("row") int row, @RequestParam("column") int column) throws MolgenisDataException {
        DoubleMatrix matrix = getMatrixByEntityTypeId(entityName);
        return matrix.getValueByIndex(row, column);
    }

    @Override
    @RequestMapping(value = "{entityId}/valueByNames", method = GET)
    @ResponseBody
    public List<Score> getValueByNames(@PathVariable("entityId") String entityName, @RequestParam("rows") String rows, @RequestParam("columns") String columns) throws MolgenisDataException {
        List<Score> results = new ArrayList<>();
        DoubleMatrix matrix = getMatrixByEntityTypeId(entityName);

        for (String row : rows.split(",")) {
            MatrixMapper rowMapper = rowMappingMap.get(entityName);
            if (rowMapper != null) row = rowMapper.map(row);
            for (String column : columns.split(",")) {
                MatrixMapper columnMapper = columnMappingMap.get(entityName);
                if (columnMapper != null) column = columnMapper.map(column);
                results.add(new Score(row, column, matrix.getValueByName(row, column)));
            }
        }
        return results;
    }
}
