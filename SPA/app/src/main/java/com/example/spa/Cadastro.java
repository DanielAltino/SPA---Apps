package com.example.spa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Cadastro extends AppCompatActivity {

    Button botaoCadastrar;
    EditText et_nome, et_telefone, et_rua, et_numero, et_complemento, et_bairro, et_cidade, et_estado, et_email, et_senha, et_cpf;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        FirebaseApp.initializeApp(Cadastro.this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        et_nome = findViewById(R.id.editText_nome);
        et_telefone = findViewById(R.id.editText_telefone);
        et_rua = findViewById(R.id.editText_rua);
        et_numero = findViewById(R.id.editText_numero);
        et_complemento = findViewById(R.id.editText_complemento);
        et_bairro = findViewById(R.id.editText_bairro);
        et_cidade = findViewById(R.id.editText_cidade);
        et_estado = findViewById(R.id.editText_estado);
        et_email = findViewById(R.id.editText_email);
        et_senha = findViewById(R.id.editText_senha);
        et_cpf = findViewById(R.id.editText_cpf);

        botaoCadastrar = findViewById(R.id.button_cadastrar);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString().trim();
                String senha = et_senha.getText().toString().trim();
                criarUser(email,senha);
            }
        });
    }

    private void criarUser(String email, String senha) {
        auth.createUserWithEmailAndPassword(email,senha)
                .addOnCompleteListener(Cadastro.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Cadastro.this, "Usuária Cadastrada com Sucesso", Toast.LENGTH_SHORT).show();

                            Pessoa p = new Pessoa();
                            p.setUuid(UUID.randomUUID().toString());
                            p.setNome(et_nome.getText().toString());
                            p.setBairro(et_bairro.getText().toString());
                            p.setCidade(et_cidade.getText().toString());
                            p.setComplemento(et_complemento.getText().toString());
                            p.setEmail(et_email.getText().toString());
                            p.setEstado(et_estado.getText().toString());
                            p.setNumero(et_numero.getText().toString());
                            p.setSenha(et_senha.getText().toString());
                            p.setRua(et_rua.getText().toString());
                            p.setTelefone(et_telefone.getText().toString());
                            p.setCpf(et_cpf.getText().toString());
                            databaseReference.child("Pessoa").child(p.getUuid()).setValue(p);

                            Intent i = new Intent(Cadastro.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else{
                            Toast.makeText(Cadastro.this, "Erro ao cadastrar.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void Entrar(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = Conexao.getFirebaseAuth();
    }
}
