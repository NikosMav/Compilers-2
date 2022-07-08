//Parent class for other data classes. This will help us later in overriding the visit function.

public class Data {
    protected String name;
    protected int offset;

    public String getName() {
        return this.name;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
