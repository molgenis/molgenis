package org.molgenis.data.matrix.impl;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.matrix.MatrixMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MatrixMapperImpl implements MatrixMapper {

    private final File mappingFile;
    private Map<String, String> mapping = new HashMap<>();
    private boolean inited = false;

    public MatrixMapperImpl(File file) {
        this.mappingFile = file;
    }

    private void init() {
        try {
            Scanner mappingScanner = new Scanner(mappingFile, "UTF-8");
            mappingScanner.nextLine();//skip header
            while (mappingScanner.hasNext()) {
                String from = null;
                String to;
                Scanner lineScanner = new Scanner(mappingScanner.nextLine());
                if (lineScanner.hasNext()) {
                    to = lineScanner.next();
                    if (StringUtils.isNotEmpty(to)) {
                        if (lineScanner.hasNext()) {
                            from = lineScanner.next();
                            if (StringUtils.isNotEmpty(from)) mapping.put(from, to);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new MolgenisDataException(e);
        }
        inited = true;
    }

    @Override
    public String map(String input) {
        if (!inited) init();
        String result = mapping.get(input);
        if (result == null)
            throw new MolgenisDataException("the specified value [" + input + "] was not found in the mappingfile");
        return result;
    }
}
