package com.example.edsonfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edsonfirebase.Objetos.Contactos;
import com.example.edsonfirebase.Objetos.ReferenciasFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnGuardar;
    private Button btnListar;
    private Button btnLimpiar;
    private TextView txtNombre;
    private TextView txtDireccion;
    private TextView txtTelefono1;
    private TextView txtTelefono2;
    private TextView txtNotas;
    private CheckBox cbkFavorite;
    private FirebaseDatabase basedatabase;
    private DatabaseReference referencia;
    private Contactos savedContacto;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        setEvents();
    }
    public void initComponents(){
        // Se obtiene una instancia de la base de datos y se obtiene la referencia que apunta a la tabla de contactos
        this.basedatabase = FirebaseDatabase.getInstance();
        this.referencia = this.basedatabase.getReferenceFromUrl
                (ReferenciasFirebase.URL_DATABASE +
                        ReferenciasFirebase.DATABASE_NAME + "/" +
                        ReferenciasFirebase.TABLE_NAME);
        // ---------------------------------------------------------------------------------- \\
        this.txtNombre = findViewById(R.id.txtNombre);
        this.txtTelefono1 = findViewById(R.id.txtTelefono1);
        this.txtTelefono2 = findViewById(R.id.txtTelefono2);
        this.txtDireccion = findViewById(R.id.txtDireccion);
        this.txtNotas = findViewById(R.id.txtNotas);
        this.cbkFavorite = findViewById(R.id.cbxFavorito);
        this.btnGuardar = findViewById(R.id.btnGuardar);
        this.btnLimpiar = findViewById(R.id.btnLimpiar);
        this.btnListar = findViewById(R.id.btnListar);
        savedContacto = null;
    }

    public void setEvents(){
        this.btnGuardar.setOnClickListener(this);
        this.btnLimpiar.setOnClickListener(this);
        this.btnListar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        if(isNetworkAvailable()){
            switch (view.getId()) {
                case R.id.btnGuardar:
                    boolean completo = true;
                    if (txtNombre.getText().toString().equals("")) {
                        txtNombre.setError("Introduce el nombre");
                        completo = false;
                    }
                    if (txtTelefono1.getText().toString().equals("")) {
                        txtTelefono1.setError("Introduce el telefono principal");
                        completo = false;
                    }
                    if (txtDireccion.getText().toString().equals("")) {
                        txtDireccion.setError("Introduce la direccion");
                        completo = false;
                    }
                    if (completo) {
                        Contactos nContacto = new Contactos();
                        nContacto.setNombre(txtNombre.getText().toString());
                        nContacto.setTelefono1(txtTelefono1.getText().toString());
                        nContacto.setTelefono2(txtTelefono2.getText().toString());
                        nContacto.setDireccion(txtDireccion.getText().toString());
                        nContacto.setNotas(txtNotas.getText().toString());
                        nContacto.setFavorite(cbkFavorite.isChecked() ? 1 : 0);
                        if (savedContacto == null) {
                            agregarContacto(nContacto);
                            Toast.makeText(this, "Contacto guardado con exito", Toast.LENGTH_SHORT).show();
                            limpiar();
                        } else {
                            actualizarContacto(id, nContacto);
                            Toast.makeText(this, "Contacto actualizado con exito", Toast.LENGTH_SHORT).show();
                            limpiar();
                        }
                    }
                    break;

                case R.id.btnLimpiar:
                    limpiar();
                    break;

                case R.id.btnListar:
                    Intent i = new Intent(MainActivity.this, ListaActivity.class);
                    limpiar();
                    startActivityForResult(i, 0);
                    break;
            }
        }else{
            Toast.makeText(this, "Se necesita conexion a internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void agregarContacto(Contactos c){
        DatabaseReference newContactoReference = referencia.push();
        // Obtener el registro del ID y setearlo
        String id = newContactoReference.getKey();
        c.set_ID(id);
        newContactoReference.setValue(c);
    }

    public void actualizarContacto(String id, Contactos p){
        // Actualizar un objeto al nodo referencia
        p.set_ID(id);
        referencia.child(String.valueOf(id)).setValue(p);
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    public void limpiar(){
        savedContacto = null;
        txtNombre.setText("");
        txtTelefono1.setText("");
        txtTelefono2.setText("");
        txtNotas.setText("");
        txtDireccion.setText("");
        cbkFavorite.setChecked(false);
        id="";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
            Bundle oBundle = intent.getExtras();
            if(Activity.RESULT_OK == resultCode){
                Contactos contacto = (Contactos) oBundle.getSerializable("contacto");
                savedContacto = contacto;
                id = contacto.get_ID();
                txtNombre.setText(contacto.getNombre());
                txtTelefono1.setText(contacto.getTelefono1());
                txtTelefono2.setText(contacto.getTelefono2());
                txtDireccion.setText(contacto.getDireccion());
                txtNotas.setText(contacto.getNotas());
                if(contacto.getFavorite()>0){
                    cbkFavorite.setChecked(true);
                } else {
                    cbkFavorite.setChecked(false);
                }
            }
        }
    }
}