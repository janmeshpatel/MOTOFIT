package com.test.motofit_temp.test_1.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.test.motofit_temp.test_1.R;
import com.test.motofit_temp.test_1.Firebase_Classes.Services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ServicesFragment  extends Fragment  {
    private  View v;
    EditText et_date,et_time,odometer,e4,notes;
    Button reg_btn;
    ImageButton imageButton;
    Spinner s1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mreference;
    private String userId;
    private static final int REQUEST_LOCATION=1;

    Geocoder geocoder;
    List<Address> addresses;

    LocationManager locationManager;

    CoordinatorLayout coordinatorLayout;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_services,container,false);



        et_date=v.findViewById(R.id.et_date);
        et_time=v.findViewById(R.id.et_time);
        imageButton = v.findViewById(R.id.imageButton);
        s1 = v.findViewById(R.id.spinner);
        e4=v.findViewById(R.id.location);
        reg_btn=v.findViewById(R.id.register);
        notes =v.findViewById(R.id.notes);
        odometer = v.findViewById(R.id.kilometer);
        mAuth = FirebaseAuth.getInstance();
        coordinatorLayout = v.findViewById(R.id.coordinatorLayout);
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        ArrayAdapter<String> service = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Services));
        service.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(service);

        ActivityCompat.requestPermissions(getActivity(),new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    //enable to gps on your devices.
                    OnGPS();
                }
                else
                {
                    //gps is already on ..
                    getlocation();
                }
            }
        });


    ///Register Button Logic
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = et_date.getText().toString().trim();
                String odomtr = odometer.getText().toString().trim();
                String time = et_time.getText().toString().trim();
                String locate = e4.getText().toString().trim();
                String service = s1.getSelectedItem().toString().trim();
                String note = notes.getText().toString().trim();
                if (date.isEmpty()) {
                    et_date.setError("");
                    et_date.requestFocus();
                    return;
                }
                if (time.isEmpty()) {
                    et_time.setError("");
                    et_time.requestFocus();
                    return;
                }
                if (odomtr.isEmpty()) {
                    odometer.setError("Enter Total Kilometer");
                    odometer.requestFocus();
                    return;
                }
                if (locate.isEmpty()) {
                    e4.setError("Enter Location");
                    e4.requestFocus();
                    return;
                }
                mreference = mFirebaseDatabase.getReference("Services");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                userId = user.getUid();
                Services services = new Services(date,time,service,odomtr,note,locate);
                mreference.child(userId).push().setValue(services);
                mreference.keepSynced(true);
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Data Sent to Realtime Database", Snackbar.LENGTH_LONG);
                snackbar.show();


            }
        });

           //Date Picker Logic
           et_date.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   final Calendar myCalendar = Calendar.getInstance();
                   final Calendar system = Calendar.getInstance();
                   DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                       @Override
                       public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                           // TODO Auto-generated method stub
                           myCalendar.set(Calendar.YEAR, year);
                           myCalendar.set(Calendar.MONTH, monthOfYear);
                           myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                           String myFormat = "dd-MMM-yyyy"; // your format
                           SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                           if (system.getTimeInMillis() > myCalendar.getTimeInMillis()){
                               Toast.makeText(getActivity(), "Please Enter Another Date",Toast.LENGTH_LONG).show();
                           }
                           else{
                                et_date.setText(sdf.format(myCalendar.getTime()));
                           }
                       }
                   };
                   new DatePickerDialog(getContext(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
               }
           });

           ///Time Picker Logic
           et_time.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   final Calendar mcurrentTime = Calendar.getInstance();
                   int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                   int minute = mcurrentTime.get(Calendar.MINUTE);
                   TimePickerDialog mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                       @SuppressLint({"SetTextI18n", "DefaultLocale"})
                       @Override
                       public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                               int hour = selectedHour % 12;
                               et_time.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                       selectedMinute, selectedHour < 12 ? "AM" : "PM"));
                           }
                   }, hour, minute, false);
                   mTimePicker.setTitle("Select Time");
                   mTimePicker.show();
               }
           });
           return  v;

    }
    private void getlocation()
    {
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);

        }
        else
        {
            Location LocationGps=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


            if(LocationGps!=null)
            {
                double lat=LocationGps.getLatitude();
                double log=LocationGps.getLongitude();
                location(lat,log);
            }
            if(LocationNetwork!=null)
            {
                double lat=LocationNetwork.getLatitude();
                double log=LocationNetwork.getLongitude();
                location(lat,log);
            }
            if(LocationPassive!=null)
            {
                double lat=LocationPassive.getLatitude();
                double log=LocationPassive.getLongitude();


                location(lat,log);
            }
            else
            {
                Toast.makeText(getContext(),"Can't Get Your Location",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void location(double latitude,double longitude)
    {
        geocoder=new Geocoder(getContext(), Locale.getDefault());
        try
        {
            addresses=geocoder.getFromLocation(latitude,longitude,1);

            String address=addresses.get(0).getAddressLine(0);
            String area=addresses.get(0).getLocality();
            String city=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();
            String postalcode=addresses.get(0).getPostalCode();

            String fulladdress=address +", "+area+", "+city+", "+country+", "+postalcode;
            e4.setText(fulladdress);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void OnGPS()
    {
        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
}







