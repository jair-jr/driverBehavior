package br.com.j2.apm.function;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import br.com.j2.apm.APMException;
import br.com.j2.apm.IOUtil;
import br.com.j2.apm.event.DataCollectionEvent;

/**
 * Created by pma029 on 04/05/16.
 */
public class DataCollectionEventWriterFunction<T extends DataCollectionEvent> implements Function<T, Void>{
    private static final int STRING_BUILDER_CAPACITY = 120;

    private File file;
    private DateFormat csvDateFormat;

    private Writer writer;

    private StringBuilder stringBuilder;

    public DataCollectionEventWriterFunction(File file, String csvDateFormatPattern){
        if(file == null){
            throw new APMException("file is null");
        }
        if(csvDateFormatPattern == null){
            throw new APMException("csvDateFormatPattern is null");
        }

        this.file = file;
        this.csvDateFormat = new SimpleDateFormat(csvDateFormatPattern);
        this.stringBuilder = new StringBuilder(STRING_BUILDER_CAPACITY);
    }

    public File getFile() {
        return file;
    }

    @Override
    public Void apply(T dataCollectionEvent) throws APMException {
        try {
            if (writer == null) {
                if(file.exists()){
                    throw new APMException("Arquivo já existe: " + file.getAbsolutePath());
                }
                writer = IOUtil.createWriter(file);
                writer.append(dataCollectionEvent.getCSVHeader()).append(IOUtil.LINE_SEPARATOR);
            }

            stringBuilder.setLength(0);
            dataCollectionEvent.toCSV(csvDateFormat, stringBuilder);
            stringBuilder.append(IOUtil.LINE_SEPARATOR);
            writer.write(stringBuilder.toString());

            return null;
        }
        catch(IOException e){
            throw new APMException("Erro ao escrever arquivo '" + file.getAbsolutePath() + "'; evento: " + dataCollectionEvent, e);
        }
    }

    @Override
    public void clean() {
        IOUtil.closeQuietly(writer);
    }
}
