package hu.kits.tennis.common;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    void test() {
        String json = """
                {
                      "numberOfTournaments": 1, // number of tournaments the player participated
                      "numberOfGamesLost": 2,
                      "ktrHigh": {
                        "date": "2023-04-01",
                        "ktr": 8.2
                      },
                      "bestKTRMatchId": 1,
                      "numberOfGamesWon": 12,
                      "gamesLossPercentage": 14.29
                }
                """;

        System.out.println(json.replaceAll("//.*\n", "\n"));
        
    }

}
