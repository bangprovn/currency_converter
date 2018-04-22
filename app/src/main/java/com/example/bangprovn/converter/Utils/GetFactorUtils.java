package com.example.bangprovn.converter.Utils;

/**
 * Created by bangprovn on 21-04-2018.
 */

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GetFactorUtils {
    private static final String TAG = "FactorUtils";


    public static double conversionFactor = 0;


    public static double getJsonQuery() {

        return conversionFactor;

    }

    public static double getFactor(String currencyQuery, String fromCurrency, String toCurrency,
                                   int type) throws JSONException {
        double conversionFactor = 0;

        try {

            JSONObject currencyQueryObject = new JSONObject(currencyQuery);
            JSONObject quotesObject = currencyQueryObject.getJSONObject("quotes");

            switch (type)   {
                case 0:
                    conversionFactor = quotesObject.getDouble(fromCurrency + toCurrency);
                    break;
                case 1:
                    conversionFactor = 1 / (quotesObject.getDouble(fromCurrency + toCurrency));
                    break;
                case 2:
                    /*
                    The USD to Currency conversion value for the two required currencies is
                    obtained and these values are used to obtain the conversion factor between
                    the two required currencies
                     */
                    double tempFrom = quotesObject.getDouble("USD" + fromCurrency);
                    double tempTo = quotesObject.getDouble("USD" + toCurrency);
                    conversionFactor = tempFrom / tempTo;
            }


        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return conversionFactor;

    }
}