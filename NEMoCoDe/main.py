import DataStructures as ds
import time

def main():
    print("Hello World")
    controller = ds.Controller()
    controller.initialize_connection(0x53, 0)
    while True:
        ap = controller.get_accelerometer_packet(0)
        print(f"x={ap.x}, y={ap.y}, z={ap.z}")
        time.sleep(0.2)


if __name__ == '__main__':
    main()
