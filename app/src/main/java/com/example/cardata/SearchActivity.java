package com.example.cardata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private EditText cityNameEdit;
    private EditText yearEdit;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.activity_search);

        cityNameEdit = findViewById(R.id.CityNameEdit);
        yearEdit = findViewById(R.id.YearEdit);
        statusText = findViewById(R.id.StatusText);
        Button searchButton = findViewById(R.id.SearchButton);
        Button listInfoActivityButton = findViewById(R.id.ListInfoActivityButton);
        Button backButton = findViewById(R.id.BackToMainButton);


        searchButton.setOnClickListener(v -> {
            String city = cityNameEdit.getText().toString().trim();
            String yearStr = yearEdit.getText().toString().trim();

            if (city.isEmpty()) {
                statusText.setText("Search failed: City field cannot be empty");
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                getData(this, city, year);
            } catch (NumberFormatException e) {
                statusText.setText("Search failed: Year field must be a number");
            }
        });

        listInfoActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, ListInfoActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void getData(Context context, String city, int year) {
        statusText.setText("Searching");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                String apiUrl = "https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px";

                URL url = new URL(apiUrl);
                HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
                getConn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                StringBuilder getResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    getResponse.append(line);
                }
                reader.close();

                JSONObject metaData = new JSONObject(getResponse.toString());
                JSONArray variables = metaData.getJSONArray("variables");

                String areaCode = null;
                for (int i = 0; i < variables.length(); i++) {
                    JSONObject var = variables.getJSONObject(i);
                    if (var.getString("code").equals("Alue")) {
                        JSONArray values = var.getJSONArray("values");
                        JSONArray valueTexts = var.getJSONArray("valueTexts");

                        for (int j = 0; j < valueTexts.length(); j++) {
                            if (valueTexts.getString(j).equalsIgnoreCase(city)) {
                                areaCode = values.getString(j);
                                break;
                            }
                        }
                    }
                }

                if (areaCode == null) {
                    runOnUiThread(() -> statusText.setText("Search failed: No area was found"));
                    return;
                }

                String jsonInputString = "{\n" +
                        "  \"query\": [\n" +
                        "    {\"code\": \"Alue\", \"selection\": {\"filter\": \"item\", \"values\": [\"" + areaCode + "\"]}},\n" +
                        "    {\"code\": \"Ajoneuvoluokka\", \"selection\": {\"filter\": \"item\", \"values\": [\"01\", \"02\", \"03\", \"04\", \"05\"]}},\n" +
                        "    {\"code\": \"Liikennekäyttö\", \"selection\": {\"filter\": \"item\", \"values\": [\"0\"]}},\n" +
                        "    {\"code\": \"Vuosi\", \"selection\": {\"filter\": \"item\", \"values\": [\"" + year + "\"]}}\n" +
                        "  ],\n" +
                        "  \"response\": {\"format\": \"json-stat2\"}\n" +
                        "}";

                HttpURLConnection postConn = (HttpURLConnection) url.openConnection();
                postConn.setRequestMethod("POST");
                postConn.setRequestProperty("Content-Type", "application/json; utf-8");
                postConn.setRequestProperty("Accept", "application/json");
                postConn.setDoOutput(true);

                try (OutputStream os = postConn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                BufferedReader postReader = new BufferedReader(new InputStreamReader(postConn.getInputStream(), "utf-8"));
                StringBuilder postResponse = new StringBuilder();
                String postLine;
                while ((postLine = postReader.readLine()) != null) {
                    postResponse.append(postLine);
                }
                postReader.close();

                JSONObject dataObj = new JSONObject(postResponse.toString());
                JSONArray valuesArray = dataObj.getJSONArray("value");

                CarDataStorage storage = CarDataStorage.getInstance();
                storage.clearData();
                storage.setCity(city);
                storage.setYear(year);

                String[] ajoneuvoTyypit = {"Henkilöautot", "Pakettiautot", "Kuorma-autot", "Linja-autot", "Erikoisautot"};

                for (int i = 0; i < valuesArray.length() && i < ajoneuvoTyypit.length; i++) {
                    int amount = valuesArray.getInt(i);
                    storage.addCarData(new CarData(ajoneuvoTyypit[i], amount));
                }

                runOnUiThread(() -> statusText.setText("Search was succesful"));

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Search failed. Try different values."));
            }
        });
    }

}
