#include <SoftwareSerial.h>
#include "control_message.h"
#include "motor_driver.h"

const int BLUETOOTH_SERIAL_RX_PIN = 9;
const int BLUETOOTH_SERIAL_TX_PIN = 10;
const int BUZZER_PIN = 11;

const int BUZZER_FREQUENCY = 100;

SoftwareSerial bluetoothSerial(BLUETOOTH_SERIAL_RX_PIN, BLUETOOTH_SERIAL_TX_PIN);

size_t bufferSize = 10;
size_t bufferPos = 0;
char buffer[10];

void setup() {
  Serial.begin(9600);
  bluetoothSerial.begin(9600);
  initializeMotorDriver();
}

void loop() {
  char* read = readRawMessage();
  if (read) {
    ControlMessage* msg = parseControlMessage(read);
    if (msg) {
      processMessage(msg); // todo: processing functions should be lifted out of the 'read' condition
      delete msg;
    }

    processHorn(msg);
  }
}

char* readRawMessage() {
  if (!bluetoothSerial.available()) {
    return 0;
  }

  char read = bluetoothSerial.read();

  bool readIndicator = bufferPos < MESSAGE_INDICATOR_SIZE && MESSAGE_INDICATOR[bufferPos] == read;
  bool readMessage = bufferPos >= MESSAGE_INDICATOR_SIZE && bufferPos < MESSAGE_SIZE;

  if (readIndicator || readMessage) {
    buffer[bufferPos++] = read;
  } else {
    bufferPos = 0;
  }

  if (bufferPos == MESSAGE_SIZE) {
    buffer[bufferPos] = 0;
    bufferPos = 0;
    return buffer;
  }

  return 0;
}

void processHorn(ControlMessage* msg) {
  bool hornStatus = msg && msg->horn;
  if (hornStatus) {
    tone(BUZZER_PIN, BUZZER_FREQUENCY, 1500);
  } else {
    noTone(BUZZER_PIN);
  }
}
