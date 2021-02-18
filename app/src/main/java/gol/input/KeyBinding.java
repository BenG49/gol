package gol.input;

public class KeyBinding {
    private String[] binding;

    public enum Config {
        MAIN(new String[] {
            "Space",
            "Enter",
            "q"
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
}
