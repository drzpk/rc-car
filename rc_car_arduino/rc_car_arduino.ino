#include <SoftwareSerial.h>
#include "control_message.h"
#include "motor_driver.h"

SoftwareSerial bluetoothSerial(9, 10); // RX, TX

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
      processMessage(msg);
      delete msg;
    }
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
