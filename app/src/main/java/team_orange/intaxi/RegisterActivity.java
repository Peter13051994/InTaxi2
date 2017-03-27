package team_orange.intaxi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword, editTextConfirmPassword,edittTextName,editTextICO;
    private Button buttonSignup;
    private TextView textViewSignin, textViewPasswordWarning, textViewConfirmedPasswordWarning;
    private ProgressDialog progressDialog;
    private ImageView imageViewPassword;

    DatabaseReference mRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mDriverRef=mRootRef.child("drivers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfirmPassword=(EditText)findViewById(R.id.editTextConfirmPassword);
        edittTextName=(EditText)findViewById(R.id.editTextName);
        editTextICO=(EditText)findViewById(R.id.editTextICO);

        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);
        textViewPasswordWarning = (TextView) findViewById(R.id.textViewPasswordWarning);
        textViewConfirmedPasswordWarning = (TextView) findViewById(R.id.textViewConfirmedPasswordWarning);
        imageViewPassword=(ImageView)findViewById(R.id.imageViewPassword);
        progressDialog = new ProgressDialog(this);

        buttonSignup.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);

        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    String phrase=editTextPassword.getText().toString();
                    onPasswordChange(phrase);
                }
            }
        });
        editTextConfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String password = editTextPassword.getText().toString();
                String confirmedPassword = editTextConfirmPassword.getText().toString();

                if (!password.equals(confirmedPassword)&& confirmedPassword.length() != 0) {
                    textViewConfirmedPasswordWarning.setVisibility(View.VISIBLE);
                    textViewConfirmedPasswordWarning.setText("Heslá sa nezhodujú!");
                } else
                    textViewConfirmedPasswordWarning.setVisibility(View.GONE);
            }

        });
    }

    private void registerUser(){
        if(!isOnline())
        {
            Toast.makeText(this,"No internet connection!",Toast.LENGTH_LONG).show();
            return;
        }
        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this,"Successfully registered",Toast.LENGTH_LONG).show();
                            String company=edittTextName.getText().toString();
                            String ico=editTextICO.getText().toString();
                            FirebaseUser user = task.getResult().getUser();
                            mDriverRef.child(user.getUid()).child("Email").setValue(user.getEmail());
                            mDriverRef.child(user.getUid()).child("Company_name").setValue(company);
                            mDriverRef.child(user.getUid()).child("Company_ico").setValue(ico);
                        }else{
                            Toast.makeText(RegisterActivity.this,"Registration Error",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if(view == buttonSignup){
            registerUser();
        }

        if(view == textViewSignin){
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private boolean onPasswordChange(String phrase) {
        StringBuilder sb = new StringBuilder();

        if (!phrase.matches(".*\\d+.*")) {
            sb.append("Password must contains number!\n");

        }
        if (phrase.equals(phrase.toLowerCase())) {
            sb.append("Password must contains upper case!\n");
        }
        if (phrase.length()<6) {
            sb.append("Password must contains minimal 6 characters!\n");
        }
        String message=sb.toString();
        if(message.length()!=0) {
            textViewPasswordWarning.setVisibility(View.VISIBLE);
            imageViewPassword.setBackground(getResources().getDrawable(R.mipmap.ic_uncheck));

        }
        else
        {
            textViewPasswordWarning.setVisibility(View.GONE);
            imageViewPassword.setBackground(getResources().getDrawable(R.mipmap.ic_check));
        }

        textViewPasswordWarning.setText(message);
        return true;
    }
    public  boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
