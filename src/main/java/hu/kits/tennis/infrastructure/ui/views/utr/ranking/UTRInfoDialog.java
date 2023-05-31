package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;

import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class UTRInfoDialog extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private UTRInfoDialog() {
        
        setDraggable(true);
        setResizable(true);
        
        setHeaderTitle("UTR információ");
        Button closeButton = UIUtils.createTertiaryButton(VaadinIcon.CLOSE);
        closeButton.addClickListener(click -> close());
        getHeader().add(closeButton);
        
        String content = """
                A játékos UTR-je a legutóbbi 25&ast; mérkőzése <b>meccs UTR</b>-jének súlyozott átlaga.<br/>
                A súlyozás szempontjai:
                <ul>
                <li>mérkőzés dátuma: a korábbi mérkőzések kisebb súlyt képviselnek</li>
                <li>mérkőzés típusa: a 2 győztes szettre játszott TOUR meccsek súlya kétszerese az 1 szettes napi meccek súlyának</li>
                </ul>
                Meccs UTR: egy játékos mérkőzésének rá vonatkozó meccs UTR-je az ellenfél adott napi UTR-jétől és a mérkőzésen nyert illetve veszített gamek számától függ.<br/>
                Példa: Kovács András (<b>UTR 8.0</b>) játszik Kócsó Sándorral (<b>UTR 7.0</b>) és az eredmény:<br/>
                <li><b>6:0 6:0</b>: a meccs UTR Kokónak <b>9.0</b>, Sanyinak <b>5.0</b></li>
                <li><b>6:2 6:4</b>: a meccs UTR Kokónak <b>8.0</b>, Sanyinak <b>8.0</b></li>
                <li><b>3:6 3:6</b>: a meccs UTR Kokónak <b>6.0</b>, Sanyinak <b>9.0</b></li>
                <li><b>0:6 0:6</b>: a meccs UTR Kokónak <b>5.0</b>, Sanyinak <b>10.0</b></li>
                <br/>
                Ha a két játékos UTR-jének különbsége nagyobb mint 2, akkor a mérkőzésen nem számolunk UTR-t függetlenül az eredménytől.
                <br/>
                <h4>Meccs táblázat</h4>
                Az UTR rangsorban egy játékosra kattintva megjelenik a játékos összes mérkőzése. (a funkció jelenleg mobilon nem működik)<br/>
                <li>zöld szín jelöli a játékos legjobb meccsét</li>
                <li>piros szín jelöli a játékos legrosszabb meccsét</li>
                <li>szürke szín jelöli az UTR szempontjából figyelembe nem vett meccseket</li>
                <br/>
                &ast; <small>amennyiben a játékos 25. mérkőzése napján több meccset is játszott (pl egy napi versenyen szerepelt), akkor minden aznap játszott mérkőzés beleszámít az UTR-be</small>
                """;
        
        Label contentLabel = new Label();
        contentLabel.getElement().setProperty("innerHTML", content);
        add(contentLabel);
    }
    
    static void openDialog() {
        new UTRInfoDialog().open();
        VaadinUtil.logUserAction(logger, "opened UTRInfoDialog");
    }
    
}
