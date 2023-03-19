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
        
        Map<Integer, UTREntry> originalMap = loadEntries("utr_20220920.txt");
        Map<Integer, UTREntry> updatedMap = loadEntries("utr.txt");
        
        Set<Integer> ids = CollectionsUtil.union(originalMap.keySet(), updatedMap.keySet());
        
        Map<String, Double> diffMap = new HashMap<>();
        for(int id : ids) {
            UTREntry originalEntry = originalMap.get(id);
            UTREntry updatedEntry = updatedMap.get(id);
            
            if(originalEntry == null) {
                diffMap.put(updatedEntry.name(), 0.);
            } else if(updatedEntry == null) {
                diffMap.put(originalEntry.name(), 0.);
            } else {
                diffMap.put(updatedEntry.name(), updatedEntry.utr - originalEntry.utr);
            }
        }
        
        double avg = diffMap.values().stream().mapToDouble(d -> d).average().getAsDouble();
        UTRStatEntry maxEntry = findMax(diffMap);
        System.out.println("Average: " + avg);
        System.out.println("Max: " + maxEntry.value + " (" + maxEntry.name + ")");
        
        diffMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(e -> System.out.println(String.format("%2.2f", e.getValue()) + "\t" + e.getKey()));
    }
    
    private static Map<Integer, UTREntry> loadEntries(String fileName) throws Exception {
        
        Map<Integer, UTREntry> entries = new HashMap<>();
        for(String line : Files.readAllLines(FOLDER.resolve(fileName))) {
            String parts[] = line.split("\t");
            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            double utr = parts[2].equals("nu") ? 0 : FORMAT.parse(parts[2]).doubleValue();
            entries.put(id, new UTREntry(id, name, utr));
        }
        
        return entries;
    }
    
    private static UTRStatEntry findMax(Map<String, Double> diffMap) {
        UTRStatEntry maxEntry = new UTRStatEntry("", 0);
        for(var entry : diffMap.entrySet()) {
            if(entry.getValue() > maxEntry.value) {
                maxEntry = new UTRStatEntry(entry.getKey(), entry.getValue());
            }
        }
        return maxEntry;
    }
    
    private static record UTREntry(int id, String name, double utr) {
        
    }
    
    private static record UTRStatEntry(String name, double value) {
        
    }
    
}
