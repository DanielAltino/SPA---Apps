package com.example.spapolicial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spapolicial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    Button botaoLogin, botaoCadastrar;
    private FirebaseAuth auth;
    EditText et_email,et_senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        et_email = findViewById(R.id.editText_login);
        et_senha = findViewById(R.id.editText_password);
        botaoLogin = findViewById(R.id.button_sign_in);
        botaoCadastrar = findViewById(R.id.button_cadastrar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = Conexao.getFirebaseAuth();
    }

    public void Entrar(View view){

        String email = et_email.getText().toString().trim();
        String senha = et_senha.getText().toString().trim();
        login(email,senha);
    }

    private void login(String email, String senha) {
        auth.signInWithEmailAndPassword(email,senha)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent i = new Intent(Login.this, MainActivity.class);
                            startActivity(i);
                        }
                        else
                            Toast.makeText(Login.this, "Email ou senha inválido.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void Cadastrar(View view){
        Intent intent = new Intent(this, Cadastro.class);
        startActivity(intent);
    }
}
