package gol.input;

public class KeyBinding {
    private String[] binding;

    public enum Config {
        MAIN(new String[] {
            "Space",
            "Enter",
            "q",
            "e", "r",
            "w", "a", "s", "d"
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

    public String quitKey() {
        return binding[2];
    }

    public String zoomOut() {
        return binding[3];
    }

    public String zoomIn() {
        return binding[4];
    }

    public String up() {
        return binding[5];
    }

    public String left() {
        return binding[6];
    }

    public String down() {
        return binding[7];
    }

    public String right() {
        return binding[8];
    }
}
