package com.example.cardata;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        TextView cityText = findViewById(R.id.CityText);
        TextView yearText = findViewById(R.id.YearText);
        TextView carInfoText = findViewById(R.id.CarInfoText);
        Button backButton = findViewById(R.id.BackToSearchButton);


        CarDataStorage storage = CarDataStorage.getInstance();

        cityText.setText("City: " + storage.getCity());
        yearText.setText("Year: " + storage.getYear());

        StringBuilder infoBuilder = new StringBuilder();
        int totalCars = 0;

        ArrayList<CarData> dataList = storage.getCarData();
        for (CarData car : dataList) {
            infoBuilder.append(car.getType()).append(": ").append(car.getAmount()).append("\n");
            totalCars += car.getAmount();
        }

        if (dataList.isEmpty()) {
            infoBuilder.append("No data.\n");
        } else {
            infoBuilder.append("\nTotal amount: ").append(totalCars);
        }

        carInfoText.setText(infoBuilder.toString());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}
