package com.example.spa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {


    MqttAndroidClient client;

    static String HOST = "tcp://192.168.0.110:1883";

    String topico = "ajuda";
    String mensagem = "";
    String msgRecebida = "";
    String topicoUnico = "";

    private FirebaseAuth auth;
    private FirebaseUser user;
    private AlertDialog alerta;
    Button btn_ajuda, btn_conversa;
    TextView texto;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    SimpleDateFormat formataData;
    private DatabaseReference pessoaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), HOST,
                clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        formataData = new SimpleDateFormat("dd-MM-yyyy");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Conectado", Toast.LENGTH_SHORT).show();
                    pessoaRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Pessoa pessoa = dataSnapshot.getValue(Pessoa.class);
                            topicoUnico = pessoa.getCpf();
                            Toast.makeText(MainActivity.this, topicoUnico, Toast.LENGTH_SHORT).show();
                            setSub(topicoUnico);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                msgRecebida = (new String(message.getPayload()));
                texto.setText(msgRecebida);
                Toast.makeText(MainActivity.this, msgRecebida, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        pessoaRef = firebaseDatabase.getReference("Pessoa");

        texto = findViewById(R.id.textView);
        btn_ajuda = findViewById(R.id.button_ajuda);
        btn_ajuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
                msgBox.setTitle("Pedindo Ajuda ...");
                msgBox.setMessage("Você realmente deseja pedir ajuda?");
                msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pessoaRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                Pessoa pessoa = dataSnapshot.getValue(Pessoa.class);
                                System.out.println(pessoa.getNome());
                                mensagem = pessoa.getNome() + ";" + pessoa.getRua()
                                        + ";" + pessoa.getNumero() + ";" + pessoa.getComplemento()
                                        + ";" + pessoa.getBairro() + ";" + pessoa.getCidade()
                                        + ";" + pessoa.getEstado() + ";" + pessoa.getTelefone() + ";" + pessoa.getCpf();
                                publicar();
                                Date data = new Date();
                                String dataFormatada = formataData.format(data);
                                databaseReference.child("Pessoa").child(pessoa.getUuid()).child("Pedidos de Ajuda").child(UUID.randomUUID().toString()).setValue("A " + pessoa.getNome()
                                        + " pediu ajuda no dia " + dataFormatada + ". No endereço " + pessoa.getRua() +
                                        ", " + pessoa.getNumero() + ", " + pessoa.getComplemento() + ", " + pessoa.getBairro() +
                                        ", " + pessoa.getCidade() + ", " + pessoa.getEstado());
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //Toast.makeText(MainActivity.this, "SIM", Toast.LENGTH_SHORT).show();
                    }
                });
                msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alerta = msgBox.create();
                alerta.show();
            }
        });
    }

    public void publicar() {

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = mensagem.getBytes();
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topico, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = Conexao.getFirebaseAuth();
        user = Conexao.getFirebaseUser();
        verificaUser();
    }

    private void verificaUser() {
        if (user == null) {
            finish();
        } else {
            //Toast.makeText(this, user.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }

    public void ajuda() {

    }

    public void setSub(String topicoDesejado) {
        try {
            client.subscribe(topicoDesejado, 1);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void sair(View view) {
        Conexao.logOut();
        finish();
    }
}
