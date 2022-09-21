from abc import abstractmethod


class AccelerometerData:
    def __init__(self, id: str, t: float, x: float, y: float, z: float):
        self.id = id
        self.t = t
        self.x = x
        self.y = y
        self.z = z

    def get_id(self):
        return self.id

    def get_time(self):
        return self.t

    def get_x(self):
        return self.x

    def get_y(self):
        return self.y

    def get_z(self):
        return self.z

    def reset(self):
        self.t = 0


class Packet:
    def __init__(self, id: str, t: float, accel: AccelerometerData):
        self.id = id
        self.t = t
        self.accel = accel


class Package:
    def __init__(self, id: str, t: float, packs):
        self.id = id
        self.t = t
        self.packs = packs


class ImpactData:
    def __init__(self, severity, data):
        self.severity = severity
        self.data = data 


class ControllerInterface:
    """
    Gets a packet from an accelerometer with a given id
    :param id: The id of the accelerometer to pull data from
    returns A packet representing x - y - z acceleration at a moment in time
    """

    @abstractmethod
    def get_accelerometer_packet(self, id: str) -> Packet:
        pass

    """
    @param Packet[]: a group of packets representing a single data collection moment
    @throws An error if the time between packets is not adequately synchronized
    @returns A Package created from an array of Packets with synchronized timings
    """

    @abstractmethod
    def assemble_package(self, packets) -> Package:
        pass

    """
    Adds a Package to the front of the queue and removes the oldest Package from the queue
    @param pack: the Package object to be added to the queue    
    """

    @abstractmethod
    def add_package_to_queue(self, pack: Package):
        pass

    """
    @param accelPort: serial id of the specific accelerometer being connected to
    @param id: ID to set for the accelerometer
    @throws An error if the connection has failed to initialize
    @returns an accelerometer object representing the accelerometer connected to
    """

    @abstractmethod
    def initialize_connection(self, accel_port: int, id: str) -> AccelerometerData:
        pass

    """
    Start collecting data from all of the accelerometers
    Save this data in a circular array / queue type data struct
    Watch for potentially concussive events
    If a concussive event is detected, take a snapshot of the data and send back to user
    """

    @abstractmethod
    def runData_collection_loop(self):
        pass

    """
    Algorithmic checking of queue for potential concussive impact above threshold limit
    @returns true or false indicating if a concussive event has occurred
    """

    @abstractmethod
    def check_queue_for_concussion(self) -> bool:
        pass

    """
    Create an ImpactData object from the current queue data
    This object will contain critical information that the client controller needs to visualize the data
    """

    @abstractmethod
    def create_impact_data(self) -> ImpactData:
        pass

    """
    Send alert to application with ImpactData for further analysis
    @param report: ImpactData object sent to application for further analysis
    """

    @abstractmethod
    def alert_user(self, report: ImpactData):
        pass

    """
    connects the microcontroller to phone application and initiates mic`rocontroller in standby mode
    @returns a true or false indicating if the connection is successful`
    """

    @abstractmethod
    def connect_to_user_device(self) -> bool:
        pass

    """
    Start data collection loop and give diagnostic info to microcontroller/application
    """

    @abstractmethod
    def start_session(self):
        pass

    """
    Monitor connection with accelerometers for functionality and connection with application for 
    start session indication. 
    """

    @abstractmethod
    def run_standby_loop(self):
        pass


class Controller(ControllerInterface):
    def get_accelerometer_packet(self, id: str) -> Packet:
        pass

    def assemble_package(self, packets) -> Package:
        pass

    def add_package_to_queue(self, pack: Package):
        pass

    def initialize_connection(self, accel_port: int, id: str) -> AccelerometerData:
        pass

    def runData_collection_loop(self):
        pass

    def check_queue_for_concussion(self) -> bool:
        pass

    def create_impact_data(self) -> ImpactData:
        pass

    def alert_user(self, report: ImpactData):
        pass

    def connect_to_user_device(self) -> bool:
        pass

    def start_session(self):
        pass

    def run_standby_loop(self):
        pass
