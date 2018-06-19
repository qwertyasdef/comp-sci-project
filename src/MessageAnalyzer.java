
import java.util.ArrayList;
import java.util.HashMap;

public class MessageAnalyzer {
    
    private MainFrame parent;
    private MessageListener ml = new MessageListener(this);
    
    public MessageAnalyzer(MainFrame parent) {
        this.parent = parent;
    }
    
    public void analyze(ArrayList<Double> input) {
        /*
            Input looks like

              Sensor
              value
            1   |     _       ___
                |    / \     /   \
                |   |   |   |     |       etc.
            0   |__/     \_/       \___ 
                         Time

        
            Derivative looks like

              Slope
            max |   _       _
                |  | |     | |
                |  | |     | |
            0   |--+ +-+ +-+ +---+ +---   etc.
                |      | |       | |
                |      |_|       |_|
            min |
                          Time

            Peak in derivative means sensor turned on, trough means sensor turned off
        */
        ArrayList<Double> derivative = new ArrayList<>();
        // min < 0 and max > 0 since the signal must go up and down, so this is ok
        double min = 0;
        double max = 0;
        for (int i = 0; i + 1 < input.size(); i++) {
            double temp = input.get(i + 1) - input.get(i);
            derivative.add(temp);
            if (temp > max)
                max = temp;
            if (temp < min)
                min = temp;
        }
        
        // Message is length of on and off segments, alternating
        // Always start with off, then on
        ArrayList<Integer> message = new ArrayList<>();
        int start = 0;
        int stop;
        boolean isOn = false; // false for off, true for on
        for (int i = 0; i < derivative.size(); i++) {
            if (message.isEmpty() && derivative.get(i) < min / 2) {
                // It was on, so add a 0 time off to the start
                message.add(0);
                isOn = true;
            }
            // Peaks further than half the extrema count as turning on or off
            if (!isOn && derivative.get(i) > max / 2 || isOn && derivative.get(i) < min / 2) {
                stop = i;
                message.add(stop - start);
                start = i;
                isOn = !isOn;
            }
        }
        // The last off is not added because it is just the waiting time to make sure the message is over
        
        /*
            Off times are easier to analyze than on times because different gaps
            should have different frequencies, but dots and dashes are about
            equally frequent
            The value for dot time calculated from the off times should be
            applicable to on times as well
        */
        
        /*
            offTimes should have three peaks:
            Gap between dots and dashes (1)
            Gap between letters (3)
            Gap between words (7)
        
            Gaps between dots and dashes should appear the most frequently
        */
        HashMap<Integer, Integer> offTimeFreq = new HashMap<>();
        // Every other entry in message starting from 0 is the length of an off
        for (int i = 0; i < message.size(); i += 2) {
            int time = message.get(i);
            if (offTimeFreq.containsKey(time)) {
                offTimeFreq.put(time, offTimeFreq.get(time) + 1);
            } else {
                offTimeFreq.put(time, 1);
            }
        }
        // Find the most common length
        int commonestOffLength = (int) offTimeFreq.keySet().toArray()[0];
        for (int length : offTimeFreq.keySet()) {
            if (offTimeFreq.get(length) > offTimeFreq.get(commonestOffLength)) {
                commonestOffLength = length;
            }
        }
        int dotLength = commonestOffLength;
        
        String code = "";
        // The first off is just the time after the previous message, it means nothing
        for (int i = 1; i < message.size(); i++) {
            int time = message.get(i);
            if (i % 2 == 0) {
                // Off
                if (time > 5 * dotLength) {
                    // Too long for character break, must be word break
                    code += "   ";
                } else if (time > 2 * dotLength) {
                    // Too long for dot dash break, must be character break
                    code += " ";
                }
            } else {
                // On
                if (time > 2 * dotLength) {
                    // Too long for dot, must be dash
                    code += MorseTranslator.dash;
                } else {
                    code += MorseTranslator.dot;
                }
            }
        }
        
        parent.messageReceived(code);
        
    }
    
}
