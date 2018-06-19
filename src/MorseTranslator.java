import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

public class MorseTranslator {
    
    public static final String dot = "•";
    public static final String dash = "–";
    
    private static String fileName = "morsecodes.txt";
    private static HashMap<String, String> morseCodes = new HashMap<>();
    
    static /* Initializer */ {
        try {
            // Read codes from file
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "unicode"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] temp = line.split(" ");
                String letter = temp[0];
                String code = temp[1];
                morseCodes.put(letter, code);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Problem reading morse code translations from file: " + e);
        }
        
    }
    
    public static String textToMorse(String text) {
        String output = "";
        for (int i = 0; i < text.length(); i++) {
            String c = text.substring(i, i + 1).toLowerCase();
            switch (c) {
                case " ":
                    output += "   ";
                    break;
                case "\n":
                    c = "\\n";
                default:
                    output += morseCodes.get(c) + " ";
            }
        }
        return output;
    }
    
    public static String morseToText(String code) {
        String output = "";
        code = code.replaceAll("   ", " / ");
        String[] letters = code.split(" ");
        for (String letter : letters) {
            if (letter.equals("/")) {
                output += " ";
            } else {
                // Find the letter corresponding with the code
                boolean found = false;
                for (Entry<String, String> entry : morseCodes.entrySet()) {
                    if (entry.getValue().equals(letter)) {
                        if (entry.getKey().equals("end")) {
                            continue;
                        }
                        output += entry.getKey();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    output += "�";
                }
            }
        }
        output = output.replaceAll("\\\\n", "\n");
        return output;
    }
    
}
