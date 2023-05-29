package hu.kits.tennis.infrastructure.web.api;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.common.UseCaseFileParser;
import hu.kits.tennis.common.UseCaseFileParser.TestCall;
import io.javalin.http.Context;

class ApiDocHandler {

    void createTestCasesList(Context context) {
        
        File testCasesDir = Paths.get("test/test-cases").toFile();
        if(!testCasesDir.exists()) {
            throw new KITSException("No test cases file found at " + testCasesDir.getAbsolutePath());
        }
        List<String> testCases = Stream.of(testCasesDir.list()).sorted().collect(toList());
        context.render("test-case-list.mustache", Map.of("testCases", testCases));
    }
    
    void createTestCaseDoc(Context context) {
        
        File testCasesDir = Paths.get("test/test-cases").toFile();
        String testCaseFileName = context.pathParam("testCase");
        
        Optional<File> testCaseFile = Stream.of(testCasesDir.listFiles())
            .filter(file -> file.getName().equals(testCaseFileName))
            .findAny();
        
        if(testCaseFile.isPresent()) {
            File file = testCaseFile.get();
            try {
                List<TestCall> useCaseCalls = UseCaseFileParser.parseUseCaseFile(file, true);
                String testCase = formatTestCaseName(file.getName());
                context.render("test-case.mustache", Map.of("testCase", testCase, "useCaseCalls", useCaseCalls));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            context.redirect("/api/docs");
        }
        
    }
    
    private static String formatTestCaseName(String testCaseFileName) {
        String name = testCaseFileName.split("\\.")[0];
        name = name.substring(name.indexOf("_") + 1);
        return StringUtil.capitalize(name);
    }
    
}
