package com.hpdp.carreralambda;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    TextView tvRespuesta;
    Button btnVerCarreras;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRespuesta = findViewById(R.id.tvRespuesta);
        btnVerCarreras = findViewById(R.id.btnVerCarreras);

        btnVerCarreras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConsumirWS();
            }
        });
    }

    public void ConsumirWS() {
        String url = "https://5m5ov8ryx0.execute-api.us-east-2.amazonaws.com/Produccion/";
        OkHttpClient cliente = new OkHttpClient();
        Request get = new Request.Builder()
                .url(url)
                .build();

        cliente.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(() ->
                        tvRespuesta.setText("Error al conectar con el servidor: " + e.toString())
                );
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        String respuestaJson = responseBody.string();

                        // Convertir la respuesta en un objeto JSON
                        JSONObject jsonResponse = new JSONObject(respuestaJson);

                        // Obtener el "body" y parsearlo como un JSONArray
                        JSONArray lista = new JSONArray(jsonResponse.getString("body"));

                        // Formatear el resultado
                        StringBuilder resultado = new StringBuilder();
                        for (int i = 0; i < lista.length(); i++) {
                            JSONObject carrera = lista.getJSONObject(i);
                            resultado.append("🏁 Carrera ID: ").append(carrera.getInt("id"))
                                    .append("\n📏 Distancia: ").append(carrera.getString("distance"))
                                    .append("\n🏆 Ganador: ").append(carrera.getString("ganador"))
                                    .append("\n🔚 Terminada: ").append(carrera.getBoolean("terminada"))
                                    .append("\n\nCorredores:\n");

                            JSONArray corredores = carrera.getJSONArray("corredores");
                            for (int j = 0; j < corredores.length(); j++) {
                                JSONObject corredor = corredores.getJSONObject(j);
                                resultado.append("👤 ").append(corredor.getString("nombre"))
                                        .append(" (ID: ").append(corredor.getInt("id"))
                                        .append(", 🚀 Velocidad: ").append(corredor.getInt("velocidad"))
                                        .append(", 📍 Posición: ").append(corredor.getInt("posicion"))
                                        .append(")\n");
                            }
                            resultado.append("\n-----------------\n");
                        }

                        // Mostrar la respuesta formateada en la UI
                        MainActivity.this.runOnUiThread(() -> {
                            tvRespuesta.setText(resultado.toString());
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(() ->
                            tvRespuesta.setText("Error al procesar la respuesta del servidor")
                    );
                }
            }
        });
    }

}