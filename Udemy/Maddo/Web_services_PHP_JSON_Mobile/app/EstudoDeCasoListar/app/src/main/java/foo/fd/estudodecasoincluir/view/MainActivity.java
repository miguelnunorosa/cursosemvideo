package foo.fd.estudodecasoincluir.view;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import foo.fd.estudodecasoincluir.R;
import foo.fd.estudodecasoincluir.controller.ControllerCidade;
import foo.fd.estudodecasoincluir.model.Cidade;
import foo.fd.estudodecasoincluir.model.Estado;

public class MainActivity extends AppCompatActivity {


    // CRUD - Só falta incluir

    Button btnListarEstados;
    Button btnListarCidades;
    Button btnDeletarCidade;
    // 4º Estudo de Caso - Alterar dados
    // Controller - Cidade - método pesquisa (nome) - obj
    Button btnAlterarCidade;
    Button btnPesquisar;
    Button btnIncluir;

    EditText editIdCidade;
    EditText editNomeCidade;
    TextView txtResultado;

    List<Cidade> cidadesList;
    List<Estado> estadosList;

    int idCidade;
    Cidade obj;

    String token;

    ControllerCidade controllerCidade;

    ListarCidadesAsyncTask listarCidadesAsyncTask;
    ListarEstadosAsyncTask listarEstadosAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        token = "fabricadedesenvolvedor";

        btnListarEstados = findViewById(R.id.btnListarEstados);
        btnListarCidades = findViewById(R.id.btnListarCidades);
        btnDeletarCidade = findViewById(R.id.btnDeletarCidade);
        btnAlterarCidade = findViewById(R.id.btnAlterarCidade);
        btnPesquisar = findViewById(R.id.btnPesquisar);
        btnIncluir = findViewById(R.id.btnIncluir);

        editIdCidade = findViewById(R.id.editIdCidade);
        editIdCidade.setEnabled(false);

        editNomeCidade = findViewById(R.id.editNomeCidade);

        txtResultado = findViewById(R.id.txtResultado);

        // Popula o Array com a lista de estados
        listarEstadosAsyncTask = new ListarEstadosAsyncTask(token);
        listarEstadosAsyncTask.execute();

        // Popula o Array com a lista de cidades
        listarCidadesAsyncTask = new ListarCidadesAsyncTask(token);
        listarCidadesAsyncTask.execute();

        btnIncluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent iIncluir =
                        new Intent(MainActivity.this, IncluirActivity.class);

                iIncluir.putExtra("estados", (Serializable) estadosList);
                startActivity(iIncluir);

            }
        });


        btnListarEstados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListarEstadosAsyncTask task = new ListarEstadosAsyncTask(token);
                task.execute();

            }
        });

        btnListarCidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cidadesList.clear();

                listarCidadesAsyncTask = new ListarCidadesAsyncTask(token);
                listarCidadesAsyncTask.execute();


            }
        });


        btnDeletarCidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                idCidade = Integer.parseInt(editIdCidade.getText().toString());

                DeletarCidadeAsyncTask task = new DeletarCidadeAsyncTask(token, idCidade);
                task.execute();

            }
        });

        btnPesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                obj = new Cidade();
                controllerCidade = new ControllerCidade();

                // Não estamos validadando as entrada de dados.

                obj = controllerCidade.buscarObjeto(cidadesList,editNomeCidade.getText().toString());

                try {

                    txtResultado.setText("Nome " + obj.getNome());

                    editIdCidade.setText(String.valueOf(obj.getId()));

                }catch (Exception e){

                    txtResultado.setText("Cidade Não localizada...");

                }


            }
        });


        btnAlterarCidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Altera o nome da cidade no Objeto Cidade
                obj.setNome(editNomeCidade.getText().toString());

                AlterarCidadeAsyncTask task =
                        new AlterarCidadeAsyncTask(token, obj);
                task.execute();

            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class ListarEstadosAsyncTask
            extends
            AsyncTask<String, String, String> {

        String api_token, query;

        HttpURLConnection conn;
        URL url = null;
        Uri.Builder builder;

        final String URL_WEB_SERVICES = "http://192.168.0.12/cursoudemy/APIListarEstados.php";

        final int READ_TIMEOUT = 10000; // MILISSEGUNDOS
        final int CONNECTION_TIMEOUT = 30000;

        int response_code;


        public ListarEstadosAsyncTask(String token) {

            this.api_token = token;
            this.builder = new Uri.Builder();
            builder.appendQueryParameter("api_token", api_token);

        }

        @Override
        protected void onPreExecute() {

            Log.i("APIListar", "onPreExecute()");

        }

        @Override
        protected String doInBackground(String... strings) {

            Log.i("APIListar", "doInBackground()");

            // Gerar o conteúdo para a URL

            try {

                url = new URL(URL_WEB_SERVICES);

            } catch (MalformedURLException e) {

                Log.i("APIListar", "MalformedURLException --> " + e.getMessage());

            } catch (Exception e) {

                Log.i("APIListar", "doInBackground() --> " + e.getMessage());
            }

            // Gerar uma requisição HTTP - POST - Result será um ArrayJson

            // conn

            try {

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("charset", "utf-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "HttpURLConnection --> " + e.getMessage());

            }

            // Adicionar o TOKEN e/ou outros parâmetros como por exemplo
            // um objeto a ser incluido, deletado ou alterado.
            // CRUD completo

            try {

                query = builder.build().getEncodedQuery();

                OutputStream stream = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(stream, "utf-8"));

                writer.write(query);
                writer.flush();
                writer.close();
                stream.close();

                conn.connect();


            } catch (Exception e) {

                Log.i("APIListar", "BufferedWriter --> " + e.getMessage());


            }

            // receber o response - arrayJson
            // http - código do response | 200 | 404 | 503

            try {

                response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(

                            new InputStreamReader(input)

                    );

                    StringBuilder result = new StringBuilder();

                    String linha = null;

                    while ((linha = reader.readLine()) != null) {

                        result.append(linha);
                    }

                    return result.toString();

                } else {

                    return "HTTP ERRO: " + response_code;
                }


            } catch (Exception e) {

                Log.i("APIListar", "StringBuilder --> " + e.getMessage());

                return "Exception Erro: " + e.getMessage();

            } finally {

                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            Log.i("APIListar", "onPostExecute()--> Result: " + result);

            try {

                Estado estado;

                JSONArray jsonArray = new JSONArray(result);

                estadosList = new ArrayList<>();

                if (jsonArray.length() != 0) {

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        estado = new Estado(jsonObject.getInt("id"),
                                jsonObject.getString("sigla"),
                                jsonObject.getString("nome"));

                        estadosList.add(estado);

                        Log.i("APIListar", "Estado: -> " + estado.getId() + " - " + estado.getSigla() + " - " + estado.getNome());


                    }

                }

            } catch (Exception e) {


                Log.i("APIListar", "onPostExecute()--> " + e.getMessage());


            }


        }
    }


    public class ListarCidadesAsyncTask
            extends
            AsyncTask<String, String, String> {

        String api_token, query;

        HttpURLConnection conn;
        URL url = null;
        Uri.Builder builder;

        final String URL_WEB_SERVICES = "http://192.168.0.12/cursoudemy/APIListarCidades.php";

        final int READ_TIMEOUT = 10000;
        final int CONNECTION_TIMEOUT = 30000;

        int response_code;

        public ListarCidadesAsyncTask(String token) {

            this.api_token = token;
            this.builder = new Uri.Builder();
            builder.appendQueryParameter("api_token", api_token);

        }

        @Override
        protected void onPreExecute() {

            Log.i("APIListar", "onPreExecute()");

        }

        @Override
        protected String doInBackground(String... strings) {

            Log.i("APIListar", "doInBackground()");

            // Gerar o conteúdo para a URL

            try {

                url = new URL(URL_WEB_SERVICES);

            } catch (MalformedURLException e) {

                Log.i("APIListar", "MalformedURLException --> " + e.getMessage());

            } catch (Exception e) {

                Log.i("APIListar", "doInBackground() --> " + e.getMessage());
            }

            // Gerar uma requisição HTTP - POST - Result será um ArrayJson

            // conn

            try {

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("charset", "utf-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "HttpURLConnection --> " + e.getMessage());

            }

            // Adicionar o TOKEN e/ou outros parâmetros como por exemplo
            // um objeto a ser incluido, deletado ou alterado.
            // CRUD completo

            try {

                query = builder.build().getEncodedQuery();

                OutputStream stream = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(stream, "utf-8"));

                writer.write(query);
                writer.flush();
                writer.close();
                stream.close();

                conn.connect();


            } catch (Exception e) {

                Log.i("APIListar", "BufferedWriter --> " + e.getMessage());


            }

            // receber o response - arrayJson
            // http - código do response | 200 | 404 | 503

            try {

                response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(

                            new InputStreamReader(input)

                    );

                    StringBuilder result = new StringBuilder();

                    String linha = null;

                    while ((linha = reader.readLine()) != null) {

                        result.append(linha);
                    }

                    return result.toString();

                } else {

                    return "HTTP ERRO: " + response_code;
                }


            } catch (Exception e) {

                Log.i("APIListar", "StringBuilder --> " + e.getMessage());

                return "Exception Erro: " + e.getMessage();

            } finally {

                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            Log.i("APIListar", "onPostExecute()--> Result: " + result);

            try {

                Cidade cidade;

                JSONArray jsonArray = new JSONArray(result);

                if (jsonArray.length() != 0) {

                    cidadesList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        cidade = new Cidade();

                        cidade.setId(jsonObject.getInt("id"));
                        cidade.setNome(jsonObject.getString("nome"));

                        cidadesList.add(cidade);

                        Log.i("APIListar", cidade.toString(jsonObject.getString("sigla")));


                    }

                }

            } catch (Exception e) {


                Log.i("APIListar", "onPostExecute()--> " + e.getMessage());


            }


        }
    }


    public class DeletarCidadeAsyncTask
            extends
            AsyncTask<String, String, String> {

        String api_token, query;

        HttpURLConnection conn;
        URL url = null;
        Uri.Builder builder;

        final String URL_WEB_SERVICES = "http://192.168.0.12/cursoudemy/APIDeletarCidade.php";

        final int READ_TIMEOUT = 10000;
        final int CONNECTION_TIMEOUT = 30000;

        int response_code;

        public DeletarCidadeAsyncTask(String token, int idCidade) {

            this.api_token = token;
            this.builder = new Uri.Builder();
            builder.appendQueryParameter("api_token", api_token);
            builder.appendQueryParameter("api_idCidade", String.valueOf(idCidade));

        }

        @Override
        protected void onPreExecute() {

            Log.i("APIListar", "onPreExecute()");

        }

        @Override
        protected String doInBackground(String... strings) {

            Log.i("APIListar", "doInBackground()");

            // Gerar o conteúdo para a URL

            try {

                url = new URL(URL_WEB_SERVICES);

            } catch (MalformedURLException e) {

                Log.i("APIListar", "MalformedURLException --> " + e.getMessage());

            } catch (Exception e) {

                Log.i("APIListar", "doInBackground() --> " + e.getMessage());
            }

            // Gerar uma requisição HTTP - POST - Result será um ArrayJson

            // conn

            try {

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("charset", "utf-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "HttpURLConnection --> " + e.getMessage());

            }

            // Adicionar o TOKEN e/ou outros parâmetros como por exemplo
            // um objeto a ser incluido, deletado ou alterado.
            // CRUD completo

            try {

                query = builder.build().getEncodedQuery();

                OutputStream stream = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(stream, "utf-8"));

                writer.write(query);
                writer.flush();
                writer.close();
                stream.close();

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "BufferedWriter --> " + e.getMessage());

            }

            // receber o response - arrayJson
            // http - código do response | 200 | 404 | 503

            try {

                response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(

                            new InputStreamReader(input)

                    );

                    StringBuilder result = new StringBuilder();

                    String linha = null;

                    while ((linha = reader.readLine()) != null) {

                        result.append(linha);
                    }

                    return result.toString();

                } else {

                    return "HTTP ERRO: " + response_code;
                }

            } catch (Exception e) {

                Log.i("APIListar", "StringBuilder --> " + e.getMessage());

                return "Exception Erro: " + e.getMessage();

            } finally {

                conn.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) { // Objeto Json

            Log.i("APIListar", "onPostExecute()--> Result: " + result);

            try {

                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.getBoolean("deletado")) {

                    txtResultado.setText("Registro Deletado: "+idCidade);

                    Log.i("APIListar", "onPostExecute()--> Deletado com Sucesso");

                }else{

                    txtResultado.setText("Falha ao Deletar: "+idCidade);

                    Log.i("APIListar", "onPostExecute()--> Falha ao Deletar");
                    Log.i("APIListar", "onPostExecute()--> "+jsonObject.getString("SQL"));

                }



            }catch (Exception e){

                Log.i("APIListar", "onPostExecute()--> " + e.getMessage());
            }

        }
    }


    public class AlterarCidadeAsyncTask
            extends
            AsyncTask<String, String, String> {

        String api_token, query;

        HttpURLConnection conn;
        URL url = null;
        Uri.Builder builder;

        final String URL_WEB_SERVICES = "http://192.168.0.12/cursoudemy/APIAlterarCidade.php";

        final int READ_TIMEOUT = 10000;
        final int CONNECTION_TIMEOUT = 30000;

        int response_code;

        // Contrutor recebe um objeto Cidade
        public AlterarCidadeAsyncTask(String token, Cidade obj) {

            this.api_token = token;
            this.builder = new Uri.Builder();
            builder.appendQueryParameter("api_token", api_token);
            builder.appendQueryParameter("api_idCidade", String.valueOf(obj.getId()));
            builder.appendQueryParameter("api_nome", String.valueOf(obj.getNome()));

        }

        @Override
        protected void onPreExecute() {

            Log.i("APIListar", "onPreExecute()");

        }

        @Override
        protected String doInBackground(String... strings) {

            Log.i("APIListar", "doInBackground()");

            // Gerar o conteúdo para a URL

            try {

                url = new URL(URL_WEB_SERVICES);

            } catch (MalformedURLException e) {

                Log.i("APIListar", "MalformedURLException --> " + e.getMessage());

            } catch (Exception e) {

                Log.i("APIListar", "doInBackground() --> " + e.getMessage());
            }

            // Gerar uma requisição HTTP - POST - Result será um ArrayJson

            // conn

            try {

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("charset", "utf-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "HttpURLConnection --> " + e.getMessage());

            }

            // Adicionar o TOKEN e/ou outros parâmetros como por exemplo
            // um objeto a ser incluido, deletado ou alterado.
            // CRUD completo

            try {

                query = builder.build().getEncodedQuery();

                OutputStream stream = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(stream, "utf-8"));

                writer.write(query);
                writer.flush();
                writer.close();
                stream.close();

                conn.connect();

            } catch (Exception e) {

                Log.i("APIListar", "BufferedWriter --> " + e.getMessage());

            }

            // receber o response - arrayJson
            // http - código do response | 200 | 404 | 503

            try {

                response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(

                            new InputStreamReader(input)

                    );

                    StringBuilder result = new StringBuilder();

                    String linha = null;

                    while ((linha = reader.readLine()) != null) {

                        result.append(linha);
                    }

                    return result.toString();

                } else {

                    return "HTTP ERRO: " + response_code;
                }

            } catch (Exception e) {

                Log.i("APIListar", "StringBuilder --> " + e.getMessage());

                return "Exception Erro: " + e.getMessage();

            } finally {

                conn.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) { // Objeto Json

            Log.i("APIListar", "onPostExecute()--> Result: " + result);

            try {

                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.getBoolean("alterado")) {

                    txtResultado.setText("Registro Alterado: "+obj.getId()+" "+obj.getNome());

                    Log.i("APIListar", "onPostExecute()--> Alterado com Sucesso");

                }else{

                    txtResultado.setText("Falha ao Alterar: "+obj.getId());

                    Log.i("APIListar", "onPostExecute()--> Falha ao Alterar");
                    Log.i("APIListar", "onPostExecute()--> "+jsonObject.getString("SQL"));

                }


            }catch (Exception e){

                Log.i("APIListar", "onPostExecute()--> " + e.getMessage());
            }

        }
    }

}
