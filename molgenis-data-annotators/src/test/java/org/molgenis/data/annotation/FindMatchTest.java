package org.molgenis.data.annotation;

import static org.testng.Assert.assertTrue;
import org.molgenis.data.annotation.FindMatch;
import org.testng.annotations.Test;

import java.util.Arrays;

public class FindMatchTest
{
    String ref1 = "TTCCTCC";
    String alt1 = "TTCC";

    @Test
    public void test0()
    {
        assertTrue(FindMatch.match(ref1,"TTCCTCC",alt1, Arrays.asList("ATG","GTG","TTCC","TTCCCC")));
    }

    @Test
    public void test1()
    {
        assertTrue(FindMatch.match(ref1,"TTCCTCCTCC",alt1, Arrays.asList("TTGGTCC","TTCCTCC")));
    }

    @Test
    public void test1b()
    {
        assertTrue(FindMatch.match("TTCCTCCTCC",ref1, "TTCCTCC", Arrays.asList("TTGGT","TTCC")));
    }

    @Test
    public void test3()
    {
        assertTrue(FindMatch.match("GA","GAA","G", Arrays.asList("GA","G")));
    }

    @Test
    public void test3b()
    {
        assertTrue(FindMatch.match("GAA","GA","GA", Arrays.asList("GC","G")));
    }
}
