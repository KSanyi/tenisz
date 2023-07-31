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

import hu.kits.tennis.common.IdGenerator;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResultInfo;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.UTRService;

public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final VenueRepository venueRepository;
    private final UTRService utrService;

    public TournamentService(TournamentRepository tournamentRepository, MatchRepository matchRepository, VenueRepository venueRepository, UTRService utrService) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.utrService = utrService;
        this.venueRepository = venueRepository;
    }
    
    public List<TournamentSummary> loadDailyTournamentSummariesList() {
        return loadTournamentSummariesList(Type.DAILY);
    }
    
    public List<TournamentSummary> loadTourTournamentSummariesList() {
        return loadTournamentSummariesList(Type.TOUR);
    }
    
    private List<TournamentSummary> loadTournamentSummariesList(Type tournamentType) {
        logger.info("Loading {} tournament summaries list", tournamentType);
        List<TournamentSummary> tournamentSummaries = tournamentRepository.loadTournamentSummariesList().stream()
                .filter(t -> t.type() == tournamentType)
                .toList();
        logger.info("{} {} tournament summaries list loaded", tournamentType, tournamentSummaries.size());
        return tournamentSummaries;
    }

    public Tournament createTournament(TournamentParams tournamentParams) {
        String id = IdGenerator.generateId();
        Tournament tournament = new Tournament(
                id,
                tournamentParams,
                List.of(), 
                Status.DRAFT, 
                List.of());
        tournamentRepository.createTournament(tournament);
        logger.info("Tournament is created: {}", tournament);
        return tournament;
    }
    
    public void updateTournamentName(Tournament tournament, String updatedName) {
        tournamentRepository.updateTournamentName(tournament.id(), updatedName);
        logger.info("Tournament name is updated: {} -> {}", tournament.params().name(), updatedName);
    }
    
    public void updateTournamentDate(Tournament tournament, LocalDate updatedDate) {
        tournamentRepository.updateTournamentDate(tournament.id(), updatedDate);
        logger.info("Tournament date is updated: {} -> {}", tournament.params().date(), updatedDate);
    }
    
    public void updateTournamentVenue(Tournament tournament, String venue) {
        tournamentRepository.updateTournamentVenue(tournament.id(), venue);
        logger.info("Tournament venue is updated: {} -> {}", tournament.params().venue(), venue);
    }
    
    public void updateTournamentType(Tournament tournament, Structure structure) {
        tournamentRepository.updateTournamentType(tournament.id(), structure);
        logger.info("Tournament type is updated: {} -> {}", tournament.params().structure(), structure);
    }
    
    public void updateContestants(Tournament tournament, List<Contestant> contestants) {
        
        List<Contestant> updatedContestants = IntStream.range(0, contestants.size())
                .mapToObj(index -> contestants.get(index).withRank(index+1))
                .filter(c -> !c.player().equals(Player.BYE))
                .collect(toList());
        
        tournamentRepository.updateContestants(tournament.id(), updatedContestants);
        logger.info("Contestants updated: {} -> {}", tournament.contestants(), updatedContestants);
    }
    
    public void deleteTournament(Tournament tournament) {
        tournamentRepository.deleteTournament(tournament.id());
        matchRepository.deleteMatchesForTournament(tournament.id());
        logger.info("Tournament {} is deleted", tournament);
    }
    
    public Optional<Tournament> findTournament(String tournamentId) {
        logger.debug("Finding tournament: {}", tournamentId);
        return tournamentRepository.findTournament(tournamentId);
    }
    
    public void createMatches(String tournamentId) {
        
        Tournament tournament = loadTournament(tournamentId);
        
        if(tournament.status() == Status.DRAFT) {
            
            matchRepository.deleteMatchesForTournament(tournament.id());
            
            List<Contestant> contestants = tournament.playersLineup();
            
            int matchNumber = 1;
            List<Match> matches = new ArrayList<>();
            for(int i=0;i<contestants.size();i+=2) {
                Player player1 = contestants.get(i).player();
                Player player2 = contestants.get(i+1).player();
                Match match = Match.createNew(tournament.id(), 1, matchNumber, null, player1, player2);
                matchRepository.save(new BookedMatch(match, null, null, null, null));
                matches.add(match);
                
                if(player1.equals(Player.BYE)) {
                    int nextRoundMatchNumber = tournament.mainBoard().nextRoundMatchNumber(matchNumber);
                    Match nextRoundMatch = tournament.followUpMatch(match);
                    if(nextRoundMatch == null) {
                        if(matchNumber % 2 == 1) {
                            nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.params().date(), player2, null);    
                        } else {
                            nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.params().date(), null, player2);   
                        }
                        matchRepository.save(new BookedMatch(nextRoundMatch, null, null, null, null));
                    } else {
                        if(match.tournamentMatchNumber() % 2 == 1) {
                            matchRepository.setPlayer1(nextRoundMatch.id(), player2);    
                        } else {
                            matchRepository.setPlayer2(nextRoundMatch.id(), player2);
                        }
                    }
                } else if(player2.equals(Player.BYE)) {
                    int nextRoundMatchNumber = tournament.mainBoard().nextRoundMatchNumber(matchNumber);
                    Match nextRoundMatch = tournament.followUpMatch(match);
                    if(nextRoundMatch == null) {
                        if(matchNumber % 2 == 1) {
                            nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.params().date(), player1, null);    
                        } else {
                            nextRoundMatch = Match.createNew(tournament.id(), 1, nextRoundMatchNumber, tournament.params().date(), null, player1);   
                        }
                        matchRepository.save(new BookedMatch(nextRoundMatch, null, null, null, null));
                    } else {
                        if(match.tournamentMatchNumber() % 2 == 1) {
                            matchRepository.setPlayer1(nextRoundMatch.id(), player1);    
                        } else {
                            matchRepository.setPlayer2(nextRoundMatch.id(), player1);
                        }
                    }
                }
                
                tournament = tournamentRepository.findTournament(tournamentId).get();
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
        match = matchRepository.loadMatch(match.id());
        
        Tournament tournament = loadTournament(match.tournamentId());
        
        if(tournament.status() == Status.DRAFT) {
            tournamentRepository.updateTournamentStatus(match.tournamentId(), Status.LIVE);
            logger.info("Tournament {} status is updated to LIVE", tournament);
        }
        
        Player winner = match.winner();
        
        if(tournament.isBoardFinal(match)) {
            tournamentRepository.updateTournamentStatus(match.tournamentId(), Status.COMPLETED);
            logger.info("Tournament {} status is updated to COMPLETED", tournament);

            setWinner(tournament.id(), match.winner());
        } else {
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
            
            if(tournament.params().structure() == Structure.BOARD_AND_CONSOLATION && match.tournamentBoardNumber() == 1 && tournament.mainBoard().roundNumber(match) == 1) {
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
        
        utrService.recalculateAllUTRs();
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
    
    public void deleteMatchResult(Match match) {
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
        
        matchRepository.setResult(new MatchResultInfo(match, null, null));
        
        logger.info("Match result deleted: {}", match);
    }

    public void setWinner(String tournamentId, Player winner) {
        tournamentRepository.setWinner(tournamentId, winner.id());
    }
    
    public List<String> loadVenues() {
        return venueRepository.loadVenues();
    }

    public void setPaymentStatus(Tournament tournament, Player player, PaymentStatus paymentStatus) {
        tournamentRepository.setPaymentStatus(tournament.id(), player.id(), paymentStatus);
        logger.info("Payment staus is set to {} for {} for tournament {}", paymentStatus, player, tournament);
    }

}
