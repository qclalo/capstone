import time
import board
import adafruit_adxl37x

i2c = board.I2C()
accelerometer = adafruit_adxl37x.ADXL375(i2c)

while True:
    print("%f %f %f m/s^2" % accelerometer.acceleration)
    time.sleep(0.2)

