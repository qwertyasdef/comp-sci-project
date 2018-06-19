public class MorsePlayer extends Thread {
    
    private static final int dotTime = 100;
    private static final NotePlayer np = new NotePlayer();
    private static final int freq = 100;
    private static final int volume = 100;
    
    private final String code;
    private final MainFrame parent;
    
    public MorsePlayer(MainFrame parent, String code) {
        np.open();
        this.parent = parent;
        this.code = code;
    }
    
    public void run() {
        try {
            for (int i = 0; i < code.length(); i++) {
                switch (code.substring(i, i + 1)) {
                    case MorseTranslator.dot:
                        on();
                        Thread.sleep(dotTime);
                        off();
                        break;
                    case MorseTranslator.dash:
                        on();
                        Thread.sleep(3 * dotTime);
                        off();
                        break;
                    case " ":
                        Thread.sleep(dotTime);
                        break;
                }
                Thread.sleep(dotTime);
            }
        } catch (InterruptedException e) {
            off();
        }
        parent.sendDone();
    }
    
    private void on() {
        np.noteOn(freq, volume);
    }
    
    private void off() {
        np.noteOff(freq);
    }
    
}
