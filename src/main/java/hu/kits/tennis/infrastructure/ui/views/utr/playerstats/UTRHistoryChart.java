package hu.kits.tennis.infrastructure.ui.views.utr.playerstats;

import java.util.ArrayList;
import java.util.List;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.labels.builder.StyleBuilder;
import com.github.appreciated.apexcharts.config.yaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;

import hu.kits.tennis.domain.utr.UTRHistory;;

class UTRHistoryChart extends ApexCharts {

    UTRHistoryChart(UTRHistory utrHistory) {
        super.setChart(ChartBuilder.get()
                            .withType(Type.AREA)
                            .withStacked(false)
                            .withToolbar(ToolbarBuilder.get()
                                    .withShow(false)
                                    .build())
                            .build());

        super.setDataLabels(DataLabelsBuilder.get()
                .withEnabled(false)
                .build());
        
        super.setYaxis(new YAxis[] {YAxisBuilder.get()
                .withLabels(LabelsBuilder.get()
                        .withFormatter("""
                                function (val) {
                                  return new Intl.NumberFormat('hu-HU', {
                                  maximumFractionDigits: 2,
                                }).format(val)
                                }
                                """)
                        .build())
                .build()});
        
        super.setXaxis(XAxisBuilder.get()
                .withType(XAxisType.DATETIME)
                .withLabels(com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder.get().withStyle(StyleBuilder.get()
                        .withFontSize("10px")
                        .build()).build())
                .build());
        
        super.setStroke(StrokeBuilder.get()
                .withCurve(Curve.STRAIGHT)
                .withWidth(2.)
                .build());
        
        List<Coordinate<?,?>> seriesData = new ArrayList<>();
        for(var entry : utrHistory.entries()) {
            seriesData.add(new Coordinate<>(entry.date().toString(), entry.utr().value()));
        }
        super.setSeries(new Series<>("UTR", seriesData.toArray(new Coordinate[0])));
    }
    
    public void setData(UTRHistory utrHistory) {
        List<Coordinate<?,?>> seriesData = new ArrayList<>();
        for(var entry : utrHistory.entries()) {
            seriesData.add(new Coordinate<>(entry.date().toString(), entry.utr().value()));
        }
        
        super.setSeries(new Series<>("UTR", seriesData.toArray(new Coordinate[0])));
    }
    
}
