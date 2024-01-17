package com.queentylion.sibitranslator.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.data.GloveReceiveManager
import com.queentylion.sibitranslator.data.GloveResult
import com.queentylion.sibitranslator.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@SuppressLint("MissingPermission")
class GloveBLERecieveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : GloveReceiveManager {

    private val DEVICE_NAME = "Glove Flex Sensor"

    private val GLOVE_FLEX_SERVICE_UUID = "05f2f637-4809-4623-acbe-e2f2114ad9fe"

    private val P0_CHARACTERISTICS_UUID = "870542dd-02a1-45f7-89f4-f56bbd9dc31c"
    private val P1_CHARACTERISTICS_UUID = "e961724a-234c-4686-a63c-d2f2ae498dad"
    private val P2_CHARACTERISTICS_UUID = "cec35e6f-e94a-4992-a402-447cc682544d"
    private val P3_CHARACTERISTICS_UUID = "5664c1bd-03f9-4c03-a5fc-6e026cacbe0e"
    private val P4_CHARACTERISTICS_UUID = "4e2ffd1b-682c-4216-8b9a-726e797bf50a"

    private var characteristicsUuidArray = arrayListOf(
        P0_CHARACTERISTICS_UUID,
        P1_CHARACTERISTICS_UUID,
        P2_CHARACTERISTICS_UUID,
        P3_CHARACTERISTICS_UUID,
        P4_CHARACTERISTICS_UUID,
    )

    override val data: MutableSharedFlow<Resource<GloveResult>> = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.name == DEVICE_NAME){
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device..."))
                }
                if(isScanning){
                    result.device.connectGatt(context,false, gattCallback)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))
                    }
                    gatt.discoverServices()
                    this@GloveBLERecieveManager.gatt = gatt
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = GloveResult(IntArray(5),ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            }else{
                gatt.close()
                currentConnectionAttempt+=1
                coroutineScope.launch {
                    data.emit(
                        Resource.Loading(
                            message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"
                        )
                    )
                }
                if(currentConnectionAttempt<=MAXIMUM_CONNECTION_ATTEMPTS){
                    startReceiving()
                }else{
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
                }
                gatt.requestMtu(517)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
//            val characteristic = findCharacteristics(GLOVE_FLEX_SERVICE_UUID, TEMP_HUMIDITY_CHARACTERISTICS_UUID)
//            if(characteristic == null){
//                coroutineScope.launch {
//                    data.emit(Resource.Error(errorMessage = "Could not find temp and humidity publisher"))
//                }
//                return
//            }
//            enableNotification(characteristic)

//            val characteristicsUuids = listOf(
//                P0_CHARACTERISTICS_UUID,
//                P1_CHARACTERISTICS_UUID,
//                P2_CHARACTERISTICS_UUID,
//                P3_CHARACTERISTICS_UUID,
//                P4_CHARACTERISTICS_UUID
//            )

//            for( uuid in characteristicsUuids) {
//                val characteristic = findCharacteristics(GLOVE_FLEX_SERVICE_UUID,uuid)
//                if( characteristic == null ) {
//                    coroutineScope.launch {
//                        data.emit(Resource.Error(errorMessage = "could not find characteristic with UUID: $uuid"))
//                    }
//                    return
//                }
//                enableNotification(characteristic)
//            }
            enableNotification(gatt)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val characteristicUuids = listOf(
                UUID.fromString(P0_CHARACTERISTICS_UUID),
                UUID.fromString(P1_CHARACTERISTICS_UUID),
                UUID.fromString(P2_CHARACTERISTICS_UUID),
                UUID.fromString(P3_CHARACTERISTICS_UUID),
                UUID.fromString(P4_CHARACTERISTICS_UUID)
            )

            val index = characteristicUuids.indexOf(characteristic.uuid)
            if (index != -1) {
                // Assume each characteristic only contains a single integer value for this example.
                var value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0)
                var value4 = 0
                // If the characteristic is part of our list, we process it.
                var tempHumidityResult = GloveResult(
                    // Replace with actual logic to create an IntArray from characteristic values.
                    IntArray(5).apply {
                        this[index] = value
                    },
                    ConnectionState.Connected
                )

                if(index == 3) {
                    value4 = value / 10000
                    value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0) - value4 * 10000
                    tempHumidityResult = GloveResult(
                        IntArray(5).apply {
                            this[3] = value;
                            this[4] = value4
                        },
                        ConnectionState.Connected
                    )

                }

                coroutineScope.launch {
                    data.emit(
                        Resource.Success(data = tempHumidityResult)
                    )
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
//            TimeUnit.SECONDS.sleep(1)
            characteristicsUuidArray.removeAt(0)
            if (gatt != null) {
                enableNotification(gatt)
            }
        }


    }



    private fun enableNotification(gatt: BluetoothGatt){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            true -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            true-> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

//        if(characteristicsUuidArray.size == 1) {
//            TimeUnit.SECONDS.sleep(5)
//        }

        if(characteristicsUuidArray.size == 0) {
            characteristicsUuidArray = arrayListOf(
                P3_CHARACTERISTICS_UUID,
                P0_CHARACTERISTICS_UUID,
                P1_CHARACTERISTICS_UUID,
                P2_CHARACTERISTICS_UUID,
                P4_CHARACTERISTICS_UUID
            )
//            if(gatt.services)

            return
        };

        val characteristic = findCharacteristics(GLOVE_FLEX_SERVICE_UUID,
            characteristicsUuidArray[0]
        )

        if (characteristic != null) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        };

        characteristic?.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                Log.d("BLEReceiveManager","set characteristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    private fun findCharacteristics(serviceUUID: String, characteristicsUUID:String):BluetoothGattCharacteristic?{
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning Ble devices..."))
        }
        isScanning = true
        bleScanner.startScan(null,scanSettings,scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }


    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)

        // Assuming 'gatt' is the connected BluetoothGatt instance
        val serviceUuid = UUID.fromString(GLOVE_FLEX_SERVICE_UUID)
        val service = gatt?.getService(serviceUuid)

        if (service != null) {
            // List of characteristic UUIDs
            val characteristicsUuids = listOf(
                UUID.fromString(P0_CHARACTERISTICS_UUID),
                UUID.fromString(P1_CHARACTERISTICS_UUID),
                UUID.fromString(P2_CHARACTERISTICS_UUID),
                UUID.fromString(P3_CHARACTERISTICS_UUID),
                UUID.fromString(P4_CHARACTERISTICS_UUID)
            )

            // Iterate through each characteristic UUID and disable notifications
            for (charUuid in characteristicsUuids) {
                val characteristic = service.getCharacteristic(charUuid)
                if (characteristic != null) {
                    disconnectCharacteristic(characteristic)
                }
            }
        }

        gatt?.close()
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic,false) == false){
                Log.d("TempHumidReceiveManager","set charateristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

}