package br.com.j2.apm.function;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.j2.apm.APMException;

import static org.mockito.Mockito.*;

/**
 * Created by pma029 on 11/04/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class FunctionUtilTest {

    private void chain(final String f2Return){
        final String f1Input = "a";
        final String inOut = "b";

        final Function<String, String> f1 = mock(Function.class);
        when(f1.apply(f1Input)).thenReturn(inOut);

        final Function<String, String> f2 = mock(Function.class);
        when(f2.apply(inOut)).thenReturn(f2Return);

        final Function<String, String> chained = FunctionUtil.chainWhenNonNull(f1, f2);
        final String result = chained.apply(f1Input);
        verify(f1).apply(f1Input);
        verify(f2).apply(inOut);
        Assert.assertEquals(result, f2Return);
    }

    @Test
    public void chainWhenNoneReturnsNull(){
        chain("c");
    }

    @Test
    public void chainWhenFirstReturnsNull(){
        final String f1Input = "a";

        final Function<String, String> f1 = mock(Function.class);
        when(f1.apply(f1Input)).thenReturn(null);

        final Function<String, String> f2 = mock(Function.class);

        final Function<String, String> chained = FunctionUtil.chainWhenNonNull(f1, f2);
        final String result = chained.apply(f1Input);
        verify(f1).apply(f1Input);
        verify(f2, never()).apply(anyString());
        Assert.assertNull(result);

    }

    @Test
    public void chainWhenSecondReturnsNull(){
        chain(null);
    }

    @Test
    public void cleanExceptionInFirstFunction(){
        final RuntimeException e1 = new APMException("E1");
        final Function<String, String> f1 = mock(Function.class);
        doThrow(e1).when(f1).clean();

        final Function<String, String> f2 = mock(Function.class);

        Function chained = FunctionUtil.chainWhenNonNull(f1, f2);
        try {
            chained.clean();
            Assert.fail("Exception in clean expected");
        }
        catch(RuntimeException e){
            Assert.assertSame(e, e1);
            Assert.assertEquals(e.getSuppressed().length, 0);
        }

        verify(f1).clean();
        verify(f2).clean();
    }

    @Test
    public void cleanExceptionInSecondFunction(){
        final Function<String, String> f1 = mock(Function.class);

        final RuntimeException e2 = new APMException("E2");
        final Function<String, String> f2 = mock(Function.class);
        doThrow(e2).when(f2).clean();

        Function chained = FunctionUtil.chainWhenNonNull(f1, f2);
        try {
            chained.clean();
            Assert.fail("Exception in clean expected");
        }
        catch(RuntimeException e){
            Assert.assertSame(e, e2);
            Assert.assertEquals(e.getSuppressed().length, 0);
        }

        verify(f1).clean();
        verify(f2).clean();
    }

    @Test
    public void cleanExceptionInBothFunctions(){
        final RuntimeException e1 = new APMException("E1");
        final Function<String, String> f1 = mock(Function.class);
        doThrow(e1).when(f1).clean();

        final RuntimeException e2 = new APMException("E2");
        final Function<String, String> f2 = mock(Function.class);
        doThrow(e2).when(f2).clean();

        Function chained = FunctionUtil.chainWhenNonNull(f1, f2);
        try {
            chained.clean();
            Assert.fail("Exception in clean expected");
        }
        catch(RuntimeException e){
            Assert.assertSame(e, e1);
            Assert.assertEquals(e.getSuppressed().length, 1);
            Assert.assertSame(e.getSuppressed()[0], e2);
        }

        verify(f1).clean();
        verify(f2).clean();

    }

}