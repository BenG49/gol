package gol.input;

public class KeyBinding {
    private String[] binding;

    public enum Config {
        MAIN(new String[] {
            "Space",
            "Enter",
            "e", "r",
            "w", "a", "s", "d",
            "Up", "Down",
            "f",
            "c",
            "F1",
            "o", "p", 
            "Ctrl z",
            "Ctrl 1", "Ctrl 2"
        });

        String[] array;

        Config(String[] array) {
            this.array = array;
        }

        public String[] getArray() {
            return array;
        }
    };

    public KeyBinding() { this(Config.MAIN); }
    public KeyBinding(Config config) {
        binding = config.getArray();
    }

    // get key methods
    public String toggleAutoKey() {
        return binding[0];
    }

    public String singleStepKey() {
        return binding[1];
    }

    public String zoomOut() {
        return binding[2];
    }

    public String zoomIn() {
        return binding[3];
    }

    public String up() {
        return binding[4];
    }

    public String left() {
        return binding[5];
    }

    public String down() {
        return binding[6];
    }

    public String right() {
        return binding[7];
    }

    public String speedUp() {
        return binding[8];
    }

    public String speedDown() {
        return binding[9];
    }

    public String toOrigin() {
        return binding[10];
    }

    public String clear() {
        return binding[11];
    }

    public String toggleOptimized() {
        return binding[12];
    }

    public String saveKey() {
        return binding[13];
    }

    public String cancelKey() {
        return binding[14];
    }

    public String reset() {
        return binding[15];
    }

    public String mode1() {
        return binding[16];
    }

    public String mode2() {
        return binding[17];
    }
}
