package nz.pumbas.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import nz.pumbas.commands.tokens.context.InvocationContext;

public class InvocationContextTests
{

    @Test
    public void getNextSurroundedTest() {
        InvocationContext tokenInfo = InvocationContext.of("[1 2 3] [#Block[1 2 3] #Block[2 3 4]]");

        Optional<String> oArray1 = tokenInfo.getNextSurrounded("[", "]");
        Optional<String> oArray2 = tokenInfo.getNextSurrounded("[", "]");
        Optional<String> oArray3 = tokenInfo.getNextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.isPresent());
        Assertions.assertTrue(oArray2.isPresent());
        Assertions.assertFalse(oArray3.isPresent());

        Assertions.assertEquals("1 2 3", oArray1.get());
        Assertions.assertEquals("#Block[1 2 3] #Block[2 3 4]", oArray2.get());
    }

    @Test
    public void getNextSurroundedIncompleteTest() {
        InvocationContext tokenInfo1 = InvocationContext.of("[1 2 3] [#Block[1 2 3] #Block[2 3 4] ");
        InvocationContext tokenInfo2 = InvocationContext.of("#Block[1 2 3]]");

        Optional<String> oArray1 = tokenInfo1.getNextSurrounded("[", "]");
        Optional<String> oArray2 = tokenInfo1.getNextSurrounded("[", "]");
        Optional<String> oArray3 = tokenInfo2.getNextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.isPresent());
        Assertions.assertFalse(oArray2.isPresent());
        Assertions.assertFalse(oArray3.isPresent());

        Assertions.assertEquals("1 2 3", oArray1.get());
    }

    @Test
    public void getNextSurroundedStepPastTest() {
        InvocationContext tokenInfo = InvocationContext.of("#Block[1 2 3]");

        Optional<String> oType = tokenInfo.getNextSurrounded("#", "[", false);
        Optional<String> oParameters = tokenInfo.getNextSurrounded("[", "]");

        Assertions.assertTrue(oType.isPresent());
        Assertions.assertTrue(oParameters.isPresent());
        Assertions.assertFalse(tokenInfo.hasNext());

        Assertions.assertEquals("Block", oType.get());
        Assertions.assertEquals("1 2 3", oParameters.get());
    }
}
