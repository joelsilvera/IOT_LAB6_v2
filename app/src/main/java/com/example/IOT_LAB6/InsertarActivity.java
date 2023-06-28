package com.example.IOT_LAB6;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.IOT_LAB6.Entity.Actividad;
import com.example.IOT_LAB6.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InsertarActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    StorageReference storageRef;
    FirebaseDatabase firebaseDatabase;
    Actividad actividad = new Actividad();
    List<Actividad> listaActividades = new ArrayList<>();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    final static LocalTime HORA_MIN = LocalTime.of(6,0);
    final static LocalTime HORA_MAX = LocalTime.of(23,30);

    EditText etFecha;
    EditText etHorainicio;
    EditText etHorafin;
    ImageView imageView;
    Button btnCrear;
    TextView mensaje;
    EditText titulo;
    Button btnSubirFoto;
    LocalTime timeInicio;
    LocalTime timeFin;
    EditText descripcion ;
    LocalDate selectedLocalDate;
    String imageUrl;
    LocalTime horaInbase;
    LocalTime horaFinbase;
    String updatedTitulo;
    Integer horaInicio = 6;
    Integer horaFin = 23;
    Integer minutoInicio = 0;
    Integer minutoFin = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar);
        Intent intent = getIntent();
        boolean updated = intent.hasExtra("activity");
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child(firebaseAuth.getCurrentUser().getUid());
        etFecha = findViewById(R.id.editTextDate);
        etHorainicio = findViewById(R.id.editTextHoraInicio);
        etHorafin = findViewById(R.id.editTextHoraFin);
        imageView = findViewById(R.id.ivInsertar);
        btnCrear = findViewById(R.id.btnCrearActividad);
        btnSubirFoto = findViewById(R.id.btnSubirImagen);
        titulo = findViewById(R.id.editTextTitulo);
        descripcion = findViewById(R.id.editTextDescripcion);
        mensaje = findViewById(R.id.textViewMensaje);

        if(updated){
            actividad = (Actividad) intent.getSerializableExtra("activity");
            timeInicio = LocalTime.parse(actividad.getHoraInicio(),timeFormatter);
            timeFin = LocalTime.parse(actividad.getHoraFin(),timeFormatter);
            btnCrear.setText("Actualizar actividad");
            btnSubirFoto.setText("Actualizar Foto");
            etFecha.setText(actividad.getFecha());
            etHorafin.setText(actividad.getHoraFin());
            etHorainicio.setText(actividad.getHoraInicio());
            descripcion.setText(actividad.getDescripcion());
            titulo.setText(actividad.getTitulo());
            updatedTitulo = actividad.getTitulo();
            Glide.with(InsertarActivity.this).load(actividad.getImagenUrl()).into(imageView);
            btnCrear.setEnabled(true);
            horaInicio = timeInicio.getHour();
            horaFin= timeFin.getHour();
            minutoInicio = timeInicio.getMinute();
            minutoFin = timeFin.getMinute();
            selectedLocalDate = LocalDate.parse(actividad.getFecha(),dateFormatter);
        }

        MaterialTimePicker pickerHoraInicio = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(horaInicio)
                .setMinute(minutoInicio)
                .setTitleText("Selecciona la hora de inicio")
                .build();
        pickerHoraInicio.addOnCancelListener(dialogInterface -> {
            timeInicio = null;
            etHorainicio.setText("");
        });
        pickerHoraInicio.addOnNegativeButtonClickListener(dialogInterface -> {
            timeInicio = null;
            etHorainicio.setText("");
        });
        pickerHoraInicio.addOnPositiveButtonClickListener(dialogInterface -> {
            timeInicio = LocalTime.of(pickerHoraInicio.getHour(),pickerHoraInicio.getMinute());
            if(timeInicio.isBefore(HORA_MIN) || timeInicio.isAfter(HORA_MAX)){
                Toast.makeText(this, "Solo se pueden agendar actividades entre las 6:00 am y las 11:30 pm", Toast.LENGTH_SHORT).show();
                timeInicio = null;
                etHorainicio.setText("");
                return;
            }
            if(selectedLocalDate.isEqual(LocalDate.now()) && !timeInicio.isAfter(LocalTime.now())){
                Toast.makeText(this, "Ingresa una hora posterior a la actual", Toast.LENGTH_SHORT).show();
                timeInicio = null;
                etHorainicio.setText("");
                return;
            }
            if (timeFin!=null && timeInicio.isAfter(timeFin)){
                Toast.makeText(this, "La hora de inicio no puede ser posterior a la hora fin", Toast.LENGTH_SHORT).show();
                timeInicio = null;
                etHorainicio.setText("");
                return;
            }
            etHorainicio.setText(timeInicio.format(timeFormatter));
            actividad.setHoraInicio(timeInicio.format(timeFormatter));
        });

        MaterialTimePicker pickerHoraFin= new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(horaFin)
                .setMinute(minutoFin)
                .setTitleText("Selecciona la hora de fin")
                .build();
        pickerHoraFin.addOnCancelListener(dialogInterface -> {
            timeFin = null;
            etHorafin.setText("");
        });
        pickerHoraFin.addOnNegativeButtonClickListener(dialogInterface -> {
            timeFin = null;
            etHorafin.setText("");
        });
        pickerHoraFin.addOnPositiveButtonClickListener(dialogInterface -> {
            timeFin = LocalTime.of(pickerHoraFin.getHour(),pickerHoraFin.getMinute());
            if(timeFin.isBefore(HORA_MIN) || timeFin.isAfter(HORA_MAX)){
                Toast.makeText(this, "Solo se pueden agendar actividades entre las 6:00 am y las 11:30 pm", Toast.LENGTH_SHORT).show();
                timeFin = null;
                etHorafin.setText("");
                return;
            }
            if(selectedLocalDate.isEqual(LocalDate.now()) && !timeFin.isAfter(LocalTime.now())){
                Toast.makeText(this, "Ingresa una hora posterior a la actual", Toast.LENGTH_SHORT).show();
                timeFin = null;
                etHorafin.setText("");
                return;
            }
            if (timeInicio!=null && timeFin.isBefore(timeInicio)){
                Toast.makeText(this, "La hora de fin debe ser posterior a la hora de inicio", Toast.LENGTH_SHORT).show();
                timeFin = null;
                etHorafin.setText("");
                return;
            }
            etHorafin.setText(timeFin.format(timeFormatter));
            actividad.setHoraFin(timeFin.format(timeFormatter));


            String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dtRf = firebaseDatabase.getReference().child(user).child("actividades");
            dtRf.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren() ){
                        if(d.getValue(Actividad.class).getFecha().equals(etFecha.getText().toString())){
                            Actividad actividad = d.getValue(Actividad.class);
                            actividad.setKey(d.getKey());
                            listaActividades.add(actividad);
                        }
                    }
                    if(!listaActividades.isEmpty()){
                        List<Actividad> listaCruza = new ArrayList<>();
                        for(Actividad activity: listaActividades){
                            if(actividad.getKey() == null || !actividad.getKey().equals(activity.getKey())){
                                horaInbase = LocalTime.parse(activity.getHoraInicio(),timeFormatter);
                                horaFinbase = LocalTime.parse(activity.getHoraFin(),timeFormatter);
                                if(!((timeInicio.isBefore(horaInbase) && timeFin.isBefore(horaInbase)) || (timeInicio.isAfter(horaFinbase) && timeFin.isAfter(horaFinbase)))){
                                    listaCruza.add(activity);
                                }
                            }
                        }
                        Log.d("msg",listaCruza.size()+"");
                        Log.d("msg",listaActividades.size()+"");

                        if (listaCruza.size()==0){
                            btnCrear.setEnabled(true);
                            mensaje.setText("El horario esta dispobible");
                            mensaje.setTextColor(Color.GREEN);
                            mensaje.setVisibility(View.VISIBLE);
                        }else{
                            String val = "";

                            for(Actividad act : listaCruza){
                                val += "Actividad: " +act.getTitulo() + " | hora: "+act.getHoraInicio()+" - "+act.getHoraFin()+"\n";

                            }
                            mensaje.setText("El horario no esta disponible\n"+ val);
                            mensaje.setTextColor(Color.RED);
                            mensaje.setVisibility(View.VISIBLE);
                            btnCrear.setEnabled(false);
                        }
                    }else{
                        btnCrear.setEnabled(true);
                    }

                }
            });
        });


        etFecha.setOnClickListener(view -> showDatePickerDialog());
        etHorainicio.setOnClickListener(view -> {
            if(etFecha.getText().toString().isEmpty()){
                etFecha.setError("Indica una fecha");
                Toast.makeText(InsertarActivity.this, "Indica una fecha", Toast.LENGTH_SHORT).show();
                return;
            }
            pickerHoraInicio.show(getSupportFragmentManager(),"HoraInicio");
        });
        etHorafin.setOnClickListener(view -> {
            if(etFecha.getText().toString().isEmpty()){
                etFecha.setError("Indica una fecha");
                Toast.makeText(InsertarActivity.this, "Indica una fecha", Toast.LENGTH_SHORT).show();
                return;
            }
            pickerHoraFin.show(getSupportFragmentManager(),"HoraFin");
        });

    }

    private void showDatePickerDialog(){
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                String dayStr = String.valueOf(day);
                String monthStr = String.valueOf(month+1);
                if(dayStr.length()==1){
                    dayStr = "0" + dayStr;
                }
                if(monthStr.length()==1){
                    monthStr = "0" + monthStr;
                }

                final String selectedDate = dayStr + "/" + monthStr + "/" + year;
                selectedLocalDate = LocalDate.parse(selectedDate, dateFormatter);
                if (!selectedLocalDate.isBefore(LocalDate.now())){
                    etFecha.setText(selectedDate);
                    actividad.setFecha(selectedDate);
                    etFecha.setError(null);
                }else{
                    etFecha.setText("");
                    Toast.makeText(InsertarActivity.this, "Ingresa una fecha posterior a la actual", Toast.LENGTH_SHORT).show();
                }
            }
        });
        newFragment.show(getSupportFragmentManager(),"datePicker");
    }


    public void subirImagen(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        launcherPhotos.launch(intent);
    }

    ActivityResultLauncher<Intent> launcherPhotos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    StorageReference child = storageRef.child(uri.getLastPathSegment());

                    child.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            child.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                imageUrl = uri1.toString();
                                actividad.setImagenUrl(imageUrl);
                                Log.d("msg-test", "ruta archivo: " + imageUrl);
                                updateImageView();
                            }).addOnFailureListener(e -> {
                                imageUrl = "";
                                updateImageView();
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("msg", "Fallo subida",e);
                            imageUrl = "";
                            updateImageView();
                        }
                    });
                } else {
                    Toast.makeText(InsertarActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                    imageUrl = "";
                    updateImageView();
                }
            }
    );

    public void updateImageView(){
        if(!imageUrl.isEmpty()){
            Glide.with(InsertarActivity.this).load(imageUrl).into(imageView);
        }else{
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    public void crearActividad(View view){
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(user).child("actividades");
        Intent intent = getIntent();
        boolean updated = intent.hasExtra("activity");
        String tituloString  =  titulo.getText().toString();
        String descripcionString = descripcion.getText().toString();
        if(tituloString.isEmpty() || descripcionString.isEmpty()){
            Toast.makeText(InsertarActivity.this, "Ni el título ni la descripción pueden ser vacíos", Toast.LENGTH_SHORT).show();
            return;
        }
        actividad.setTitulo(tituloString);
        actividad.setDescripcion(descripcionString);
        if(updated){
            databaseReference.child(actividad.getKey()).setValue(actividad);
            Toast.makeText(InsertarActivity.this, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show();
        }else{
            Log.d("user",user);
            String i = "1";
            databaseReference.push().setValue(actividad);
            Toast.makeText(InsertarActivity.this, "Actividad creada exitosamente", Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(InsertarActivity.this, AgendaActivity.class));
        finish();

    }
}

