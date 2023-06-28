package com.example.IOT_LAB6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.IOT_LAB6.Adapter.ActividadAdapter;
import com.example.IOT_LAB6.Entity.Actividad;
import com.example.IOT_LAB6.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AgendaActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;
    ProgressBar progressBar;
    LinearLayout emptyView;

    //DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    private LocalDate filtroFechaInicio = LocalDate.now();
    private LocalDate filtroFechaFin = LocalDate.now();
    private LocalTime filtroHoraInicio = LocalTime.of(6,0);
    private LocalTime filtroHoraFin = LocalTime.of(11,30);

    private List<Actividad> firebaseActividades = new ArrayList<>();
    private ArrayList<Actividad> actividadesFiltradas = new ArrayList<>();
    ActividadAdapter actividadAdapter;

    private ModalBottomSheet modalBottomSheet = new ModalBottomSheet();

    boolean primeraVez = true;
    int i;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.filterMenu:
                Log.d("msg", "llevarlo a filtros");
                modalBottomSheet.show(getSupportFragmentManager(), modalBottomSheet.getTag());
                return true;
            case R.id.logoutMenu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AgendaActivity.this, OnboardingMainActivity.class));
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        setTitle("Mi agenda");

        progressBar = findViewById(R.id.pbAgenda);
        emptyView = findViewById(R.id.llEmptyView);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAct);
        actividadAdapter = new ActividadAdapter(this,actividadesFiltradas);
        recyclerView.setAdapter(actividadAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AgendaActivity.this));

        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference().child(FirebaseAuth.getInstance().getUid()).child("actividades");
    }

    @Override
    protected void onResume() {
        cargarDatosdeFirebase();
        super.onResume();
    }

    public void goToInsertarActivity(View view){
        startActivity(new Intent(AgendaActivity.this, InsertarActivity.class));
    }

    public void filtrarActividades(){
        actividadesFiltradas.clear();
        LocalDate fechaActividad;
        LocalTime horaInicioActividad;
        LocalTime horaFinActividad;
        for (Actividad a : firebaseActividades){
            fechaActividad = LocalDate.parse(a.getFecha(),dateFormatter);
            horaInicioActividad = LocalTime.parse(a.getHoraInicio(),timeFormatter);
            horaFinActividad = LocalTime.parse(a.getHoraFin(),timeFormatter);
            if(fechaActividad.isEqual(filtroFechaInicio) && !horaInicioActividad.isBefore(filtroHoraInicio)){
                actividadesFiltradas.add(a);
                Log.d("msg", a.getTitulo());
            } else if(fechaActividad.isAfter(filtroFechaInicio) && fechaActividad.isBefore(filtroFechaFin)){
                actividadesFiltradas.add(a);
                Log.d("msg", a.getTitulo());
            } else if(!filtroFechaFin.isEqual(filtroFechaInicio) && fechaActividad.isEqual(filtroFechaFin) && !horaFinActividad.isAfter(filtroHoraFin)){
                actividadesFiltradas.add(a);
                Log.d("msg", a.getTitulo());
            }
        }
        actividadAdapter.notifyDataSetChanged();
        if(actividadesFiltradas.isEmpty()){
            emptyView.setVisibility(View.VISIBLE);
            ((TextView) emptyView.findViewById(R.id.tvMensajeEmpty)).setText("No hay actividades entre "+filtroFechaInicio.format(dateFormatter)+ " "+
                    filtroHoraInicio.format(timeFormatter)+ " y "+filtroFechaFin.format(dateFormatter)+ " "+ filtroHoraFin.format(timeFormatter));
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    public void cargando(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void terminarCargando(){
        progressBar.setVisibility(View.GONE);
    }


    public void cargarDatosdeFirebase(){
        emptyView.setVisibility(View.GONE);
        cargando();
        firebaseActividades.clear();
        ref.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                terminarCargando();
                for (DataSnapshot d : dataSnapshot.getChildren()){
                    Actividad actividad = d.getValue(Actividad.class);
                    actividad.setKey(d.getKey());
                    firebaseActividades.add(actividad);
                }
                filtrarActividades();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                terminarCargando();
                Toast.makeText(AgendaActivity.this, "No se pudo establecer conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setFilters(LocalDate filtroFechaInicio, LocalDate filtroFechaFin, LocalTime filtroHoraInicio, LocalTime filtroHoraFin){
        this.filtroFechaInicio = filtroFechaInicio;
        this.filtroFechaFin = filtroFechaFin;
        this.filtroHoraInicio = filtroHoraInicio;
        this.filtroHoraFin = filtroHoraFin;
    }
}