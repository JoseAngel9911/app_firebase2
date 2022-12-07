package com.programacionandroid.app_firebase2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnBuscarV, btnAgregarV, btnModificarV, btnEliminarV;
    EditText txtIdV, txtNombreV, txtCorreoV, txtTelefonoV;
    ListView listaDatos;

    private FirebaseDatabase objFirebaseDatabase;
    private DatabaseReference objDatabaseReference;

    private List<Agenda> listadoAgenda = new ArrayList<Agenda>();
    ArrayAdapter<Agenda> arrayAdapterAgenda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle("Conexion con Firebase");

        //Campos de texto
        txtIdV = findViewById(R.id.txtId);
        txtNombreV= findViewById(R.id.txtNombre);
        txtTelefonoV = findViewById(R.id.txtTelefono);
        txtCorreoV = findViewById(R.id.txtCorreo);

        //Botones
        btnAgregarV = findViewById(R.id.btnRegistrar);
        btnBuscarV = findViewById(R.id.btnBuscar);
        btnModificarV = findViewById(R.id.btnModificar);
        btnEliminarV = findViewById(R.id.btnEliminar);

        //Listado
        listaDatos = findViewById(R.id.lvDatos);

        iniciarConexionFirebase();
        enlistarDatos();
        botonAgregar();
        botonBuscar();
        botonModificar();
        botonEliminar();
    }

    private void botonEliminar() {
        btnEliminarV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idBuscando = txtIdV.getText().toString().trim();

                if(idBuscando.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Ningun Id para eliminar", Toast.LENGTH_SHORT).show();
                    ocultarTeclado();
                    limpiarCampos();
                }else{

                    Agenda objAgenda = new Agenda(idBuscando);
                    objDatabaseReference.child("Agenda").child(objAgenda.getId()).removeValue();
                    Toast.makeText(getApplicationContext(), "Datos eliminados", Toast.LENGTH_SHORT).show();
                    ocultarTeclado();
                    limpiarCampos();
                }
            }
        });
    }

    private void botonModificar() {

        btnModificarV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idBuscando = txtIdV.getText().toString().trim();

                if(idBuscando.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Ningun Id fue ingresado para modificar", Toast.LENGTH_SHORT).show();
                    ocultarTeclado();
                    limpiarCampos();
                }else{
                    String nombre = txtNombreV.getText().toString().trim();
                    String correo = txtCorreoV.getText().toString().trim();
                    String telefono = txtTelefonoV.getText().toString().trim();

                    Agenda objAgenda = new Agenda(idBuscando, nombre, telefono, correo);

                    objDatabaseReference.child("Agenda").child(objAgenda.getId()).setValue(objAgenda);
                    Toast.makeText(getApplicationContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
                    ocultarTeclado();
                    limpiarCampos();
                }
            }
        });

    }

    private void botonBuscar() {

        btnBuscarV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idBuscando = txtIdV.getText().toString().trim();

                objDatabaseReference.child("Agenda").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot corredor : snapshot.getChildren()){
                            //System.out.println("corro ");
                            if(idBuscando.isEmpty()){
                                ocultarTeclado();
                                Toast.makeText(MainActivity.this, "Buscador vacio", Toast.LENGTH_SHORT).show();
                            }else{
                                if(idBuscando.equalsIgnoreCase(corredor.child("id").getValue().toString())){
                                    txtIdV.setText(corredor.child("id").getValue().toString());
                                    txtNombreV.setText(corredor.child("nombre").getValue().toString());
                                    txtTelefonoV.setText(corredor.child("telefono").getValue().toString());
                                    txtCorreoV.setText(corredor.child("correo").getValue().toString());
                                    ocultarTeclado();
                                    break;
                                }else{
                                    Toast.makeText(MainActivity.this, "Id " + idBuscando + " no existe", Toast.LENGTH_SHORT).show();
                                    ocultarTeclado();
                                    limpiarCampos();

                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void enlistarDatos() {

        objDatabaseReference.child("Agenda").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listadoAgenda.clear();
                for (DataSnapshot corredor : snapshot.getChildren()) {
                    Agenda objAgenda = corredor.getValue(Agenda.class);
                    //String aux = corredor.getValue();
                    //snapshot.getChildrenCount();
//                    System.out.println("Hola " + corredor.getValue());
                    listadoAgenda.add(objAgenda);

                    arrayAdapterAgenda = new ArrayAdapter<Agenda>(MainActivity.this, android.R.layout.simple_list_item_1, listadoAgenda);
                    listaDatos.setAdapter(arrayAdapterAgenda);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void iniciarConexionFirebase() {
        FirebaseApp.initializeApp(this);
        objFirebaseDatabase = FirebaseDatabase.getInstance();
        objDatabaseReference = objFirebaseDatabase.getReference();
    }

    public void botonAgregar(){

        btnAgregarV.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                //Variables que guardan dato de los campos de texto y se usaran para subir a la bd
                String id = txtIdV.getText().toString().trim();
                String nombre = txtNombreV.getText().toString().trim();
                String correo = txtCorreoV.getText().toString().trim();
                String telefono = txtTelefonoV.getText().toString().trim();

                if(id.isEmpty() || nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()){
                    ocultarTeclado();
                    Toast.makeText(getApplicationContext(), "Ingrese los datos faltantes", Toast.LENGTH_SHORT).show();
                }else{
                    String idBuscando = txtIdV.getText().toString().trim();
                    //boolean resp = false;
                    objDatabaseReference.child("Agenda").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean resp = false;
                            for(DataSnapshot corredor : snapshot.getChildren()){
                                if(corredor.child("id").getValue().toString().equalsIgnoreCase(idBuscando)){
                                    resp = true;
                                    ocultarTeclado();
                                    limpiarCampos();
                                    Toast.makeText(getApplicationContext(), "Id " + idBuscando + " ya se encuentra en uso", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            if(resp == false){
                                agregarDatosABD(id, nombre, correo, telefono);
                                limpiarCampos();
                                ocultarTeclado();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //int idaux = Integer.parseInt(id);

                }
            }
        });

    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    private void agregarDatosABD(String idaux, String nombre, String correo, String telefono) {

        //Crear obj de agenda
        Agenda objAgenda = new Agenda(idaux, nombre, telefono, correo);

        //Dato random para resguardar
        //String variableResguardo = UUID.randomUUID().toString();

        objDatabaseReference.child("Agenda").child(idaux).setValue(objAgenda);
        Toast.makeText(getApplicationContext(), "Ingreso de datos exitoso", Toast.LENGTH_SHORT).show();



    }

    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imn = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void limpiarCampos(){
        txtIdV.setText("");
        txtNombreV.setText("");
        txtCorreoV.setText("");
        txtTelefonoV.setText("");
    }

}