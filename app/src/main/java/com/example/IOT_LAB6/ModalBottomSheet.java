package com.example.IOT_LAB6;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.IOT_LAB6.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ModalBottomSheet extends BottomSheetDialogFragment {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    EditText etFechainicio;
    EditText etFechafin;
    EditText etHorainicio;
    EditText etHorafin;

    LocalDate fechaInicio = LocalDate.now();
    LocalDate fechaFin = LocalDate.now();
    LocalTime timeInicio = LocalTime.of(6,0);
    LocalTime timeFin = LocalTime.of(23,30);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        MaterialTimePicker pickerHoraInicio = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(6)
                .setMinute(0)
                .setTitleText("Selecciona la hora de inicio")
                .build();
        pickerHoraInicio.addOnPositiveButtonClickListener(dialogInterface -> {
            timeInicio = LocalTime.of(pickerHoraInicio.getHour(),pickerHoraInicio.getMinute());
            if (timeInicio.isAfter(timeFin)){
                Toast.makeText(getActivity(), "La hora de inicio no puede ser posterior a la hora fin", Toast.LENGTH_SHORT).show();
                timeInicio = timeFin;
            }
            etHorainicio.setText(timeInicio.format(timeFormatter));
        });

        MaterialTimePicker pickerHoraFin= new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(23)
                .setMinute(30)
                .setTitleText("Selecciona la hora de fin")
                .build();
        pickerHoraFin.addOnPositiveButtonClickListener(dialogInterface -> {
            timeFin = LocalTime.of(pickerHoraFin.getHour(),pickerHoraFin.getMinute());
            if (timeFin.isBefore(timeInicio)){
                Toast.makeText(getActivity(), "La hora de inicio no puede ser posterior a la hora fin", Toast.LENGTH_SHORT).show();
                timeFin = timeInicio;
            }
            etHorafin.setText(timeFin.format(timeFormatter));
        });

        etFechainicio = view.findViewById(R.id.etBottomSheetFechaInicio);
        etFechafin = view.findViewById(R.id.etBottomSheetFechaFin);
        etHorainicio = view.findViewById(R.id.etBottomSheetHoraInicio);
        etHorafin = view.findViewById(R.id.etBottomSheetHoraFin);
        etFechainicio.setText(fechaInicio.format(dateFormatter));
        etFechafin.setText(fechaFin.format(dateFormatter));
        etHorainicio.setText(timeInicio.format(timeFormatter));
        etHorafin.setText(timeFin.format(timeFormatter));

        etFechainicio.setOnClickListener(view1 -> {
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
                    fechaInicio = LocalDate.parse(selectedDate, dateFormatter);
                    if (!fechaInicio.isAfter(fechaFin)){
                        etFechainicio.setText(selectedDate);
                    }else{
                        fechaInicio = fechaFin;
                        etFechainicio.setText(fechaInicio.format(dateFormatter));
                        Toast.makeText(getActivity(), "La fecha de inicio debe ser igual o anterior a la fecha fin", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            newFragment.show(getActivity().getSupportFragmentManager(),"datePicker");
        });
        etFechafin.setOnClickListener(view1 -> {
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
                    fechaFin = LocalDate.parse(selectedDate, dateFormatter);
                    if (!fechaFin.isBefore(fechaInicio)){
                        etFechafin.setText(selectedDate);
                    }else{
                        fechaFin = fechaInicio;
                        etFechafin.setText(fechaFin.format(dateFormatter));
                        Toast.makeText(getActivity(), "La fecha fin debe ser igual o posterior a la fecha de inicio", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            newFragment.show(getActivity().getSupportFragmentManager(),"datePicker");
        });
        etHorainicio.setOnClickListener(view1 -> {
            pickerHoraInicio.show(getActivity().getSupportFragmentManager(),"HoraInicio");
        });
        etHorafin.setOnClickListener(view1 -> {
            pickerHoraFin.show(getActivity().getSupportFragmentManager(),"HoraFin");
        });

        view.findViewById(R.id.bottomsheet_button).setOnClickListener(v -> {
            AgendaActivity activity = (AgendaActivity) getActivity();
            assert activity != null;
            activity.setFilters(fechaInicio,fechaFin,timeInicio,timeFin);
            activity.cargarDatosdeFirebase();
            dismiss();
        });
        return view;
    }


    public String TAG = "ModalBottomSheet";
}
