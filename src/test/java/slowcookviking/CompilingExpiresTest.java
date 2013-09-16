package slowcookviking;

import instantviking.ExpiresProcessor;
import org.junit.Test;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class CompilingExpiresTest
{
    private File [] files;

    @Test
    public void compileThings() throws IOException
    {
        files = new File[]
                {
                        new File("src/test/java/slowcookviking/AnExpiredThing.java"),
                        new File("src/test/java/slowcookviking/AThingWithExpiringMethods.java"),
                        new File("src/test/java/slowcookviking/NothingUser.java"),
                        new File("src/test/java/slowcookviking/ThingUser.java")
                };

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null); //srsly?

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        compilerTask.setProcessors(Collections.singleton(new ExpiresProcessor()));
        compilerTask.call();

        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            System.out.format("Error on line %d in %s%n",
                    diagnostic.getLineNumber(),
                    diagnostic.getSource());

        fileManager.close();
    }

}
