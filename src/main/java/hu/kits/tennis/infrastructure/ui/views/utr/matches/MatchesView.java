package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BoxSizing;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Horizontal;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Top;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "matches", layout = MainLayout.class)
@PageTitle("Meccsek")
@AllowedRoles({Role.ADMIN})
public class MatchesView extends SplitViewFrame implements View {

    private final UTRService utrService = Main.resourceFactory.getUTRService();
    
    private final Button recalculateButton = createRecalculateButton();
    private final Button addMatchButton = createAddMatchButton();
    private final TextField filterField = new TextField();
    private final AllMatchesGrid matchesGrid = new AllMatchesGrid();
    
    public MatchesView() {
        filterField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        filterField.addValueChangeListener(e -> matchesGrid.filter(e.getValue()));
        filterField.setPlaceholder("Szűrő");
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        HorizontalLayout buttonsLayout = new HorizontalLayout(addMatchButton, recalculateButton);
        recalculateButton.setVisible(VaadinUtil.getUser().userId().equals("ksanyi"));
        recalculateButton.getStyle().set("margin-left", "auto");
        buttonsLayout.setWidthFull();
        FlexBoxLayout content = new FlexBoxLayout(buttonsLayout, filterField, matchesGrid);
        filterField.setWidth("250px");
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setSizeFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setAlignSelf(Alignment.CENTER, filterField);
        
        return content;
    }
    
    private static Button createAddMatchButton() {
        Button button = new Button("Új meccs");
        button.setIcon(new Icon(VaadinIcon.PLUS));

        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        //button.addClickListener(click -> new SimpleMatchDialog(Match.).open());
        return button;
    }
    
    private Button createRecalculateButton() {
        Button button = new Button("UTR újraszámolás");
        button.setIcon(new Icon(VaadinIcon.AUTOMATION));

        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(click -> recalculateUTRs());
        return button;
    }
    
    private void recalculateUTRs() {
        // TODO
        boolean resetUTRGroupsBefore = false;
        utrService.recalculateAllUTRs(resetUTRGroupsBefore);
        refresh();
        KITSNotification.showInfo("Az összes meccs UTR újrakalkulálva");
    }

    public void refresh() {
        matchesGrid.refresh();
    }
    
}
