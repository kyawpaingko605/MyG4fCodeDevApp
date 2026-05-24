package com.aistudiopro.g4fdev;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aistudiopro.g4fdev.network.G4FClient;

public class MainActivity extends AppCompatActivity {

    private EditText etCodePrompt;
    private Button btnGenerateCode;
    private TextView tvCodeOutput;
    private ProgressBar progressSpinner;
    private G4FClient g4fClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCodePrompt = findViewById(R.id.etCodePrompt);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        tvCodeOutput = findViewById(R.id.tvCodeOutput);
        progressSpinner = findViewById(R.id.progressSpinner);

        g4fClient = new G4FClient();

        btnGenerateCode.setOnClickListener(v -> {
            String prompt = etCodePrompt.getText().toString().trim();
            if (!prompt.isEmpty()) {
                startG4FGeneration(prompt);
            } else {
                Toast.makeText(MainActivity.this, "ကျေးဇူးပြု၍ တည်ဆောက်လိုသော Code ၏ Logic ကို ရေးပါဗျာ။", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startG4FGeneration(String prompt) {
        progressSpinner.setVisibility(View.VISIBLE);
        btnGenerateCode.setEnabled(false);
        tvCodeOutput.setText("G4F Engine မှ ကုဒ်များကို တည်ဆောက်နေပါသည်... ခေတ္တစောင့်ဆိုင်းပေးပါ...");

        g4fClient.generateCode(prompt, new G4FClient.G4FCallback() {
            @Override
            public void onSuccess(String responseText) {
                progressSpinner.setVisibility(View.GONE);
                btnGenerateCode.setEnabled(true);
                tvCodeOutput.setText(responseText);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressSpinner.setVisibility(View.GONE);
                btnGenerateCode.setEnabled(true);
                tvCodeOutput.setText("အမှားအယွင်း ဖြစ်ပေါ်ခဲ့သည်:\n" + errorMessage);
                Toast.makeText(MainActivity.this, "G4F Connection Failed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
