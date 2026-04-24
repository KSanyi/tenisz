package hu.kits.tennis.infrastructure.ui.views.ktr.head2head;

import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.match.Head2HeadData;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.ktr.ranking.KTRRankingView;

@Route(value = "head2head", layout = MainLayout.class)
@PageTitle("Head-2-Head")
@AllowedRoles({Role.ADMIN, Role.ANONYMUS, Role.VISITOR, Role.MEMBER})
public class Head2HeadView extends SplitViewFrame implements View {

    private final MatchService matchService = Main.applicationContext.getMatchService();

    private final TextField filterField = new TextField();
    private final Head2HeadGrid head2HeadGrid = new Head2HeadGrid();
    private final Head2HeadMatchesPanel matchesPanel = new Head2HeadMatchesPanel();
    private final Button ktrRankingButton = UIUtils.createButton("KTR Rangsor", ButtonVariant.LUMO_SMALL);

    private HorizontalLayout content;

    public Head2HeadView() {
        filterField.setPlaceholder("Játékos szűrő");
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(e -> head2HeadGrid.filter(e.getValue()));
        ktrRankingButton.addClickListener(click -> UI.getCurrent().navigate(KTRRankingView.class));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());

        head2HeadGrid.addSelectionListener(this::pairSelected);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleParts(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleParts(e.getWidth()));

        refresh();
    }

    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }

    private Component createContent() {
        HorizontalLayout toolbar = new HorizontalLayout(filterField, ktrRankingButton);
        toolbar.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        VerticalLayout gridColumn = new VerticalLayout(toolbar, head2HeadGrid);
        gridColumn.setPadding(false);
        gridColumn.setSpacing(false);
        gridColumn.setSizeUndefined();
        gridColumn.setWidthFull();

        content = new HorizontalLayout(gridColumn, matchesPanel);
        content.setFlexGrow(1, gridColumn);
        content.setFlexGrow(2, matchesPanel);
        content.setSizeFull();
        content.setPadding(true);

        return content;
    }

    private void pairSelected(SelectionEvent<Grid<Head2HeadData>, Head2HeadData> event) {
        event.getFirstSelectedItem().ifPresent(data -> {
            matchesPanel.setHead2HeadData(data);
        });
    }

    @Override
    public void refresh() {
        List<Head2HeadData> data = matchService.loadHead2HeadDataTopList(500);
        head2HeadGrid.setData(data);
    }

    private void updateVisibleParts(int width) {
        boolean isSmallScreen = width < VaadinUtil.SMALL_SCREEN_BREAKPOINT;
        matchesPanel.setVisible(!isSmallScreen);
        filterField.setWidth(width < VaadinUtil.MOBILE_BREAKPOINT ? "150px" : "220px");
        content.setPadding(width >= VaadinUtil.MOBILE_BREAKPOINT);
    }
}
