package com.example.dheeraj.btmusicreceiver;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dheeraj.btmusicreceiver.Adapters.PairedDevicesAdapter;
import com.example.dheeraj.btmusicreceiver.Adapters.SongListAdapters;
import com.example.dheeraj.btmusicreceiver.models.Song;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class mainmenu extends AppCompatActivity {

    public static final String TAG="mainmenu";
    private static final int REQUEST_ENABLE_BT = 101;
    BluetoothAdapter bluetoothAdapter;
    RecyclerView rv_paired_devices,rv_scanned_devices;
    Set<BluetoothDevice> scannedDevices;
    public static BluetoothDevice currDevice;
    public ArrayList<Song> songList;
    public boolean serviceOn=false;
    public static MusicPlayerService service;
    Intent playIntent;
    UUID serverUUID;

    ProgressDialog progressDialog;
    Set<BluetoothDevice> pairedDevices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        rv_paired_devices=(RecyclerView) findViewById(R.id.rv_paired_devices);
        rv_scanned_devices=(RecyclerView) findViewById(R.id.rv_scanned_devices);
        serverUUID=UUID.fromString("7feb4c6a-709f-44ea-a820-77acf550dc5d");
        Log.d(TAG, "onCreate: UUID::"+serverUUID);
        scannedDevices= new HashSet<BluetoothDevice>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        TextView tvphone=(TextView) findViewById(R.id.tvdevice);
        tvphone.setText(bluetoothAdapter.getName()+" is acting as the receiver....");
        progressDialog=new ProgressDialog(mainmenu.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Connecting.....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);

        if (bluetoothAdapter == null) {
            Toast.makeText(mainmenu.this,"This device does not support bluetooth",Toast.LENGTH_SHORT).show();
        }
        else{
            checkandmanagepermissions();
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicPlayerService.class);
            startService(playIntent);
            bindService(playIntent, musicservice, Context.BIND_AUTO_CREATE);
        }
    }




    ServiceConnection musicservice=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicBinder binder= (MusicPlayerService.MusicBinder) iBinder;
            service=binder.fetchService();
            service.setSongs(songList);
            serviceOn=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceOn=false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                Log.d(TAG, "onActivityResult: The activity result is received as OK");
                listPairedDevices();
                listScannedDevices();
            }
            else{
                Log.d(TAG, "onActivityResult: The activity result is received as Cancelled");
            }
        }
    }


    public void listPairedDevices(){
        pairedDevices = bluetoothAdapter.getBondedDevices();



        Log.d(TAG, "listPairedDevices: the size for all paired devices is"+pairedDevices.size());
/*  if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "listPairedDevices: paired device NAME: "+deviceName+" ADDRESS: "+deviceHardwareAddress);
            }
        }
*/

        rv_paired_devices.setLayoutManager(new LinearLayoutManager(mainmenu.this));
        PairedDevicesAdapter adapter=new PairedDevicesAdapter(mainmenu.this, pairedDevices, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currDevice=pairedDevices.toArray(new BluetoothDevice[0])[rv_paired_devices.getChildLayoutPosition(view)];
/*
                Toast.makeText(mainmenu.this,"curr device is: "+rv_paired_devices.getChildLayoutPosition(view),Toast.LENGTH_SHORT).show();
*/

                ConnectThread st=new ConnectThread(currDevice);
                st.start();
/*
                Toast.makeText(mainmenu.this,"Using as client",Toast.LENGTH_SHORT).show();
*/

            }
        });
        rv_paired_devices.setAdapter(adapter);


        /*rv_paired_devices.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Toast.makeText(mainmenu.this,"THis view was touched",Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/



    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.d(TAG, "scanned devices: adding:"+device.getName());

                scannedDevices.add(device);
                if (scannedDevices.size() > 0) {
                    listScannedDevices();
                }
            }
        }
    };


    public void listScannedDevices(){





/* if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "listPairedDevices: paired device NAME: "+deviceName+" ADDRESS: "+deviceHardwareAddress);
            }
        }
*/

        rv_scanned_devices.setLayoutManager(new LinearLayoutManager(mainmenu.this));
        PairedDevicesAdapter adapter=new PairedDevicesAdapter(mainmenu.this, scannedDevices, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currDevice=scannedDevices.toArray(new BluetoothDevice[0])[rv_paired_devices.getChildLayoutPosition(view)];
/*
                Toast.makeText(mainmenu.this,"curr device is: "+rv_paired_devices.getChildLayoutPosition(view),Toast.LENGTH_SHORT).show();
*/
                client_btn();
            }
        });
        rv_scanned_devices.setAdapter(adapter);
        adapter.notifyDataSetChanged();



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(playIntent);
        service=null;

    }

/*
    public void server_btn(View view) {
        ServerThread st=new ServerThread();
        st.start();
        Toast.makeText(mainmenu.this,"Using as server",Toast.LENGTH_SHORT).show();
    }
*/

    public void client_btn() {
        ConnectThread st=new ConnectThread(currDevice);

/*
        Toast.makeText(mainmenu.this,"Using as client",Toast.LENGTH_SHORT).show();
*/

        st.start();
    }

    class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public ServerThread() {
            // Toast.makeText(mainmenu.this,"Constructor1",Toast.LENGTH_SHORT).show();

            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothMusicPlayerServer", serverUUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    Log.d(TAG, "Server accepting a connection");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    //                  Toast.makeText(mainmenu.this,"Server:the connection is done a separate thread.",Toast.LENGTH_LONG).show();
                    //manageMyConnectedSocket(socket);


                    Log.d(TAG, "Connection established finally");


                    int len=0;


                    final BluetoothSocket finalSocket = socket;


                    try {


                        len=finalSocket.getInputStream().read();

                        Log.d("mainmenu","The client has "+len+" songs");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final int finalLen = len;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(),"The client has "+finalLen+" songs",Toast.LENGTH_SHORT).show();
                            AlertDialog dialog=new AlertDialog.Builder(mainmenu.this).create();
                            dialog.setMessage("Server control");
                            View v= LayoutInflater.from(mainmenu.this).inflate(R.layout.musicontrol,null,false);


                            RecyclerView rvmusic=(RecyclerView) v.findViewById(R.id.rv_music);

                            rvmusic.setVisibility(View.GONE);
                            ImageButton ibplay=(ImageButton) v.findViewById(R.id.play_pause);
                            ImageButton ibrewind=(ImageButton) v.findViewById(R.id.rewind);
                            ImageButton ibforward=(ImageButton) v.findViewById(R.id.forward);





/*

                            rvmusic.setLayoutManager(new LinearLayoutManager(mainmenu.this));
                            SongListAdapters adapter=new SongListAdapters(mainmenu.this,songList,true, finalLen);
                            rvmusic.setAdapter(adapter);
*/


                       /*     rvmusic.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    service.setSong(Integer.parseInt(view.getTag().toString()));
                                    service.playSong();
                                }
                            });
                       */



                            ibplay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        finalSocket.getOutputStream().write(1);
                                    } catch (IOException e) {
                                        Log.d(TAG, "Exception occured in sending the data"+e.getMessage());

                                        e.printStackTrace();
                                    }
                                }
                            });

                            ibrewind.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        finalSocket.getOutputStream().write(3);
                                    } catch (IOException e) {
                                        Log.d(TAG, "Exception occured in sending the data"+e.getMessage());

                                        e.printStackTrace();
                                    }
                                }
                            });

                            ibforward.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        finalSocket.getOutputStream().write(2);
                                    } catch (IOException e) {
                                        Log.d(TAG, "Exception occured in sending the data"+e.getMessage());

                                        e.printStackTrace();
                                    }
                                }
                            });


                            dialog.setView(v);

                            dialog.show();
                        }
                    });

                    Log.d(TAG, "Sent data at last");
                    try {

                        Log.d(TAG, "Server closing the socket");
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.d(TAG, "exception:  "+e.getMessage());

                        e.printStackTrace();
                    }
                    break;
                }
                else{

                    Log.d(TAG, "Connection could not be established, it is null");
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private InputStream mmInStream;
        private OutputStream mmOutStream;


        public ConnectThread(BluetoothDevice device) {

            if(device!=null){

                Log.d(TAG, "Found a proper bluetooth device to connect to");

                BluetoothSocket tmp = null;
                mmDevice = device;

                //        Toast.makeText(mainmenu.this,"Constructor2",Toast.LENGTH_SHORT).show();
                try {

                    Log.d(TAG, "Client creating RFCOMM");
                    tmp = device.createRfcommSocketToServiceRecord(serverUUID);
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                }
                mmSocket = tmp;
            }
            else{
                mmDevice=null;
                mmSocket=null;

                Log.d(TAG, "Could not find proper bluetooth device to connect to");

            }



            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;




        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            //      Toast.makeText(mainmenu.this,"Run2",Toast.LENGTH_SHORT).show();
            try {
                Log.d(TAG, "Client connecting to the remote device");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                    }
                });
                mmSocket.connect();

            } catch (IOException connectException) {
                try {
                    Log.d(TAG, "Client closing to the remote device");

                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.d(TAG, "Could not close the client socket", closeException);
                }
                return;
            }




            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();

                    final AlertDialog dialog=new AlertDialog.Builder(mainmenu.this).create();
                    dialog.setTitle("Connection established");
                    dialog.setMessage(" A connection was successfully established with "+mmSocket.getRemoteDevice());
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                }
            });



            Log.d(TAG, "Connection made on this thread");

            final int finalLen = songList.size();
            try {
                Log.d(TAG, "run: sending to server"+finalLen);
                mmSocket.getOutputStream().write(finalLen);


            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(),"The client has "+finalLen+" songs",Toast.LENGTH_SHORT).show();
                    AlertDialog dialog=new AlertDialog.Builder(mainmenu.this).create();
                    dialog.setMessage("Server control");
                    View v= LayoutInflater.from(mainmenu.this).inflate(R.layout.musicontrol,null,false);


                    RecyclerView rvmusic=(RecyclerView) v.findViewById(R.id.rv_music);
                    ImageButton ibplay=(ImageButton) v.findViewById(R.id.play_pause);
                    ImageButton ibrewind=(ImageButton) v.findViewById(R.id.rewind);
                    ImageButton ibforward=(ImageButton) v.findViewById(R.id.forward);






                    rvmusic.setLayoutManager(new LinearLayoutManager(mainmenu.this));
                    SongListAdapters adapter=new SongListAdapters(mainmenu.this,songList,false, finalLen);
                    rvmusic.setAdapter(adapter);




                    ibplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            service.playpause();
                        }
                    });

                    ibrewind.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            service.playprevsong();
                        }
                    });

                    ibforward.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            service.playnextsong();
                        }
                    });


                    dialog.setView(v);

                    dialog.show();
                }
            });


            while (true) {
                try {
                    int numBytes = mmInStream.read();
                    if(numBytes==1){
                        service.playpause();

                    }
                    else if(numBytes==2){
                        service.playnextsong();
                    }
                    else if(numBytes==3){
                        service.playprevsong();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }


            try {
                Log.d(TAG, "run: on client side we received"+mmSocket.getInputStream().read());

            } catch (IOException e) {
                Log.d(TAG, "run: exception while reading the data on client side"+e.getMessage());
                e.printStackTrace();
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }



    public ArrayList<Song> getAllSongs(){


        ArrayList<Song> songs=new ArrayList<Song>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                Log.d(TAG, "getAllSongs: adding "+thisTitle+" by "+thisArtist);
                songs.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }

        return songs;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode==105){
            int count=0;
            for(int i=0;i<permissions.length;i++){
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    count++;
                }
            }

            checkandmanagepermissions();

        }
    }

    public boolean allPermissionsGranted(){
        if((ActivityCompat.checkSelfPermission(mainmenu.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(mainmenu.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(mainmenu.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(mainmenu.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){

            Toast.makeText(mainmenu.this,"All permissions are granted",Toast.LENGTH_SHORT).show();
            return true;

        }
        else{

            Toast.makeText(mainmenu.this,"All permissions are not granted",Toast.LENGTH_SHORT).show();
            return false;
        }

    }
    public void checkandmanagepermissions(){

        if(allPermissionsGranted()){


            songList = new ArrayList<Song>();
            songList=getAllSongs();

            Toast.makeText(mainmenu.this,"Attaching Bluetooth adapter ...",Toast.LENGTH_SHORT).show();

            if(bluetoothAdapter.isEnabled()){
                Log.d(TAG, "onCreate: The bluetooth of the device is enabled");
                listPairedDevices();

                if(bluetoothAdapter.startDiscovery()){
                    Log.d(TAG, "listScannedDevices started device discovery successfully");

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                }
                listScannedDevices();
            }
            else{
                Log.d(TAG, "onCreate: The bluetooth of the device is not enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else{
            requestPermissions();
            Log.d(TAG, "onCreate: no permissions were granted at this point");
        }

    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions((Activity) mainmenu.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},105);
    }



}

