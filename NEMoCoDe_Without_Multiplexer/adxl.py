ACCEL_DEFAULT_ADDRESS = 0x53
ACCEL_DATAX0 = 0x32
ACCEL_DATAX1 = 0x33
ACCEL_DATAY0 = 0x34
ACCEL_DATAY1 = 0x35
ACCEL_DATAZ0 = 0x36
ACCEL_DATAZ1 = 0x37

class Severity(Enum):
    """
    An enum representing different severity scores.
    These severity scores are included within each ImpactData
    """
    UNSET = -1
    MINIMAL = 0
    LOW = 1
    MEDIUM = 2
    HIGH = 3

class ADXL:
    def __init__(self, address: int):
        self.address = address
        self.bus = smbus.SMBus(1)
    
    def get_accel(self):
        x0 = self.bus.read_i2c_block_data(self.address, ACCEL_DATAX0, 6)
        print(x0)
        self.bus.close()
