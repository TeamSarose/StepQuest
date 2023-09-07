package com.SAROSE.StepQuest.ui;


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


public class SignUpFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText cPassword;
    private Button signUpButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_tab, container, false);

        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        cPassword = view.findViewById(R.id.signup_confirm);
        signUpButton = view.findViewById(R.id.signUpButton);

        // Set onClickListener for sign up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from EditTexts
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = cPassword.getText().toString().trim();

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

                if (confirmPassword.isEmpty()) {
                    cPassword.setError("Password is required");
                    cPassword.requestFocus();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    cPassword.setError("Passwords do not match");
                    cPassword.requestFocus();
                    return;
                }

                DatabaseHandler databaseHandler= new DatabaseHandler(getContext());
                // Check if the username already exists in the database
                if(databaseHandler.checkUser(username)) {
                    // If the username already exists, shsow a message to the user
                    Toast.makeText(getContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // If the username doesn't exist, insert the new user into the database
                    databaseHandler.addUser(username, password);

                    // Show a message to the user that sign up was successful
                    Toast.makeText(getContext(), "Sign up successful, Head to the sign in page", Toast.LENGTH_SHORT).show();

                }

            }
        });

        return view;
    }
}
