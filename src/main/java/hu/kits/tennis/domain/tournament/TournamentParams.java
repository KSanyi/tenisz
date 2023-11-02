package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

import hu.kits.tennis.domain.tournament.TournamentSummary.CourtInfo;

public record TournamentParams(
        Organization organization,
        Type type,
        Level levelFrom,
        Level levelTo,
        LocalDate date, 
        String name,
        String venue,
        CourtInfo courtInfo,
        Structure structure,
        int bestOfNSets,
        String description) {
    
    public enum Type {
        TOUR("Tour"), DAILY("Napi verseny");
        
        public final String label;

        private Type(String label) {
            this.label = label;
        }
    }
    
    public enum Level {
        L90(90), L125(125), L250(250), L375(375), L500(500), L625(625), L750(750), L875(875), L1000(1000);
        
        public final int value;

        private Level(int value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    public enum Surface {
        CLAY("Salak"), HARD("Kemény");
        
        public final String label;

        private Surface(String label) {
            this.label = label;
        }
    }
    
    public enum VenueType {
        
        INDOOR("Csarnok"), TENT("Sátor"), OUTDOOR("Szabadtér");
        
        public final String label;

        private VenueType(String label) {
            this.label = label;
        }
    }
    
    public enum Status {
        LIVE,
        DRAFT,
        COMPLETED;
    }
    
    public enum Structure {
        SIMPLE_BOARD("Főtábla"),
        BOARD_AND_CONSOLATION("Főtábla és vigasztábla"),
        NA("Csak meccsek");
        
        public final String label;

        private Structure(String label) {
            this.label = label;
        }
    }
    
}
