package br.com.j2.apm.function;

/**
 * Created by pma029 on 11/04/16.
 */
public class FunctionUtil {
    private FunctionUtil(){

    }

    public static <I, IO, O> Function chainWhenNonNull(final Function<I, IO> f1, final Function<IO, O> f2){
        return new Function<I, O>() {
            @Override
            public O apply(I input) {
                IO io = f1.apply(input);
                if(io != null){
                    return f2.apply(io);
                }

                return null;
            }

            @Override
            public void clean() {
                RuntimeException toThrow = null;

                try {
                    f1.clean();
                }
                catch(RuntimeException e){
                    toThrow = e;
                }

                try{
                    f2.clean();
                }
                catch(RuntimeException e){
                    if(toThrow == null){
                        toThrow = e;
                    }
                    else{
                        toThrow.addSuppressed(e);
                    }
                }

                if(toThrow != null){
                    throw toThrow;
                }
            }
        };
    }
}
