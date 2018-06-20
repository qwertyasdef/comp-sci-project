
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
            high|    / \     /   \
                |   |   |   |     |       etc.
                |   |   |   |     |    
            low |__/     \_/       \___ 
                |
            0   |----------------------
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
        // Always start with on, then off
        ArrayList<Integer> message = new ArrayList<>();
        int start = 0;
        int stop;
        boolean isOn = true; // true for on, false for off
        for (int i = 0; i < derivative.size(); i++) {
            if (start == 0 && derivative.get(i) > max / 2) {
                // First time turning on, don't add the initial off segment because 
                // it is just the time since the previous message and means nothing
                start = i;
                continue;
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
        // message should start and end with on
        
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
        // Every other entry in message starting from 1 is the length of an off
        for (int i = 1; i < message.size(); i += 2) {
            int time = message.get(i);
            if (offTimeFreq.containsKey(time)) {
                offTimeFreq.put(time, offTimeFreq.get(time) + 1);
            } else {
                offTimeFreq.put(time, 1);
            }
        }
        // Find the most common length (should be dot time)
        /*
            This doesn't work for short slow messages because the dot time is measured
            in multiples of MessageListesner.samplePeriod ms. When the message is slow
            and short, a dotplot of off times can look like this just by coincidence:
            
              Freq
                |                       .
                |     ....  .           .
                |  . .........          .                              .   .
                |------------------------------------------------------------
                0       1       2       3       4       5       6       7    
                                            Length
            
            The character space just happened to be more accurate than the space 
            between dots and dashes, so the program thinks the dot length is 3 
            instead of 1 even though most of the dots are clustered around 1.
        
            The longer the message, the more data there is, the lower the chance 
            of this happening.
            The closer the dot length is to MessageListesner.samplePeriod, the 
            more variation required to be recorded as a different time, the 
            lower chance of this happening.
            
            Possible fix:
            For a given value of dotLength, the expected off times are 
            dotLength, 3 * dotLength, and 7 * dotLength. Find the value of 
            dotLength that minimizes the sum of the squared differences between 
            each time and the closest expected time.
            
            Problem:
            I don't know how to do this other than guessing and checking 
            possible values of dotLength, which would be really slow.
        */
        int commonestOffLength = (int) offTimeFreq.keySet().toArray()[0];
        for (int length : offTimeFreq.keySet()) {
            if (offTimeFreq.get(length) > offTimeFreq.get(commonestOffLength)) {
                commonestOffLength = length;
            }
        }
        int dotLength = commonestOffLength;
        
        String code = "";
        // Translate the various times in message into morse code
        for (int i = 0; i < message.size(); i++) {
            int time = message.get(i);
            if (i % 2 == 1) {
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
        
        // Send the code to the MainFrame to display and translate to english
        parent.messageReceived(code);
        
    }
    
}
