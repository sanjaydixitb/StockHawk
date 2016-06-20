package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by sanjaydixit on 20/06/16.
 */
public class StockDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG_SYMBOL = "STOCK_SYMBOL";
    private static final int STOCK_LOADER = 1;

    private String symbol;
    private LineChartView lineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        symbol = getIntent().getStringExtra(TAG_SYMBOL);

        setTitle(symbol);
        lineChartView = (LineChartView) findViewById(R.id.linechart);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    void displayLineChart(Cursor data) {
        LineSet lineSet = new LineSet();
        float minimumPrice = Float.MAX_VALUE;
        float maximumPrice = Float.MIN_VALUE;
        float margin = 1f;

        float firstPrice = Float.MAX_VALUE, lastPrice = Float.MAX_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
            float price = Float.parseFloat(label);
            lineSet.addPoint(label, price);
            Log.d(StockDetailsActivity.class.getName(),"Added price " + price);
            if(firstPrice == Float.MAX_VALUE) {
                firstPrice = price;
            }
            lastPrice = price;

            minimumPrice = Math.min(minimumPrice, price);
            maximumPrice = Math.max(maximumPrice, price);
        }

        int line,fill,dot;

        if(firstPrice <= lastPrice) {
            line = getResources().getColor(R.color.chart_green_line);
            fill = getResources().getColor(R.color.chart_green_fill);
            dot = getResources().getColor(R.color.chart_green_dot);
        } else {
            line = getResources().getColor(R.color.chart_red_line);
            fill = getResources().getColor(R.color.chart_red_fill);
            dot = getResources().getColor(R.color.chart_red_dot);
        }

        lineSet.setColor(line)
                .setFill(fill)
                .setDotsColor(dot)
                .setThickness(2)
                .setDashed(new float[]{5f, 5f});


        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(getResources().getColor(R.color.chart_label))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(Math.round(Math.max(0f, minimumPrice - margin)), Math.round(maximumPrice + margin))
                .addData(lineSet);

        Animation anim = new Animation();

        lineChartView.show(anim);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case STOCK_LOADER:
                return new CursorLoader(this,
                                        QuoteProvider.Quotes.CONTENT_URI,
                                        new String[]{
                                            QuoteColumns.BIDPRICE,
                                        },
                                        QuoteColumns.SYMBOL + " = ?",
                                        new String[]{symbol},
                                        null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0)
            displayLineChart(data);
        else
            Toast.makeText(this, "No data to display!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        //chill
    }
}
