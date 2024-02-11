package hu.kits.tennis.domain.ktr;

import java.time.LocalDate;

import hu.kits.tennis.domain.player.Player;

public record KTRUpdate(Player player, LocalDate date, KTR updatedKTR) {

}
