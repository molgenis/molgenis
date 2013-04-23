package org.molgenis.compute.db.generator;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: georgebyelas
 * Date: 23/04/2013
 * Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
    public void generateTasks(File parameters)
    {

    }

    public static void main(String[] args)
    {
        new TaskGeneratorDB().generateTasks(new File("/Users/georgebyelas/Development/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC/parameters.csv"));
    }
}
