package br.com.j2.apm;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Created by jair on 28/12/15.
 */
public class IOUtil {
    public static final String LINE_SEPARATOR = "\n";

    private IOUtil(){

    }

    public static Writer createWriter(@NonNull final File f) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
    }

    public static void closeQuietly(final Closeable c){
        if(c == null){
            return;
        }

        try{
            c.close();
        }
        catch (IOException e) {
            Log.w("fechar_recurso", e);
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static void ensureMkDirs(File dir){
        if(dir.isDirectory()){
            return;
        }
        dir.mkdirs();
        if(!dir.isDirectory()){
            throw new APMException("Impossível criar diretório: " + dir);
        }
    }
}
