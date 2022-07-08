import java.util.*;

public class ClassData extends Data {
    protected ClassData parent;
    protected ArrayList<IdData> variables;       // records for class variables of form: (variable_name, type, initialized)
    protected ArrayList<MethodData> methods;   // same for class methods of form: (class_name, return_type, arguments, variables)
    protected int fieldOffset;
    protected int methodOffset;


    public ClassData(ClassData parent, String name, int offset){
        this.parent = parent;
        this.name = name;
        this.variables = new ArrayList<IdData>();
        this.methods = new ArrayList<MethodData>();
        this.offset = offset;
        this.fieldOffset = 0;
        this.methodOffset = 0;
    }

    public ClassData getParent() {
        return this.parent;
    }

    public ArrayList<IdData> getVariables() {
        return this.variables;
    }

    public void insertVariable(IdData variableData) {
        variables.add(variableData);
    }

    public ArrayList<MethodData> getMethods() {
        return this.methods;
    }

    public void insertMethod(MethodData methodData) {
        methods.add(methodData);
    }

    public boolean variableExists(String variableName) {
        // Check if variable_name already exists in class
        ArrayList<IdData> variableList = this.getVariables();
        int classVarNum = variableList.size();
        int index;
        
        // Check for variable list
        for(index = 0; index < classVarNum; index++) {
            if(variableList.get(index).getName().equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    public boolean methodExists(String methodName) {
        // Check if methodName already exists in class
        ArrayList<MethodData> methodList = this.getMethods();
        int classMethNum = methodList.size();
        int index;
        
        // Check for variable list
        for(index = 0; index < classMethNum; index++) {
            if(methodList.get(index).getName().equals(methodName)) {
                return true;
            }
        }

        return false;
    }

    public MethodData getCertainMethod(String methodName) {
        ArrayList<MethodData> methodList = this.getMethods();

        int classMethNum = methodList.size();
        int index;
        
        for(index = 0; index < classMethNum; index++) {
            if(methodList.get(index).getName().equals(methodName)) {
                return methodList.get(index);
            }
        }

        return null;
    }

    public IdData getCertainVariable(String variableName) {
        ArrayList<IdData> variableList = this.getVariables();

        int classVarNum = variableList.size();
        int index;
        
        for(index = 0; index < classVarNum; index++) {
            if(variableList.get(index).getName().equals(variableName)) {
                return variableList.get(index);
            }
        }

        return null;
    }

    public void incFieldOffset(int increment) { 
        this.fieldOffset += increment; 
    }

    public void setFieldOffset(int fieldOffset) { 
        this.fieldOffset  = fieldOffset; 
    }

    public void incMethodOffset(int increment) { 
        this.methodOffset += increment; 
    }

    public void setMethodOffset(int methodOffset) { 
        this.methodOffset = methodOffset; 
    }

    public int getFieldOffset() { 
        return this.fieldOffset; 
    }

    public int getMethodOffset() { 
        return this.methodOffset; 
    }
}
