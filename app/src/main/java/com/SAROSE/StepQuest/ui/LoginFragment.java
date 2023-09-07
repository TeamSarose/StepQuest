package com.SAROSE.StepQuest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.SAROSE.StepQuest.Database.DatabaseHandler;
import com.SAROSE.StepQuest.R;


public class LoginFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_tab, container, false);

        // Initialize views
        usernameEditText = view.findViewById(R.id.login_email);
        passwordEditText = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.loginButton);


        // Set onClickListener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from EditTexts
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty()) {
                    usernameEditText.setError("Username is required");
                    usernameEditText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }


                // Check if username exists and password is correct
                DatabaseHandler db = DatabaseHandler.getInstance(getContext());
                boolean userExists = db.checkUser(username);
                boolean passwordMatches = false;
                if (userExists) {
                    // Get the password for the given username from the database
                    String storedPassword = db.getUserPassword(username);
                    if (storedPassword.equals(password)) {
                        passwordMatches = true;
                    }
                }

                if (userExists && passwordMatches) {
                    // Navigate to the next activity
                    navigateToNextActivity();
                } else {
                    // Display an error message
                    Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }



    // Navigate to next fragment
    private void navigateToNextActivity() {
        Intent intent= new Intent(getActivity(),ActivityMain.class);
        startActivity(intent);
    }

}
