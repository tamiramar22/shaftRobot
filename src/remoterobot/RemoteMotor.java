
package remoterobot;

import remoterobot.drivers.Motor;
//import remoterobot.drivers.MotorThread;
//import remoterobot.drivers.ShaftMotor;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
//import java.util.ArrayList;

public class RemoteMotor {

    public enum MoveDirections {
        STOP,
        ROTATE_CW,
        ROTATE_CCW
    }

    public enum MovementState {
        STOPPED,
        MOVING
    }
    
    public static Long after;
    
    public static void main(String[] args) {
        MovementState state = MovementState.MOVING;
        MoveDirections moveDirection = MoveDirections.ROTATE_CCW;
        Double velocity = 40.0;
        boolean smooth = false;
        Double speed = null;
        Double distance = 200.0;
             
        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput enable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Enable", PinState.LOW);
        Motor motor = new Motor(RaspiPin.GPIO_00, RaspiPin.GPIO_02);
              
        switch (moveDirection) {
            case ROTATE_CW:
                velocity = velocity*0.8;
                speed = 1.0;
                break;
            case ROTATE_CCW:
                velocity = velocity*0.8;
                speed = -1.0;
                break;
        }
        
        motor.SetDirection(speed > 0);
        
        if (Math.abs(speed) > 0.0000001) {
            motor.move(Math.abs(speed), velocity, distance, smooth,new Motor.MotorAdvCallback() {
                @Override
                public boolean stop() {
                    return false;
                }
                
                @Override
                public void OnComplete(long totalSteps, int totalDelay, double velocity) {
                    after = remoterobot.drivers.MotorThread.countSteps;
                    System.out.println("after distance" + " " + after);
                }    
            });
        } 
        //else {
          //  complete = true;
            
        //}  
        
    }
    
}
