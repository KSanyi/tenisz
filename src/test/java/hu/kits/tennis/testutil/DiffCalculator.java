package hu.kits.tennis.testutil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import hu.kits.tennis.common.CollectionsUtil;

public class DiffCalculator {
    
    private static final Path FOLDER = Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\");
    private static final NumberFormat FORMAT = NumberFormat.getInstance(new Locale("hu"));

    public static void main(String[] args) throws Exception {
        
        Map<Integer, KTREntry> originalMap = loadEntries("ktr_20220920.txt");
        Map<Integer, KTREntry> updatedMap = loadEntries("ktr.txt");
        
        Set<Integer> ids = CollectionsUtil.union(originalMap.keySet(), updatedMap.keySet());
        
        Map<String, Double> diffMap = new HashMap<>();
        for(int id : ids) {
            KTREntry originalEntry = originalMap.get(id);
            KTREntry updatedEntry = updatedMap.get(id);
            
            if(originalEntry == null) {
                diffMap.put(updatedEntry.name(), 0.);
            } else if(updatedEntry == null) {
                diffMap.put(originalEntry.name(), 0.);
            } else {
                diffMap.put(updatedEntry.name(), updatedEntry.ktr - originalEntry.ktr);
            }
        }
        
        double avg = diffMap.values().stream().mapToDouble(d -> d).average().getAsDouble();
        KTRStatEntry maxEntry = findMax(diffMap);
        System.out.println("Average: " + avg);
        System.out.println("Max: " + maxEntry.value + " (" + maxEntry.name + ")");
        
        diffMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(e -> System.out.println(String.format("%2.2f", e.getValue()) + "\t" + e.getKey()));
    }
    
    private static Map<Integer, KTREntry> loadEntries(String fileName) throws Exception {
        
        Map<Integer, KTREntry> entries = new HashMap<>();
        for(String line : Files.readAllLines(FOLDER.resolve(fileName))) {
            String parts[] = line.split("\t");
            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            double ktr = parts[2].equals("nu") ? 0 : FORMAT.parse(parts[2]).doubleValue();
            entries.put(id, new KTREntry(id, name, ktr));
        }
        
        return entries;
    }
    
    private static KTRStatEntry findMax(Map<String, Double> diffMap) {
        KTRStatEntry maxEntry = new KTRStatEntry("", 0);
        for(var entry : diffMap.entrySet()) {
            if(entry.getValue() > maxEntry.value) {
                maxEntry = new KTRStatEntry(entry.getKey(), entry.getValue());
            }
        }
        return maxEntry;
    }
    
    private static record KTREntry(int id, String name, double ktr) {
        
    }
    
    private static record KTRStatEntry(String name, double value) {
        
    }
    
}
