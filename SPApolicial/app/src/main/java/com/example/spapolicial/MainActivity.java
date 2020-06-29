package com.example.spapolicial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;
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
    String topicoUnico = "";
    String mensagem = "";
    String msgRecebida = "";
    ArrayList<String> pedidos;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private AlertDialog alerta;
    Button btn_ajuda, btn_conversa;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    SimpleDateFormat formataData;
    TextView tv;
    ListView lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        tv = findViewById(R.id.textView2);
        lista = findViewById(R.id.lista);
        pedidos = new ArrayList<>();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pedidos);
        lista.setAdapter(arrayAdapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
                msgBox.setTitle("Enviando Ajuda");
                msgBox.setMessage("Você deseja encaminhar ajuda para o local?");
                msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lista.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.item_selecionado));
                        lista.getChildAt(position).setEnabled(Boolean.FALSE);
                        mensagem = "A ajuda está a caminho";
                        publicar();
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
                    setSub();
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
                System.out.println(msgRecebida);
                pedidos.add(mensagem(msgRecebida));
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    public String mensagem(String msg) {
        String[] textoSeparado = msg.split(";");
        String nome = textoSeparado[0];
        String rua = textoSeparado[1];
        String numero = textoSeparado[2];
        String complemento = textoSeparado[3];
        String bairro = textoSeparado[4];
        String cidade = textoSeparado[5];
        String estado = textoSeparado[6];
        String telefone = textoSeparado[7];
        String cpf = textoSeparado[8];

        topicoUnico = cpf;

        return ("A senhorita " + nome + " está pedindo ajuda.\nSeu endereço é " + rua + ", " + numero + ", "
                + complemento + ", "+ bairro+", "+cidade+", "+ estado+"\nO telefone dela é: "+telefone);
    }

    private ArrayList<String> preencherDados() {
        ArrayList<String> dados = new ArrayList<String>();
        dados.add(msgRecebida);
        return dados;
    }

    public void publicar() {

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = mensagem.getBytes();
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topicoUnico, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void setSub() {
        try {
            client.subscribe(topico, 1);

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
        }
    }

    public void ajuda() {

    }


    public void sair(View view) {
        Conexao.logOut();
        finish();
    }
}
