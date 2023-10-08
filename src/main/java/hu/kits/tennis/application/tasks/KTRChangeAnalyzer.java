package hu.kits.tennis.application.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.infrastructure.ApplicationContext;

public class KTRChangeAnalyzer {

    private final KTRService ktrService;

    public KTRChangeAnalyzer(ApplicationContext resourceFactory) {
        ktrService = resourceFactory.getKTRService();
    }
    
    public void analyse() {
        
        List<NameAndKTR> currentKTRs = ktrService.calculateKTRRanking().stream()
                .map(u -> new NameAndKTR(u.player().name(), u.ktr().value())).toList();
        
        List<NameAndKTR> ktrsFromFile = loadNamesAndKTRsFromFile();
        
        compare(currentKTRs, ktrsFromFile);
    }
    
    private List<NameAndKTR> loadNamesAndKTRsFromFile() {
        try {
            return Files.lines(Paths.get("c:\\Users\\kocso\\Desktop\\ktr-csv.csv")).map(this::parse).toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private NameAndKTR parse(String line) {
        String[] parts = line.split(";");
        Double ktr;
        try {
            ktr = Double.parseDouble(parts[4].replace(",", "."));
        } catch(NumberFormatException ex) {
            ktr = null;
        }
        return new NameAndKTR(parts[0], ktr);
    }
    
    private static void compare(List<NameAndKTR> currentKTRs, List<NameAndKTR> ktrsFromFile) {
        
        List<NameAndKTR> result = new ArrayList<>();
        
        Map<String, Double> currentKTRsByName = currentKTRs.stream()
                .filter(n -> n.ktr != null)
                .collect(Collectors.toMap(NameAndKTR::name, NameAndKTR::ktr));
        
        for(NameAndKTR nameAndKTR : ktrsFromFile) {
            String name = nameAndKTR.name;
            Double ktr = nameAndKTR.ktr();
            Double currentKTR = currentKTRsByName.get(name);
            if(ktr != null && currentKTR != null) {
                double diff = round(currentKTR - ktr);
                result.add(new NameAndKTR(name, diff)); 
            }
        }
        
        result.stream().sorted(Comparator.comparing(NameAndKTR::ktr)).forEach(System.out::println);
    }
    
    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
    
    private record NameAndKTR(String name, Double ktr) {
        
    }
    
    
    
}
