// Class to store the basic info for every Identifier (i.e variables, arguments etc.)

public class IdData extends Data{
    protected String type;
    protected boolean initialized;

    public IdData(String type, boolean initialized, String name, int offset) {
        this.type = type;
        this.name = name;
        this.initialized = initialized;
        this.offset = offset;
    }

    public String getType() {
        return this.type;
    }

    public boolean getInitialized() {
        return this.initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized; 
    }
}
