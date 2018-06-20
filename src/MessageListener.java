import com.phidget22.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class MessageListener {
    
    private MessageAnalyzer parent;
    private VoltageRatioInput sensor;
    private boolean simulate = askSimulate();
    private SimulateFrame simulation;
    private int serial;
    private int channel;
    
    Timer timer;
    ArrayList<Double> input = new ArrayList<>();
    double min = -1;
    double max = -1;
    int samplePeriod = 50; // Read sensor every this many ms
    int msgEndWait = 5; // Seconds to wait to make sure message is over
    
    public MessageListener(MessageAnalyzer parent) {
        this.parent = parent;
        if (simulate) {
            simulation = new SimulateFrame();
            simulation.setVisible(true);
        } else {
            this.setUpIK();
        }
        this.startTimer();
    }

    private boolean askSimulate() {
        int result = JOptionPane.showConfirmDialog(null, "Simulate sensors?", "Simulate?", YES_NO_OPTION);
        return (result == YES_OPTION);
    }
    
    private void getSerial() {
        this.serial = Integer.parseInt(JOptionPane.showInputDialog(null, "What is the serial number of the phidget?", "Serial number", QUESTION_MESSAGE));
        this.channel = Integer.parseInt(JOptionPane.showInputDialog(null, "What channel is your sensor in?", "Channel", QUESTION_MESSAGE));
    }
    
    public void setUpIK() {
        if (sensor!=null)
            return;
        
        this.getSerial();
        try {
                sensor=new VoltageRatioInput();
                System.out.println("Opening and waiting 3 seconds for sensor attatchment...");
                sensor.setDeviceSerialNumber(serial);
                sensor.setChannel(channel);
                sensor.open(3000);
                System.out.println("vr1 value is : " + sensor.getSensorValue() );
        } catch(Exception e) {
            System.out.println(e);
        }
        
    }
    
    public double getReading() {
        if (simulate) {
            return simulation.getReading();
        } else {
            try {
                System.out.println(sensor.getVoltageRatio());
                return sensor.getVoltageRatio();
            } catch (PhidgetException e) {
                return -1;
            }
        }
    }
    
    public void startTimer() {                                         
        if (timer!=null) {
            System.out.println("A timer is already working!");
            //return; 
        }
        
        //make a new timer object
        timer= new Timer(true);
        //make a timertask that has a job to do (call updateTime)
        TimerTask task= new TimerTask() {
            public void run() {
                // Get a new reading every 20 ms
                double in = getReading();
                input.add(in);
                // Update min and max values
                if (min == -1 || in < min) {
                    min = in;
                }
                if (max == -1 || in > max) {
                    max = in;
                }
                // Send the message to be analyzed
                if (messageIsOver()) {
                    parent.analyze(input);
                    input.clear();
                    max = -1;
                    min =-1;
                }
            }
        };
        //tell timer to start repeating the task
        timer.scheduleAtFixedRate(task, 0, samplePeriod);
    }
    
    private boolean messageIsOver() {
        // The difference probably isn't enough to be a signal
        if (max - min < 0.5) {
            return false;
        }
        // The signal counts as off if it is below the midpoint
        double mid = (min + max) / 2;
        // Check wait period or return if it hasn't been long enough
        if (input.size() < msgEndWait * 1000 / samplePeriod) {
            return false;
        }
        for (int i = input.size() - 1; i >= input.size() - msgEndWait * 1000 / samplePeriod; i--) {
            // Input was on within wait period, message is still ongoing
            if (input.get(i) > mid) {
                return false;
            }
        }
        // The message is over
        return true;
    }
    
}
