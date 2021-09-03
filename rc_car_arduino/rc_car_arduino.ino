#include <SoftwareSerial.h>
#include "control_message.h"
#include "motor_driver.h"

const int BLUETOOTH_SERIAL_RX_PIN = 9;
const int BLUETOOTH_SERIAL_TX_PIN = 10;
const int BUZZER_PIN = 11;
const int STATUS_LED_PIN = 13;

const int BUZZER_FREQUENCY = 500;
// Maximum time wihtout signal after which all devices are stopped;
const int MAX_INACTIVE_TIME = 1000;
// Time to ignore control messages after max inactive time has been reached.
// This is to prevent from stuttering when message frequency is just above the inactivity time.
const int COOLDOWN_TIME = 2000;

SoftwareSerial bluetoothSerial(BLUETOOTH_SERIAL_RX_PIN, BLUETOOTH_SERIAL_TX_PIN);

size_t bufferSize = 10;
size_t bufferPos = 0;
char buffer[10];

bool hasConnection = false;
unsigned long lastMessageTime = 0;
unsigned long cooldownEnd = 0;

bool previousHornStatus = false;

void setup() {
  Serial.begin(9600);
  bluetoothSerial.begin(9600);
  initializeMotorDriver();

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(STATUS_LED_PIN, OUTPUT);
}

void loop() {
  unsigned long now = millis();
  bool callProcessors = false;

  ControlMessage* msg = 0;

  char* read = readRawMessage();
  if (read) {
    lastMessageTime = now;

    if (now > cooldownEnd) {
      msg = parseControlMessage(read);
      callProcessors = true;
      hasConnection = true;  
    }
  }

  if (now - lastMessageTime > MAX_INACTIVE_TIME) {
    // Call processing functions just once, to notify that connection has been lost
    callProcessors = hasConnection;
    hasConnection = false;
    cooldownEnd = now + COOLDOWN_TIME;
  }

  if (callProcessors) {
    processMessage(msg);
    processHorn(msg); 
  }

  if (msg) {
    delete msg;
  }

  processStatusLed();
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

  if (hornStatus != previousHornStatus) {
    if (hornStatus) {
      tone(BUZZER_PIN, BUZZER_FREQUENCY);
    } else {
      noTone(BUZZER_PIN);
    }
  }

  previousHornStatus = hornStatus;
}

void processStatusLed() {
  int lastDigit = (millis() / 1000L) % 10;
  digitalWrite(STATUS_LED_PIN, lastDigit % 2 == 0);
}
