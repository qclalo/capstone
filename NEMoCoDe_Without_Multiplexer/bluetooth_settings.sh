echo "Initializing bluetooth settings"
bluetoothctl -- power on
bluetoothctl -- system-alias "nemocode1"
bluetoothctl -- agent on
bluetoothctl -- discoverable on
bluetoothctl -- pairable on
hciconfig
