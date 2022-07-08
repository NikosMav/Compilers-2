import java.util.*;

public class MethodData extends Data {
    protected ClassData classData;                 // whose class implementation it is
    protected String returnType;                   // its return type
    protected ArrayList<IdData> arguments;      // its list of arguments
    protected ArrayList<IdData> variables;      // its list of variables
    protected HashMap<String, IdData> generalVarMap;   // Map the variable_names to their data


    public MethodData(ClassData classData, String returnType, String name, int offset){
        this.classData = classData;
        this.returnType = returnType;
        this.arguments = new ArrayList<IdData>();
        this.variables = new ArrayList<IdData>();
        this.generalVarMap = new HashMap<String, IdData>();
        this.name = name;
        this.offset = offset;
    }

    public ClassData getClassData() {
        return this.classData;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public ArrayList<IdData> getArguments() {
        return this.arguments;
    }

    public void insertArgument(IdData argData) {
        this.arguments.add(argData);
    }

    public ArrayList<IdData> getVariables() {
        return this.variables;
    }

    public void insertVariable(IdData varData) {
        this.variables.add(varData);
    }

    public HashMap<String, IdData> getGeneralVarMap() {
        return this.generalVarMap;
    }

    public void insertIntoGeneralVarMap(IdData variable) {
        this.generalVarMap.put(variable.getName(), variable);
    }

    public boolean variableExists(String variableName) {
        // Check if variable_name already exists in function
        // it must not exist in the variable list and in the argument list
        ArrayList<IdData> variableList = this.getVariables();
        ArrayList<IdData> argumentList = this.getArguments();
        int methodVarNum = variableList.size();
        int methodArgNum = argumentList.size();
        int index;
        
        // Check for variable list
        for(index = 0; index < methodVarNum; index++) {
            if(variableList.get(index).getName().equals(variableName)) {
                return true;
            }
        }

        // and argument list
        for(index = 0; index < methodArgNum; index++) {
            if(argumentList.get(index).getName().equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    public IdData getCertainVariable(String variableName) {
        // Check if variable_name already exists in function
        // it must not exist in the variable list and in the argument list
        ArrayList<IdData> variableList = this.getVariables();
        ArrayList<IdData> argumentList = this.getArguments();
        int methodVarNum = variableList.size();
        int methodArgNum = argumentList.size();
        int index;
        
        // Check for variable list
        for(index = 0; index < methodVarNum; index++) {
            if(variableList.get(index).getName().equals(variableName)) {
                return variableList.get(index);
            }
        }

        // and argument list
        for(index = 0; index < methodArgNum; index++) {
            if(argumentList.get(index).getName().equals(variableName)) {
                return argumentList.get(index);
            }
        }

        return null;
    }
}
