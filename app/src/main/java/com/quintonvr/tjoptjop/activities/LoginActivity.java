package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.network.CheckNetwork;
import com.quintonvr.tjoptjop.network.LoginTask;

public class LoginActivity extends AppCompatActivity {

    private EditText mCustomerCodeInput, mActivationInput;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCustomerCodeInput = findViewById(R.id.editText_customerCode);
        mActivationInput = findViewById(R.id.editText_activation_token);
        mLogin = findViewById(R.id.login_button);

        mLogin.setOnClickListener(v -> {
            String customerCode = mCustomerCodeInput.getText().toString().trim();
            String activationToken = mActivationInput.getText().toString().trim();

            if (customerCode.isEmpty()) {
               mCustomerCodeInput.requestFocus();
               mCustomerCodeInput.setError("Field cannot be empty");
            } else if (activationToken.isEmpty()) {
                mActivationInput.requestFocus();
                mActivationInput.setError("Field cannot be empty");
            } else {
                if (CheckNetwork.isInternetAvailable(this))
                    new LoginTask(this, customerCode, activationToken).execute();
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                            .setTitle("Oops!")
                            .setMessage("No internet connection.")
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .setCancelable(false);
                    dialog.show();
                }

            }
        });
    }

}
