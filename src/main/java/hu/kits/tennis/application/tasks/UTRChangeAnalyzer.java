package hu.kits.tennis.application.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ResourceFactory;

public class UTRChangeAnalyzer {

    private final UTRService utrService;

    public UTRChangeAnalyzer(ResourceFactory resourceFactory) {
        utrService = resourceFactory.getUTRService();
    }
    
    public void analyse() {
        
        List<NameAndUTR> currentUTRs = utrService.calculateUTRRanking().stream()
                .map(u -> new NameAndUTR(u.player().name(), u.utr().value())).toList();
        
        List<NameAndUTR> utrsFromFile = loadNamesAndUTRsFromFile();
        
        compare(currentUTRs, utrsFromFile);
    }
    
    private List<NameAndUTR> loadNamesAndUTRsFromFile() {
        try {
            return Files.lines(Paths.get("c:\\Users\\kocso\\Desktop\\utr-csv.csv")).map(this::parse).toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private NameAndUTR parse(String line) {
        String[] parts = line.split(";");
        Double utr;
        try {
            utr = Double.parseDouble(parts[4].replace(",", "."));
        } catch(NumberFormatException ex) {
            utr = null;
        }
        return new NameAndUTR(parts[0], utr);
    }
    
    private static void compare(List<NameAndUTR> currentUTRs, List<NameAndUTR> utrsFromFile) {
        
        List<NameAndUTR> result = new ArrayList<>();
        
        Map<String, Double> currentUTRsByName = currentUTRs.stream()
                .filter(n -> n.utr != null)
                .collect(Collectors.toMap(NameAndUTR::name, NameAndUTR::utr));
        
        for(NameAndUTR nameAndUTR : utrsFromFile) {
            String name = nameAndUTR.name;
            Double utr = nameAndUTR.utr();
            Double currentUTR = currentUTRsByName.get(name);
            if(utr != null && currentUTR != null) {
                double diff = round(currentUTR - utr);
                result.add(new NameAndUTR(name, diff)); 
            }
        }
        
        result.stream().sorted(Comparator.comparing(NameAndUTR::utr)).forEach(System.out::println);
    }
    
    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
    
    private record NameAndUTR(String name, Double utr) {
        
    }
    
    
    
}
