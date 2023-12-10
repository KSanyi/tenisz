package hu.kits.tennis.infrastructure.database;

import java.util.List;

import hu.kits.tennis.domain.tournament.VenueRepository;

public class VenueHardcodedRepository implements VenueRepository {

    @Override
    public List<String> loadVenues() {
        return List.of("Építők", "GS Tenisz Klub", "BVSC Tatai", "BVSC Szőnyi", "Mini Garros", "Open Teniszvölgy", "Normafa");
    }

}
