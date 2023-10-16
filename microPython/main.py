import bluetooth
import hashlib
import time
import machine
import ubinascii
import network

from machine import Pin

class LINEBeacon:
    def __init__(self):
        self._hwid = b'\x01\x72\x90\x90\xF2'
        self._vendor_key = b'Tanukigood'
        self._lot_key = b'TanukiLineBeacon'
        self._timestamp = 0
        self._ble = bluetooth.BLE()
        self._ble.active(True)
    def _generate_message_auth_code(self, timestamp, battery_level):
        hasher = hashlib.sha256()
        # Convert everything to bytes
        hasher.update(timestamp.to_bytes(8, 'big'))
        hasher.update(self._hwid)
        hasher.update(self._vendor_key)
        hasher.update(self._lot_key)
        hasher.update(battery_level)
        hash_value = hasher.digest()
        xor_1 = int.from_bytes(hash_value[:16], 'big') ^ int.from_bytes(hash_value[16:], 'big')
        xor_2 = (xor_1 >> 64) ^ (xor_1 & 0xFFFFFFFFFFFFFFFF)
        xor_3 = (xor_2 >> 32) ^ (xor_2 & 0xFFFFFFFF)
        return xor_3.to_bytes(4, 'big')
    def _generate_secure_message(self, timestamp, battery_level):
        mac = self._generate_message_auth_code(timestamp, battery_level)
        # Masking the timestamp
        masked_timestamp = timestamp.to_bytes(8, 'big')[-2:]
        return mac + masked_timestamp + battery_level
    def advertise(self, tx_power, battery_level):
        secure_message = self._generate_secure_message(self._timestamp, battery_level)
        # Ensure the payload has the correct length
        adv_payload = bytearray([
            0x02, 0x01, 0x06, 0x03, 0x03, 0x6F, 0xFE,
            0x11, 0x16, 0x6F, 0xFE, 0x02
        ]) + self._hwid + tx_power + secure_message + (b'\x00' * 6)
        self._ble.gap_advertise(152, adv_data=adv_payload)
        # Incrementing the timestamp
        self._timestamp += 1

battery = b'\x0B'
tx = b'\x7F'

beacon = LINEBeacon()
led = Pin("LED", Pin.OUT)
blink = 1

#28:CD:C1:05:14:9B
while True:
    '''add = network.WLAN(network.STA_IF)
    add.active(True)
    address = add.config('mac')
    print(ubinascii.hexlify(address).decode())'''

    led.value(blink)
    time.sleep(1)
    blink = not blink

    beacon.advertise(tx, battery)