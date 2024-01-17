// void setup() {
//   // put your setup code here, to run once:
//   BT.begin("Esp32-BT");
  // analogReadResolution(12); // Set the resolution to 12-bit (0 - 4095)
  // pinMode(2, OUTPUT);
// }

#include <NimBLEDevice.h>

BLEServer* pServer = NULL;
BLECharacteristic* p0Characteristic = NULL;
BLECharacteristic* p1Characteristic = NULL;
BLECharacteristic* p2Characteristic = NULL;
BLECharacteristic* p3Characteristic = NULL;
BLECharacteristic* p4Characteristic = NULL;
BLECharacteristic* p5Characteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
int STRAIGHT_RESISTANCE = 2800;
int BEND_RESISTANCE = 4095;


#define SERVICE_UUID      "05f2f637-4809-4623-acbe-e2f2114ad9fe"
#define P0_CHARACTERISTIC "870542dd-02a1-45f7-89f4-f56bbd9dc31c"
#define P1_CHARACTERISTIC "e961724a-234c-4686-a63c-d2f2ae498dad"
#define P2_CHARACTERISTIC "cec35e6f-e94a-4992-a402-447cc682544d"
#define P3_CHARACTERISTIC "5664c1bd-03f9-4c03-a5fc-6e026cacbe0e"
#define P4_CHARACTERISTIC "4e2ffd1b-682c-4216-8b9a-726e797bf50a"
#define P5_CHARACTERISTIC "5ac6d2b9-5c97-45db-8040-f77cf18050da"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      BLEDevice::startAdvertising();
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
  /***************** New - Security handled here ********************
  ****** Note: these are the same return values as defaults ********/
    uint32_t onPassKeyRequest(){
      Serial.println("Server PassKeyRequest");
      return 123456; 
    }

    bool onConfirmPIN(uint32_t pass_key){
      Serial.print("The passkey YES/NO number: ");Serial.println(pass_key);
      return true; 
    }

    void onAuthenticationComplete(ble_gap_conn_desc desc){
      Serial.println("Starting BLE work!");
    }
  /*******************************************************************/
};



void setup() {
  Serial.begin(115200);

  // Create the BLE Device
  Serial.println("Starting BLE work!");
  BLEDevice::init("Glove Flex Sensor");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  p0Characteristic = pService->createCharacteristic(
                    P0_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );
  p1Characteristic = pService->createCharacteristic(
                    P1_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );
  p2Characteristic = pService->createCharacteristic(
                    P2_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );
  p3Characteristic = pService->createCharacteristic(
                    P3_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );
  p4Characteristic = pService->createCharacteristic(
                    P4_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );
  p5Characteristic = pService->createCharacteristic(
                    P5_CHARACTERISTIC,
               /******* Enum Type NIMBLE_PROPERTY now e961724a-234c-4686-a63c-d2f2ae498dad NIMBLE_PROPERTY now *******     
               /******* cec35e6f-e94a-4992-a402-447cc682544d
               5664c1bd-03f9-4c03-a5fc-6e026cacbe0e*     
                4e2ffd1b-682c-4216-8b9a-726e797bf50aow *******     
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
                **********************************************/    
                      NIMBLE_PROPERTY::READ   |
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY |
                      NIMBLE_PROPERTY::INDICATE
                    );

  // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
  // Create a BLE Descriptor
  /***************************************************   
   NOTE: DO NOT create a 2902 descriptor 
   it will be created automatically if notifications 
   or indications are enabled on a characteristic.
   
   pCharacteristic->addDescriptor(new BLE2902());
  ****************************************************/

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMaxPreferred(0x12);

  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");
}

void loop() {
    // notify changed value
    if (deviceConnected) {
       
        int val1 = analogRead(GPIO_NUM_13);
        int val2 = analogRead(GPIO_NUM_12);
        int val3 = analogRead(GPIO_NUM_14);
        int val4 = analogRead(GPIO_NUM_27);
        int val5 = analogRead(GPIO_NUM_26);

        int angle1 = map(val1, STRAIGHT_RESISTANCE, BEND_RESISTANCE, 0, 180);  
        angle1 = constrain(angle1, 0, 180); 
        int angle2 = map(val2, STRAIGHT_RESISTANCE, BEND_RESISTANCE, 0, 180);  
        angle2 = constrain(angle2, 0, 180); 
        int angle3 = map(val3, STRAIGHT_RESISTANCE, BEND_RESISTANCE, 0, 180);  
        angle3 = constrain(angle3, 0, 180); 
        int angle4 = map(val4, STRAIGHT_RESISTANCE, BEND_RESISTANCE, 0, 180);  
        angle4 = constrain(angle4, 0, 180); 
        int angle5 = map(val5, STRAIGHT_RESISTANCE, BEND_RESISTANCE, 0, 180);  
        angle5 = constrain(angle5, 0, 180); 

        p0Characteristic->setValue(angle1);
        p0Characteristic->notify();
        p1Characteristic->setValue(angle2);
        p1Characteristic->notify();
        p2Characteristic->setValue(angle3);
        p2Characteristic->notify();
        p3Characteristic->setValue(angle4+angle5*10000);
        p3Characteristic->notify();
        delay(200);
  
    }
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;
    }
}