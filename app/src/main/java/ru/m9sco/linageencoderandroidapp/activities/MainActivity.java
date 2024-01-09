package ru.m9sco.linageencoderandroidapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.m9sco.linageencoderandroidapp.enums.HandlerCodes;
import ru.m9sco.linageencoderandroidapp.src.MyBluetoothDevice;
import ru.m9sco.linageencoderandroidapp.R;
import ru.m9sco.linageencoderandroidapp.src.ConnectThread;
import ru.m9sco.linageencoderandroidapp.src.ConnectedThread;

public class MainActivity extends AppCompatActivity {

//    Глобальные переменные
    float X, Y, OffsetX, OffsetY;

//    Блютуз

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice arduinoBTModule;
    public static Handler handler; // получение сообщений с бт
    private static int ERROR_READ = 0; // ошибки передачи сообщений по бт
    UUID arduinoUUID = UUID.fromString("00000001-0000-1000-8000-00805F9B34FB");

//    Элементы главного окна

    Button connectButton;
    Spinner spinner;

    TextView textViewStatusConnect, textViewValueX, textViewValueY;


//    Инициализация главного окна
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        connectButton = findViewById(R.id.connectButton);
        spinner = findViewById(R.id.spinner);
        textViewStatusConnect = findViewById(R.id.textViewStatusConnect) ;
        textViewValueX = findViewById(R.id.textViewValueX) ;
        textViewValueY = findViewById(R.id.textViewValueY) ;

        this.loadDevices();


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HandlerCodes.SOCKED.ordinal()){
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj, handler);
                    connectedThread.start();
                }
                else if (msg.what == HandlerCodes.META.ordinal() && msg.obj.toString().equals("Connection Success")){
                    textViewStatusConnect.setText("Подключено!");
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                } else if (msg.what == HandlerCodes.META.ordinal() && msg.obj.toString().equals("Disconnected")) {
                    textViewStatusConnect.setText(R.string.notconnected);
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();

                } else if (msg.what == HandlerCodes.DATA.ordinal()){
                    updateLinages(msg.obj.toString());
                } else {
                    Toast.makeText(MainActivity.this, "Invalid handler for: "+msg.obj.toString(), Toast.LENGTH_SHORT).show();

                }


            }
        };


    }


    public void loadDevices(){
        if (bluetoothAdapter == null) {
            return;

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    1);
            Toast.makeText(MainActivity.this, "BT not granted", Toast.LENGTH_SHORT).show();
            return;}

        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        List<MyBluetoothDevice> spinnerList = new ArrayList<MyBluetoothDevice>();


        for(BluetoothDevice device: pairedDevices){
            MyBluetoothDevice myBtDevice = new MyBluetoothDevice(device);
            spinnerList.add(myBtDevice);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @SuppressLint("MissingPermission")
    public void onClickConnectButton(View view)  {
        textViewStatusConnect.setText(R.string.doconnect);
        connectButton.setActivated(false);



        MyBluetoothDevice mdevice = (MyBluetoothDevice) spinner.getSelectedItem();
        arduinoBTModule = mdevice.mDevice;
        arduinoUUID = arduinoBTModule.getUuids()[0].getUuid();

        ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
        connectThread.start();
        if(!connectThread.getMmSocket().isConnected()) {
            textViewStatusConnect.setText(R.string.notconnected);
            return;
        }
        connectButton.setActivated(true);

    }

    public void updateLinages(String data){


        String[] elements = data.replace("\r\n", "").split("\\|");
        if(elements.length != 2) {
            Toast.makeText(MainActivity.this, "Receive ivalid row: "+ data , Toast.LENGTH_SHORT).show();

            return;

        }

        X = Float.parseFloat(elements[0])/ 1_000; // сразу в см
        Y = Float.parseFloat(elements[1])/ 1_000; // сразу в см

        updateLinages();
    }

    public void SetOffsetX(View view){
        OffsetX = X;
        updateLinages();

    }

    public void SetOffsetY(View view){
        OffsetY = Y;
        updateLinages();
    }


    public void updateLinages(){
        textViewValueX.setText(new Float(X-OffsetX).toString());
        textViewValueY.setText(new Float(Y-OffsetY).toString());
    }

}


