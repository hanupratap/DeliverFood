package com.example.deliverfood;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Location;
import android.os.Build;
import android.os.CancellationSignal;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecyclerViewAdapter_orders extends RecyclerView.Adapter<RecyclerViewAdapter_orders.MyViewHolder>  {
    List name, totals, order_id, ditance;
    Context context;
    String id_order;
    double total;

    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;


    private Activity activity;

    static float DEFAULT_ZOOM = 10;
    String phone_number;

    private FirebaseUser user;
    private OnItemClickListener mListener;


    private String COLLECTION_NAME = "Current_Orders";
    private String CONFIRM = "confirm";


    private int item_count = 0;

    public interface OnItemClickListener{

        void OnAcceptListener(int position);
    }



    void getLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {

                        if(task.getResult()!=null)
                        {
                            Toast.makeText(context, "Found location", Toast.LENGTH_SHORT).show();
                            currentLocation = (Location) task.getResult();

                        }

                        else
                        {
                            Toast.makeText(context, "Please Turn on Location Services!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else
                    {
                        Toast.makeText(context, "location not Found ", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
        catch (Exception e)
        {

        }

    }


    public void SetOnAcceptListener(RecyclerViewAdapter_orders.OnItemClickListener listener)
    {
        mListener = listener;
    }


    public RecyclerViewAdapter_orders(Context ct, List name, List prices, List order_id, FirebaseUser user, String phone_number, List distance)
    {
        this.phone_number = phone_number;
        this.user = user;
        this.order_id = order_id;
        this.name = name;
        this.totals = prices;
        this.context = ct;
        this.ditance = distance;


    }


    @NonNull
    @Override
    public RecyclerViewAdapter_orders.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.deliver_order_listitem,parent,false);
        return new RecyclerViewAdapter_orders.MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter_orders.MyViewHolder holder, int position) {
        holder.mytext1.setText(""+name.get(position).toString());
        holder.mytext2.setText("Rs. "+String.valueOf(totals.get(position)));
        holder.mytext3.setText(order_id.get(position).toString());
        holder.text4.setText("Distance from your location : "+ditance.get(position).toString());


    }

    ProgressDialog progressDialog;
    @Override
    public int getItemCount() {
        return name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView mytext1, mytext2, mytext3, text4;
        public Button btn;




        public MyViewHolder(@NonNull final View itemView, final RecyclerViewAdapter_orders.OnItemClickListener listener) {
            super(itemView);
            mytext1 = itemView.findViewById(R.id.textView17);
            mytext2 = itemView.findViewById(R.id.textView20);
            mytext3 = itemView.findViewById(R.id.textView19);
            text4 = itemView.findViewById(R.id.textView41);
            btn = itemView.findViewById(R.id.button8);

            btn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onClick(View v) {



                    if(Build.VERSION.SDK_INT>- Build.VERSION_CODES.M)
                    {
                        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
                        if (!fingerprintManager.isHardwareDetected()) {
                            // Device doesn't support fingerprint authentication
                            Toast.makeText(context, "Fingerprint Sensor not detected!, Overriding!!", Toast.LENGTH_SHORT).show();


                            id_order = mytext3.getText().toString();

                            final DocumentReference documentReference = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(id_order);
                            Map<String,String> map = new HashMap<>();
                            map.put("delivery_person_name", user.getDisplayName());
                            map.put("delivery_person_email",user.getEmail());
                            map.put("delivery_person_phone",phone_number);



                            Map mmap = new HashMap();
                            mmap.put(CONFIRM, true);

                            documentReference.set(mmap,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();

                                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, Deliver_Confirmed.class);
                                    intent.putExtra("order_id", id_order);
                                    intent.putExtra("user", user);
                                    context.startActivity(intent);
                                    ((Activity)context).finish();




                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Error!, Can't accept this order now!", Toast.LENGTH_SHORT).show();

                                }
                            });


                            documentReference.set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Details sent to user!", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                            // User hasn't enrolled any fingerprints to authenticate with
                            Toast.makeText(context, "Fingerprint not set!!, Overriding!!", Toast.LENGTH_SHORT).show();
                            id_order = mytext3.getText().toString();

                            final DocumentReference documentReference = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(id_order);
                            Map<String,String> map = new HashMap<>();
                            map.put("delivery_person_name", user.getDisplayName());
                            map.put("delivery_person_email",user.getEmail());
                            map.put("delivery_person_phone",phone_number);



                            Map mmap = new HashMap();
                            mmap.put(CONFIRM, true);

                            documentReference.set(mmap,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();

                                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, Deliver_Confirmed.class);
                                    intent.putExtra("order_id", id_order);
                                    intent.putExtra("user", user);
                                    context.startActivity(intent);
                                    ((Activity)context).finish();




                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Error!, Can't accept this order now!", Toast.LENGTH_SHORT).show();

                                }
                            });


                            documentReference.set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Details sent to user!", Toast.LENGTH_SHORT).show();

                                }
                            });




                        } else {
                            // Everything is ready for fingerprint authentication
                            Executor executor = Executors.newSingleThreadExecutor();

                            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(context)
                                    .setTitle("Fingerprint Authentication")
                                    .setSubtitle("Authorize Order")
                                    .setDescription(mytext2.getText().toString())
                                    .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).build();

                            biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {


                                    ((Activity)context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog = new ProgressDialog(context);
                                            progressDialog.show();
                                            progressDialog.setContentView(R.layout.progress_dialog);
                                            progressDialog.getWindow().setBackgroundDrawableResource(
                                                    android.R.color.transparent
                                            );

                                            getLocation();


                                            id_order = mytext3.getText().toString();

                                            final DocumentReference documentReference = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(id_order);
                                            Map<String,String> map = new HashMap<>();
                                            map.put("delivery_person_name", user.getDisplayName());
                                            map.put("delivery_person_email",user.getEmail());
                                            map.put("delivery_person_phone",phone_number);



                                            Map mmap = new HashMap();
                                            mmap.put(CONFIRM, true);

                                            documentReference.set(mmap,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();

                                                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(context, Deliver_Confirmed.class);
                                                    intent.putExtra("order_id", id_order);
                                                    intent.putExtra("user", user);
                                                    context.startActivity(intent);
                                                    ((Activity)context).finish();




                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, "Error!, Can't accept this order now!", Toast.LENGTH_SHORT).show();

                                                }
                                            });


                                            documentReference.set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(context, "Details sent to user!", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }
                                    });



                                }
                            });
                        }
                    }





                }
            });

        }
    }

}
