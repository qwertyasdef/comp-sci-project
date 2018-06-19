import javax.sound.midi.*;

public  class NotePlayer {
   
    public static int channel = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments

    public static Synthesizer synth; 
    public static MidiChannel[] channels; 
    public static boolean active=false;
    
    public static void open() {
        if (active)
            return;
        try {
            synth = MidiSystem.getSynthesizer();
            channels = synth.getChannels();  
            synth.open();  
            active=true;
        }catch(Exception e) {
            System.out.println("error initializing system synthesizer");
        }
        channels[channel].programChange(1024, 80);
    }
    
    public static void close() {
        if (!active)
            return;
        synth.close();
        active=false;
    }
    
    public static void noteOn(int freq, int volume) {
         if(!active)
             return;
         channels[channel].noteOn( freq, volume );   //c is 60
   } 
    
    public static void noteOff(int freq) {
          if (!active)
              return;
          channels[channel].noteOff( freq );
    }
    
    public static void playNote(int freq, int volume, int duration) {
          if (!active)
              return;
          try{
              noteOn(freq,volume);
              Thread.sleep(duration);
              noteOff(freq);
          }catch(Exception e) {
              System.out.println("Error in play note");
          }
    }
    
    public static void allOff(){
         if (!active)
             return;
         channels[channel].allNotesOff();
    }
    
}
