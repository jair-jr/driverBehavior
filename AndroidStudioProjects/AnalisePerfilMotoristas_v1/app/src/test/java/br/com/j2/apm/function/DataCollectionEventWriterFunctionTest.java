package br.com.j2.apm.function;

import com.google.common.io.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static br.com.j2.apm.IOUtil.*;

import br.com.j2.apm.APMException;
import br.com.j2.apm.event.DataCollectionEvent;

import static br.com.j2.apm.TestUtil.*;

/**
 * Created by pma029 on 11/04/16.
 */
public class DataCollectionEventWriterFunctionTest {

    private static final String SYSTEM_TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String TEST_TEMP_DIR = "APMTest";

    private static int filePrefixCounter = 0;

    private File homeDir;
    private File writerFile;
    private DataCollectionEventWriterFunction<DataCollectionEvent> writerFunction;

    @Before
    public void init() throws IOException {
        homeDir = new File(SYSTEM_TEMP_DIR, TEST_TEMP_DIR);
        ensureMkDirs(homeDir);
        writerFile = new File(homeDir, "filePrefix" + (++filePrefixCounter) + ".csv");
        writerFunction = new DataCollectionEventWriterFunction<>(writerFile, DATE_PATTERN);
    }

    @After
    public void clean() throws IOException {
        deleteRecursively(homeDir);
        Assert.assertFalse(homeDir.exists());
    }

    @Test
    public void oneEventToOneFile() throws ParseException, IOException {
        applyToEvents(
                new DataCollectionEvent[]{
                        createMotionEvent("11/09/2016 20:23:00", 100, 10, 20, 30)
                },

                "timestamp,uptimeNanos,x,y,z" + LINE_SEPARATOR +
                        "11/09/2016 20:23:00,100,10.0,20.0,30.0" + LINE_SEPARATOR
        );
    }

    @Test(expected = APMException.class)
    public void applyWhenFileAlreadyExists() throws ParseException, IOException {
        writerFile.createNewFile();
        writerFunction.apply(createMotionEvent("11/09/2016 20:23:00", 100, 10, 20, 30));
    }

    private static void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    private void applyToEvents(DataCollectionEvent[] events,
                               String expectedFileContents) throws IOException {
        for(DataCollectionEvent e : events){
            writerFunction.apply(e);
        }
        writerFunction.clean();

        assertTempFileContentsMatch(writerFunction.getFile(), expectedFileContents);
    }

    private void assertTempFileContentsMatch(File tempFile, String expectedFileContents) throws IOException {
        Assert.assertTrue(tempFile.exists());
        Assert.assertEquals(tempFile.toString(), expectedFileContents, Files.toString(tempFile, StandardCharsets.UTF_8));
    }

}