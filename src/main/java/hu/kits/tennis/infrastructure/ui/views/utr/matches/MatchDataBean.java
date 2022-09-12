package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import java.time.LocalDate;
import java.util.List;

import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.Player;

public class MatchDataBean {

    private final int id;
    private LocalDate date;
    private Player player1;
    private Player player2;
    private int score1;
    private int score2;
    
    public MatchDataBean(Match match) {
        id = match.id();
        date = match.date();
        player1 = match.player1();
        player2 = match.player2();
        score1 = match.result().setResults().get(0).player1Score();
        score2 = match.result().setResults().get(0).player2Score();
    }

    public MatchDataBean(MatchInfo matchInfo) {
        id = matchInfo.id();
        date = matchInfo.date();
        player1 = matchInfo.player1();
        player2 = matchInfo.player2();
        score1 = matchInfo.result().setResults().get(0).player1Score();
        score2 = matchInfo.result().setResults().get(0).player2Score();
    }

    public MatchDataBean() {
        id = 0;
        date = Clock.today();
    }

    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
    
    public int getScore1() {
        return score1;
    }
    
    public void setScore1(int score1) {
        this.score1 = score1;
    }
    
    public int getScore2() {
        return score2;
    }
    
    public void setScore2(int score2) {
        this.score2 = score2;
    }

    public Match toPlayedMatch() {
        return new Match(id, null, null, null, date, player1, player2, new MatchResult(List.of(new SetResult(score1, score2))));
    }

    public boolean isNewBean() {
        return id == 0;
    }

    public int getId() {
        return id;
    }
    
}
