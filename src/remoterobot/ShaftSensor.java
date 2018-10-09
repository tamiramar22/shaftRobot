
package remoterobot;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
//import java.util.Hashtable;
import i2c.VL53L0X;
import remoterobot.drivers.SensorThread;
//import remoterobot.drivers.Sensor;
//import remoterobot.drivers.ISensorCallBacks;

public class ShaftSensor {
     public enum DistanceSensorsType {
         SHAFT_LASER;
     }
        
    public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException, Exception{
    
    boolean tamir = true;
    VL53L0X vl53l0x = new VL53L0X();
    GpioPinDigitalOutput sensor1Pin;
    
    GpioController gpio = GpioFactory.getInstance();    
    sensor1Pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "Shaft", PinState.LOW);
    sensor1Pin.setState(true);
    
    while (tamir){
        Thread.sleep(20);
        vl53l0x.startRanging(VL53L0X.VL53L0X_BEST_ACCURACY_MODE);
        Double distance = (1.0 * vl53l0x.getDistance()) / 10;
        vl53l0x.stopRanging();
   
        /*if (distance > 30 && distance < 2000) {
                distance = distance / 10;
            } else {
                distance = null;
            }
        */    
        System.out.print(distance);
    }
    
    }
}
