package remoterobot;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.VL53L0X;
import java.util.logging.Level;
import java.util.logging.Logger;
import remoterobot.drivers.Motor;
import remoterobot.drivers.SensorThread;

public class RemoteRobot {

    public enum MoveDirections {
        STOP,
        ROTATE_CW,
        ROTATE_CCW
    }

    public enum MovementState {
        STOPPED,
        MOVING
    }

    public enum DistanceSensorsType {
        SHAFT_LASER;
    }
    public static VL53L0X vl53l0x;
    public static boolean isMoving;
    public static Double afterDistance;
    public static Double velocity;
    public static Double MaxDistance;
    public static Double distance;
    public static Double speed;
    public static boolean smooth;
    public static RemoteMotor.MoveDirections moveDirection;
    public static int count;
    public static boolean homing;
    public static GpioPinDigitalOutput opticSensor;
    public static Motor motor;

    public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException, Exception {
        moveDirection = RemoteMotor.MoveDirections.ROTATE_CCW;
        velocity = 40.0;
        MaxDistance = 1200.0;
        smooth = false;
        speed = null;
        distance = MaxDistance;
        count = 0;
        isMoving = false;
        homing = false;

        GpioController gpio = GpioFactory.getInstance();
        opticSensor = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03); // optic sensor
        GpioPinDigitalOutput enable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Enable", PinState.LOW);
        motor = new Motor(RaspiPin.GPIO_00, RaspiPin.GPIO_02); // motor

        vl53l0x = new VL53L0X();
        GpioPinDigitalOutput sensor1Pin;

        sensor1Pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "Shaft", PinState.LOW);
        sensor1Pin.setState(true);

        switch (moveDirection) {
            case ROTATE_CW:
                velocity = velocity * 0.8;
                speed = 1.0;
                break;
            case ROTATE_CCW:
                velocity = velocity * 0.8;
                speed = -1.0;
                break;
        }

        motor.SetDirection(speed > 0);
                
        while (!homing) {
            if (!isMoving) {
                isMoving = true;
                moveDirection = RemoteMotor.MoveDirections.ROTATE_CW;
                speed = 1.0;
                motor.SetDirection(speed > 0);
                isMoving = true;
                if (opticSensor.getState().toString() == "HIGH") {
                    motor.move(Math.abs(speed), velocity, distance, smooth, new Motor.MotorAdvCallback() {
                        @Override
                        public boolean stop() {
                            return opticSensor.getState().toString() == "LOW";
                        }

                        @Override
                        public void OnComplete(long totalSteps, int totalDelay, double velocity) {
                            homing = true;
                            isMoving = false;
                            moveDirection = RemoteMotor.MoveDirections.ROTATE_CCW;
                            speed = -1.0;
                            motor.SetDirection(speed > 0);
                        }
                    });
                }
            }
        }       
        Thread.sleep(2000);
        SensorThread Sensor = new SensorThread();
        Sensor.start();

        while (count < 1) {
            if (!isMoving) {
                isMoving = true;
                motor.move(Math.abs(speed), velocity, distance, smooth, new Motor.MotorAdvCallback() {
                    @Override
                    public boolean stop() {
                        Double laserDistance = null;
                        try {
                            laserDistance = Sensor.getValue();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RemoteRobot.class.getName()).log(Level.SEVERE, null, ex);
                        }                       
                        return laserDistance >= 30.0;
                    }

                    @Override
                    public void OnComplete(long totalSteps, int totalDelay, double velocity) {
                        afterDistance = remoterobot.drivers.MotorThread.countSteps * constantClass.MOTOR_STEP_SIZE;
                        System.out.println("after distance" + " " + afterDistance);
                        moveDirection = RemoteMotor.MoveDirections.ROTATE_CW;
                        switch (moveDirection) {
                            case ROTATE_CW:
                                speed = 1.0;
                                break;
                            case ROTATE_CCW:
                                speed = -1.0;
                                break;
                        }

                        motor.SetDirection(speed > 0);

                        motor.move(Math.abs(speed), velocity, afterDistance, smooth, new Motor.MotorAdvCallback() {
                            @Override
                            public boolean stop() {
                                return false;
                            }

                            @Override
                            public void OnComplete(long totalSteps, int totalDelay, double velocity) {                               
                                moveDirection = RemoteMotor.MoveDirections.ROTATE_CCW;
                                velocity = 40.0;
                                MaxDistance = 1200.0;
                                boolean complete = false;
                                smooth = false;
                                speed = null;
                                distance = MaxDistance;

                                switch (moveDirection) {
                                    case ROTATE_CW:
                                        velocity = velocity * 0.8;
                                        speed = 1.0;
                                        break;
                                    case ROTATE_CCW:
                                        velocity = velocity * 0.8;
                                        speed = -1.0;
                                        break;
                                }

                                motor.SetDirection(speed > 0);
                                count ++;
                                isMoving = false;
                            }
                        });
                    }
                });
            }
        }
    }
}
