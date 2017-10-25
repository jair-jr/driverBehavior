package br.com.j2.apm.function;

/**
 * Represents a Function that receives an input (I), processes it, and returns an output (O).
 */
public interface Function<I, O> {

    public O apply(I input);

    public void clean();
}
