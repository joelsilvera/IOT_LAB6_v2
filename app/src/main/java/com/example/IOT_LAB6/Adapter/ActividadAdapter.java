package com.example.IOT_LAB6.Adapter;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.IOT_LAB6.AgendaActivity;
import com.example.IOT_LAB6.Entity.Actividad;
import com.example.IOT_LAB6.InsertarActivity;
import com.example.IOT_LAB6.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ViewHolder> {

    private ArrayList<Actividad> listaActividades;
    FirebaseDatabase firebaseDatabase;
    private Activity activity;
    public ActividadAdapter(Activity actividad, ArrayList<Actividad> dataSet){
        activity = actividad;
        listaActividades=dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_actividad,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Actividad actividad = listaActividades.get(position);
        holder.actividad = actividad;
        String urlImage = actividad.getImagenUrl();
        String mostrarNombre = actividad.getTitulo();
        String mostrarHorario = "Fecha: "+ actividad.getFecha() + " Hora: " + actividad.getHoraInicio()+"-"+actividad.getHoraFin();
        String mostrarDescri = actividad.getDescripcion();
        ImageView imageView = holder.imageView;
        TextView nombreActividad = holder.nombreActividad;
        TextView horaActividad = holder.horaActividad;
        TextView descripcionActividad = holder.descripcionActividad;
        nombreActividad.setText(mostrarNombre);
        horaActividad.setText(mostrarHorario);
        descripcionActividad.setText(mostrarDescri);
        Glide.with(imageView).load(urlImage).into(imageView);

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        Actividad actividad;
        private final ImageView imageView;
        private final TextView nombreActividad;
        private final TextView horaActividad;
        private final TextView descripcionActividad;
        private final Button editarBtn;
        private final Button eliminarBtn;


        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.imagenAct);
            nombreActividad = (TextView) view.findViewById(R.id.nombreAct_tv);
            horaActividad = (TextView) view.findViewById(R.id.horaAct_tv);
            descripcionActividad = (TextView) view.findViewById(R.id.descripcionAct_tv);
            editarBtn = (Button) view.findViewById(R.id.editar_btn);
            editarBtn.setOnClickListener(view1 -> {
                Intent intent = new Intent(view1.getContext(),InsertarActivity.class);
                intent.putExtra("activity", actividad);
                view1.getContext().startActivity(intent);
            });
            eliminarBtn = (Button) view.findViewById(R.id.eliminar_btn);
            eliminarBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child(user).child("actividades");
                    databaseReference.child(actividad.getKey()).removeValue().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(view.getContext(), "Actividad eliminada con éxito", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(view.getContext(), "Ha ocurrido un error al eliminar la actividad", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ((AgendaActivity) activity).cargarDatosdeFirebase();
                }
            });
        }

        public TextView getTextView(){
            return getTextView();
        }

    }

    @Override
    public int getItemCount() {
        return listaActividades.size();
    }

}
