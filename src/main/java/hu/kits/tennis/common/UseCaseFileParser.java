package hu.kits.tennis.common;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.http.HttpMethod;

public class UseCaseFileParser {

    public static List<TestCall> parseUseCaseFile(File useCaseFile, boolean withComments) throws IOException {
        
        List<String> lines = Files.readAllLines(useCaseFile.toPath());
        List<List<String>> linesForCallList = new ArrayList<>();
        List<String> linesForCall = new ArrayList<>();
        for(String line : lines) {
            if(line.startsWith("Call")) {
                linesForCallList.add(linesForCall);
                linesForCall = new ArrayList<>();
            }
            linesForCall.add(line);
        }
        linesForCallList.add(linesForCall);
        
        return linesForCallList.stream()
            .filter(l -> !l.isEmpty())
            .map(l -> parseCall(l, withComments))
            .collect(toList());
    }
    
    private static TestCall parseCall(List<String> callLines, boolean withComments) {
        
        List<String> lines = callLines.stream()
                .filter(line -> withComments || !line.startsWith("#"))
                .filter(line -> !line.isBlank())
                .collect(toList());
        
        List<String> descriptionLines = findSectionLines(lines, "description");
        List<String> urlLines = findSectionLines(lines, "url");
        List<String> methodLines = findSectionLines(lines, "method");
        List<String> requestLines = findSectionLines(lines, "request");
        List<String> responseStatusLines = findSectionLines(lines, "response-status");
        List<String> responseBodyLines = findSectionLines(lines, "response-body");
        List<String> staticTimeLines = findSectionLines(lines, "set-time");
        
        String name = lines.get(0);
        String description = readValue(String.join("\n", descriptionLines));
        String urlTemplate = readValue(readValue(urlLines.get(0)));
        HttpMethod httpMethod = HttpMethod.valueOf(readValue(methodLines.get(0)));
        
        String requestJson = readJson(requestLines);
        int responseStatusCode = Integer.parseInt(readValue(responseStatusLines.get(0)));
        String responseJson = readJson(responseBodyLines);
        LocalDateTime staticTime = readStaticTime(staticTimeLines);
        
        return new TestCall(name, description, urlTemplate, httpMethod, requestJson, responseStatusCode, responseJson, staticTime);
    }
    
    private static LocalDateTime readStaticTime(List<String> staticTimeLines) {
        return !staticTimeLines.isEmpty() ? LocalDateTime.parse(readValue(staticTimeLines.get(0))) : null; 
    }

    private static String readJson(List<String> lines) {
        return lines.isEmpty() ? "" : lines.stream().skip(1).collect(joining("\n"));
    }
    
    private static String readValue(String line) {
        return line.substring(line.indexOf(":") + 1).trim();
    }
    
    private static List<String> findSectionLines(List<String> allLines, String section) {
        
        Optional<Integer> startIndex = findStartIndex(allLines, section);
        if(startIndex.isPresent()) {
            int start = startIndex.get();
            int end = findEndIndex(allLines, start);
            return allLines.subList(start, end);
        } else {
            return List.of();
        }
    }
    
    private static Optional<Integer> findStartIndex(List<String> allLines, String section) {
        for(int i=0;i<allLines.size();i++) {
            String line = allLines.get(i);
            if(line.startsWith(section + ":")) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
    
    private static Integer findEndIndex(List<String> allLines, int startIndex) {
        for(int i=startIndex+1;i<allLines.size();i++) {
            String line = allLines.get(i);
            if(StringUtil.startsWith(line, "\\w.*:")) {
                return i;
            }
        }
        return allLines.size();
    }
    
    public static record TestCall(String name, String description, String urlTemplate, HttpMethod httpMethod, 
            String requestJson, int responseStatus, String responseJson, LocalDateTime staticTime) {
        
        public String path() {
            return urlTemplate.replace("<url-base>", "");
        }
        
    }
    
}
