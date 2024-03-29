package hu.kits.tennis.infrastructure.ui.views.ktr.ranking;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;

import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class KTRInfoDialog extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private KTRInfoDialog() {
        
        setDraggable(true);
        setResizable(true);
        
        setHeaderTitle("KTR információ");
        Button closeButton = UIUtils.createTertiaryButton(VaadinIcon.CLOSE);
        closeButton.addClickListener(click -> close());
        getHeader().add(closeButton);
        
        String content = """
                A játékos KTR-je a legutóbbi 25&ast; mérkőzése <b>meccs KTR</b>-jének súlyozott átlaga.<br/>
                A súlyozás szempontjai:
                <ul>
                <li>mérkőzés dátuma: a korábbi mérkőzések kisebb súlyt képviselnek</li>
                <li>mérkőzés típusa: a 2 győztes szettre játszott TOUR meccsek súlya kétszerese az 1 szettes napi meccek súlyának</li>
                </ul>
                Meccs KTR: egy játékos mérkőzésének rá vonatkozó meccs KTR-je az ellenfél adott napi KTR-jétől és a mérkőzésen nyert illetve veszített gamek számától függ.<br/>
                Példa: Kovács András (<b>KTR 8.0</b>) játszik Kócsó Sándorral (<b>KTR 7.0</b>) és az eredmény:<br/>
                <li><b>6:0 6:0</b>: a meccs KTR Kokónak <b>9.0</b>, Sanyinak <b>5.0</b></li>
                <li><b>6:2 6:4</b>: a meccs KTR Kokónak <b>8.0</b>, Sanyinak <b>8.0</b></li>
                <li><b>3:6 3:6</b>: a meccs KTR Kokónak <b>6.0</b>, Sanyinak <b>9.0</b></li>
                <li><b>0:6 0:6</b>: a meccs KTR Kokónak <b>5.0</b>, Sanyinak <b>10.0</b></li>
                <br/>
                Ha a két játékos KTR-jének különbsége nagyobb mint 2, akkor a mérkőzésen nem számolunk KTR-t függetlenül az eredménytől.
                <br/>
                <h4>Meccs táblázat</h4>
                Az KTR rangsorban egy játékosra kattintva megjelenik a játékos összes mérkőzése.<br/>
                <li>zöld szín jelöli a játékos legjobb meccsét</li>
                <li>piros szín jelöli a játékos legrosszabb meccsét</li>
                <li>szürke szín jelöli az KTR szempontjából figyelembe nem vett meccseket</li>
                <br/>
                &ast; <small>amennyiben a játékos 25. mérkőzése napján több meccset is játszott (pl egy napi versenyen szerepelt), akkor minden aznap játszott mérkőzés beleszámít az KTR-be</small>
                """;
        
        Label contentLabel = new Label();
        contentLabel.getElement().setProperty("innerHTML", content);
        add(contentLabel);
    }
    
    static void openDialog() {
        new KTRInfoDialog().open();
        VaadinUtil.logUserAction(logger, "opened KTRInfoDialog");
    }
    
}
