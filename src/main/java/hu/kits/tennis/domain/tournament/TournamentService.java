package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResultInfo;
import hu.kits.tennis.domain.utr.Player;

public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    public TournamentService(TournamentRepository tournamentRepository, MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
    }

    // TODO
    public List<Tournament> loadAllTournaments() {
        return tournamentRepository.loadAllTournaments();
    }
    
    public Tournament createTournament(Organizer organiser, String name, String venue, LocalDate date, Tournament.Type type, int bestOfNSets) {
        Tournament tournament = Tournament.createNew(organiser, name, venue, date, type, bestOfNSets);
        tournamentRepository.createTournament(tournament);
        return tournament;
    }
    
    public void updateTournamentName(Tournament tournament, String updatedName) {
        tournamentRepository.updateTournamentName(tournament.id(), updatedName);
        logger.info("Tournament name is updated: {} -> {}", tournament.name(), updatedName);
    }
    
    public void updateTournamentDate(Tournament tournament, LocalDate updatedDate) {
        tournamentRepository.updateTournamentDate(tournament.id(), updatedDate);
        logger.info("Tournament date is updated: {} -> {}", tournament.date(), updatedDate);
    }
    
    public void updateTournamentVenue(Tournament tournament, String venue) {
        tournamentRepository.updateTournamentVenue(tournament.id(), venue);
        logger.info("Tournament venue is updated: {} -> {}", tournament.venue(), venue);
    }
    
    public void updateTournamentType(Tournament tournament, Tournament.Type type) {
        tournamentRepository.updateTournamentType(tournament.id(), type);
        logger.info("Tournament type is updated: {} -> {}", tournament.type(), type);
    }
    
    public void updateContestants(Tournament tournament, List<Player> players) {
        
        List<Contestant> contestants = IntStream.range(0, players.size())
                .mapToObj(index -> new Contestant(players.get(index), index+1))
                .filter(c -> !c.player().equals(Player.BYE))
                .collect(toList());
        
        tournamentRepository.updateContestants(tournament.id(), contestants);
        logger.info("Contestants updated: {} -> {}", tournament.contestants(), contestants);
    }
    
    public void deleteTournament(Tournament tournament) {
        tournamentRepository.deleteTournament(tournament.id());
        logger.info("Tournament {} is deleted", tournament);
    }
    
    public Optional<Tournament> findTournament(String tournamentId) {
        return tournamentRepository.findTournament(tournamentId);
    }
    
    public void createMatches(String tournamentId, DrawMode drawMode) {
        
        Tournament tournament = loadTournament(tournamentId);
        
        if(tournament.status() == Status.DRAFT) {
            
            matchRepository.deleteMatchesForTournament(tournament.id());
            
            List<Player> players = tournament.playersLineup();
            
            int matchNumber = 1;
            List<Match> matches = new ArrayList<>();
            for(int i=0;i<players.size();i+=2) {
                Player player1 = players.get(i);
                Player player2 = players.get(i+1);
                Match match = Match.createNew(tournament.id(), 1, matchNumber, null, player1, player2);
                matchRepository.save(new BookedMatch(match, null, null, null, null));
                matches.add(match);
                
                if(player1.equals(Player.BYE)) {
                    int nextRoundMatchNumber = tournament.mainBoard().nextRoundMatchNumber(matchNumber);
                    Match nextRoundMatch;
                    if(matchNumber % 2 == 1) {
                        nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.date(), player2, null);    
                    } else {
                        nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.date(), null, player2);   
                    }
                    matchRepository.save(new BookedMatch(nextRoundMatch, null, null, null, null));
                } else if(player2.equals(Player.BYE)) {
                    int nextRoundMatchNumber = tournament.mainBoard().nextRoundMatchNumber(matchNumber);
                    Match nextRoundMatch;
                    if(matchNumber % 2 == 1) {
                        nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.date(), player1, null);    
                    } else {
                        nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.date(), null, player1);   
                    }
                    matchRepository.save(new BookedMatch(nextRoundMatch, null, null, null, null));
                }
                
                matchNumber++;
            }
            
            logger.info("{} matches created for {}", matches.size(), tournament);
        }
        
    }

    public void setTournamentMatchResult(MatchResultInfo matchResultInfo) {
        
        logger.info("Setting {}", matchResultInfo);
        
        Match match = matchResultInfo.match();
        MatchResult matchResult = matchResultInfo.matchResult();
        
        matchRepository.setResult(matchResultInfo);
        
        Tournament tournament = loadTournament(match.tournamentId());
        
        if(tournament.status() == Status.DRAFT) {
            tournamentRepository.updateTournamentStatus(match.tournamentId(), Status.LIVE);
            logger.info("Tournament {} status is updated to LIVE", tournament);
        }
        
        Player winner = matchResult.isPlayer1Winner() ? match.player1() : match.player2();
        
        if(tournament.isBoardFinal(match)) {
            return;
        }
        
        int nextRoundMatchNumber = tournament.nextRoundMatchNumber(match);
        
        Match followUpMatch = tournament.followUpMatch(match);
        
        if(followUpMatch == null) {
            if(match.tournamentMatchNumber() % 2 == 1) {
                followUpMatch = Match.createNew(match.tournamentId(), match.tournamentBoardNumber(), nextRoundMatchNumber, null, winner, null);
            } else {
                followUpMatch = Match.createNew(match.tournamentId(), match.tournamentBoardNumber(), nextRoundMatchNumber, null, null, winner);
            }
            matchRepository.save(new BookedMatch(followUpMatch, null, null, null, null));
            logger.info("Next round match created: {}", followUpMatch);
        } else {
            if(match.tournamentMatchNumber() % 2 == 1) {
                matchRepository.setPlayer1(followUpMatch.id(), winner);    
            } else {
                matchRepository.setPlayer2(followUpMatch.id(), winner);
            }
            logger.info("{} is set to next round match: {}", winner.name(), followUpMatch);
        }
        
        if(tournament.type() == Type.BOARD_AND_CONSOLATION && match.tournamentBoardNumber() == 1 && tournament.mainBoard().roundNumber(match) == 1) {
            Player loser = matchResult.isPlayer1Winner() ? match.player2() : match.player1();
            
            int consolationMatchNumber = (match.tournamentMatchNumber() + 1) / 2;
            
            Match consolationMatch = tournament.getMatch(2, consolationMatchNumber);
            
            if(consolationMatch == null) {
                if(match.tournamentMatchNumber() % 2 == 1) {
                    consolationMatch = Match.createNew(match.tournamentId(), 2, consolationMatchNumber, null, loser, null);
                } else {
                    consolationMatch = Match.createNew(match.tournamentId(), 2, consolationMatchNumber, null, null, loser);
                }
                matchRepository.save(new BookedMatch(consolationMatch, null, null, null, null));
                logger.info("Consolation round match created: {}", consolationMatch);
            } else {
                if(match.tournamentMatchNumber() % 2 == 1) {
                    matchRepository.setPlayer1(consolationMatch.id(), loser);    
                } else {
                    matchRepository.setPlayer2(consolationMatch.id(), loser);
                }
                logger.info("{} is set to consolation match: {}", loser.name(), consolationMatch);
            }
        }
        
    }
    
    private Tournament loadTournament(String tournamentId) {
        return tournamentRepository.findTournament(tournamentId).get();
    }

    // TODO test
    public void deleteMatch(Match match) {
        
        if(match.tournamentId() != null) {
            Tournament tournament = loadTournament(match.tournamentId());
            Match followUpMatch = tournament.followUpMatch(match);
            Player winner = match.winner();
            if(followUpMatch != null && followUpMatch.hasPlayer(winner)) {
                if(!followUpMatch.arePlayersSet()) {
                    deleteMatch(followUpMatch);
                } else {
                    if(winner.equals(followUpMatch.player1())) {
                        matchRepository.setPlayer1(followUpMatch.id(), null);
                    } else {
                        matchRepository.setPlayer2(followUpMatch.id(), null);
                    }
                    if(followUpMatch.result() != null) {
                        matchRepository.setResult(new MatchResultInfo(followUpMatch, null, null));
                    } 
                }
            }
        }
        
        matchRepository.deleteMatch(match.id());
        
        logger.info("Match deleted: {}", match);
    }

}
