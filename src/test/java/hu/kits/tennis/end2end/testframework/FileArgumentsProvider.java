package hu.kits.tennis.end2end.testframework;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class FileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<TestCaseDirSource>  {
    
    private String testCaseFolder;
    
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        File testCasesDir = Paths.get(testCaseFolder).toFile();
        return Stream.of(testCasesDir.listFiles()).map(file -> Arguments.of(file));
    }
    
    @Override
    public void accept(TestCaseDirSource fromTestCaseDir) {
        testCaseFolder = fromTestCaseDir.value();
    }
}
