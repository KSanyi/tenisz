package hu.kits.tennis.infrastructure.ui.views.utr.playerstats;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "player-stats/:playerId", layout = MainLayout.class)
public class PlayerStatsView extends SplitViewFrame implements View, BeforeEnterObserver, HasDynamicTitle {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayerStatsComponent playerStatsComponent = new PlayerStatsComponent();
    private final PlayerStatsComponentMobile playerStatsComponentMobile = new PlayerStatsComponentMobile();
    
    private final PlayerRepository playerRepository = Main.applicationContext.getPlayerRepository();
    private Player player;
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        playerStatsComponent.setPadding(true);
        setViewContent(playerStatsComponent);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisiblePars(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisiblePars(e.getWidth()));
    }
    
    private void updateVisiblePars(int width) {
        
        boolean isMobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        
        if(isMobile) {
            setViewContent(playerStatsComponentMobile);
        } else {
            setViewContent(playerStatsComponent);
        }
    }
    
    public void refresh() {
        player = playerRepository.findPlayer(player.id()).get();
        playerStatsComponent.setPlayer(player);
        playerStatsComponentMobile.setPlayer(player);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            int playerId = event.getRouteParameters().get("playerId").map(Integer::parseInt).orElse(0);
            Optional<Player> player = playerRepository.findPlayer(playerId);
            this.player = player.get();
            playerStatsComponent.setPlayer(player.get());
            playerStatsComponentMobile.setPlayer(player.get());
            VaadinUtil.logUserAction(logger, "Looking for {}'s stats view", this.player.name());
        } catch(Exception ex) {
            KITSNotification.showError("Hibás azonosító az url-ben");
        }
    }

    @Override
    public String getPageTitle() {
        return player.name() + " statisztikái";
    }
    
}
