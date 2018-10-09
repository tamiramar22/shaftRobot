
package remoterobot.drivers;

import com.pi4j.io.i2c.I2CFactory;
import i2c.VL53L0X;
import java.util.logging.Level;
import java.util.logging.Logger;
import remoterobot.RemoteRobot;


public class SensorThread extends Thread {
    Double laserDistance = 0.0;
    private volatile boolean running = true;
    public void run(){
        while(true){
        try {            
            VL53L0X vl53l0x = new VL53L0X();
            vl53l0x.startRanging(VL53L0X.VL53L0X_BEST_ACCURACY_MODE);
            laserDistance = (1.0 * vl53l0x.getDistance()) / 10;
            vl53l0x.stopRanging();
        } catch (I2CFactory.UnsupportedBusNumberException ex) {
            Logger.getLogger(RemoteRobot.class.getName()).log(Level.SEVERE, null, ex);
        }   catch (Exception ex) {
                Logger.getLogger(RemoteRobot.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
        /*if (laserDistance > 30 && laserDistance < 2000) {
                laserDistance = laserDistance / 10;
            } else {
                laserDistance = 0.0;
            }
            */
        }
    }
     
    public Double getValue() throws InterruptedException {
        return laserDistance;
    }
}
