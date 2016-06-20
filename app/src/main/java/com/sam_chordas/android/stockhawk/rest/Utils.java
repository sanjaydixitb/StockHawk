package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  private static final String KEY_QUERY = "query";
  private static final String KEY_COUNT = "count";
  private static final String KEY_RESULTS = "results";
  private static final String KEY_QUOTE = "quote";
  private static final String KEY_CHANGE = "Change";
  private static final String KEY_CHANGE_IN_PERCENTAGE = "ChangeinPercent";
  private static final String KEY_BID = "Bid";

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(KEY_QUERY);
        int count = Integer.parseInt(jsonObject.getString(KEY_COUNT));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(KEY_RESULTS)
              .getJSONObject(KEY_QUOTE);
          ContentProviderOperation operation = buildBatchOperation(jsonObject);
          if(operation != null)
            batchOperations.add(operation);
        } else{
          resultsArray = jsonObject.getJSONObject(KEY_RESULTS).getJSONArray(KEY_QUOTE);

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      try {
        String change = jsonObject.getString(KEY_CHANGE);
        builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(QuoteColumns.SYMBOL));
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(KEY_BID)));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                jsonObject.getString(KEY_CHANGE_IN_PERCENTAGE), true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
          builder.withValue(QuoteColumns.ISUP, 0);
        } else {
          builder.withValue(QuoteColumns.ISUP, 1);
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      //Other exceptions, return null;
      return null;
    }
    return builder.build();
  }
}
