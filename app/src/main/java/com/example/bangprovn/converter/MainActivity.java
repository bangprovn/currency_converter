package com.example.bangprovn.converter;

/**
 * Created by bangprovn on 21-04-2018.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.bangprovn.converter.Utils.ConversionUtils;
import com.example.bangprovn.converter.Utils.GetFactorUtils;


public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

    private static final String TAG = "Converter";

    /**
     * How to add new currency:
     * Added new abbreviation of the language to the variable spinnerList.
     * Language must be supported by apilayer
     */

    TextView out_text;
    EditText in_text;
    String fromCurrency, toCurrency;
    String spinnerList[] = {"USD","EUR","JPY","GBP","CHF","CAD",
            "AUD","INR","CNY","AED","SGD","RUB","VND"};
    Spinner fromSpinner, toSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        out_text = findViewById(R.id.textView_output);
        in_text = findViewById(R.id.editText);

        if (savedInstanceState != null) {
            out_text.setText(savedInstanceState.getString("textviewstate"));
        }

        fromSpinner = findViewById(R.id.fromSpinner);
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        fromSpinner.setOnItemSelectedListener(this);

        toSpinner = findViewById(R.id.toSpinner);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        toSpinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textviewstate", String.valueOf(out_text.getText()));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onButtonClick(View view) {
        new urlOperation().execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.fromSpinner:
                fromSpinner.setSelection(position);
                fromCurrency = (String) fromSpinner.getSelectedItem();
                break;
            case R.id.toSpinner:
                toSpinner.setSelection(position);
                toCurrency = (String) toSpinner.getSelectedItem();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public class urlOperation extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String currencyQuery = null;
            try {
                final String BASE_URL = "http://apilayer.net/api/live";
                final String ACCESS_KEY = "?access_key=80f79d74a364a876d01fb40377b355bd&source=";
                final String CURRENCY = "&currencies=";
                final String FORMAT = "&format=1";

                /**
                 * The Free Plan of the API only support 1,000 requests per hour and the source
                 * converter is USD, so we must have a workaround for this limitation.
                 */


                URL url;
                if(fromCurrency.equals("USD"))  {
                    url = new URL(BASE_URL + ACCESS_KEY + fromCurrency + CURRENCY +
                            toCurrency + FORMAT);
                } else if(toCurrency.equals("USD"))   {
                    url = new URL(BASE_URL + ACCESS_KEY + toCurrency + CURRENCY +
                            fromCurrency + FORMAT);
                } else {
                    url = new URL(BASE_URL + ACCESS_KEY + "USD" + CURRENCY +
                            fromCurrency + "," + toCurrency + FORMAT);
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream =  urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                }

                currencyQuery = buffer.toString();

                if(fromCurrency.equals("USD")) {
                    // USD to any other currency
                    GetFactorUtils.conversionFactor = GetFactorUtils.getFactor(currencyQuery,
                            fromCurrency, toCurrency,0);
                }
                else if(toCurrency.equals("USD")) {
                    // Other currencies to USD
                    GetFactorUtils.conversionFactor = GetFactorUtils.getFactor(currencyQuery,
                            toCurrency, fromCurrency,1);
                }
                else {
                    // Neither currency is USD
                    GetFactorUtils.conversionFactor = GetFactorUtils.getFactor(currencyQuery,
                            toCurrency, fromCurrency,2);
                }

            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            double value = Float.parseFloat(in_text.getText().toString());
            double factor = GetFactorUtils.getJsonQuery();
            out_text.setText(BigDecimal.valueOf(ConversionUtils.convertCurrency(value, factor))
                    .setScale(0, RoundingMode.HALF_EVEN).toPlainString());
        }
    }
}