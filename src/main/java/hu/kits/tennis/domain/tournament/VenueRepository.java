package hu.kits.tennis.domain.tournament;

import java.util.List;

public interface VenueRepository {

    List<String> loadVenues();
    
}
