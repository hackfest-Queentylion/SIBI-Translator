#include <Arduino.h>
#include <BluetoothSerial.h>
#include <sstream>
#include <vector>

using namespace std;

BluetoothSerial BT;

vector<int> dataArray = {1, 2, 3, 4, 5};

string convertArrayToString(const vector<int>& array);

void setup() {
  // put your setup code here, to run once:
  BT.begin("Esp32-BT");
  analogReadResolution(12); // Set the resolution to 12-bit (0 - 4095)
  pinMode(2, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(BT.hasClient()){
    int val1 = analogRead(GPIO_NUM_13);
    int val2 = analogRead(GPIO_NUM_12);
    int val3 = analogRead(GPIO_NUM_14);
    int val4 = analogRead(GPIO_NUM_27);
    int val5 = analogRead(GPIO_NUM_26);
    BT.print(val1);
    BT.print(" ");
    BT.print(val2);
    BT.print(" ");
    BT.print(val3);
    BT.print(" ");
    BT.print(val4);
    BT.print(" ");
    BT.println(val5);
  }
  delay(200);
}

string convertArrayToString(const vector<int>& array) {
    stringstream ss;
    for(size_t i = 0; i < array.size(); ++i) {
        if(i != 0) {
            ss << ",";
        }
        ss << array[i];
    }
    return ss.str();
}