import java.util.*;

public class SymbolTable {
    protected ArrayList<String> validTypes; // A list of all the valid data types
    protected ArrayList<String> classes; // A list with the names of all classes
    protected HashMap<String, ClassData> classMap; // Where we'll keep information about every class

    public SymbolTable() {
        this.validTypes = new ArrayList<String>();
        this.classes = new ArrayList<String>();
        this.classMap = new HashMap<String, ClassData>();

        // Add the valid data types of mini java.
        this.validTypes.add("int");
        this.validTypes.add("boolean");
        this.validTypes.add("int[]");
    }

    public ArrayList<String> getValidTypes() {
        return this.validTypes;
    }

    public ArrayList<String> getClasses() {
        return this.classes;
    }

    public HashMap<String, ClassData> getClassMap() {
        return this.classMap;
    }

    public void insertClass(ClassData classData) {
        this.classMap.put(classData.getName(), classData);
    }

    public ClassData getCertainClass(String className) {
        return this.classMap.get(className);
    }

    // Source: stackoverflow
    public boolean isInteger(String s) {
        return isInteger(s,10);
    }
    
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }


    public boolean variableTypePolymorphism(String identifierType, ClassData idClass) {

        boolean flag = false;
        ClassData idClassParent = idClass.getParent();

        while(idClassParent != null) {
            if(idClassParent.getName().equals(identifierType)) {
                flag = true;
                break;
            }
            else {
                idClassParent = idClassParent.getParent();
                continue;
            }
        }
        return flag;
    }

    public boolean variableExistsInSuper(String fieldName, ClassData fieldClass) {

        boolean found = false;
        ClassData parentPtr = fieldClass.getParent();


        while(parentPtr != null) {

            if(parentPtr.variableExists(fieldName)) {
                found = true;
                break;
            }
            else {
                parentPtr = parentPtr.getParent();
                continue;
            }
        }

        return found;
    }

    public boolean methodExistsInSuper(String methodName, ClassData methClass) {

        boolean flag = false;
        ClassData methClassParent = methClass.getParent();

        while(methClassParent != null) {

            if(methClassParent.methodExists(methodName)) {
                flag = true;
                break;
            }
            else {
                methClassParent = methClassParent.getParent();
                continue;
            }
        }

        return flag;
    }

    public ClassData getSuperMethod(String methodName, ClassData methClass) {

        ClassData methClassParent = methClass.getParent();

        while(methClassParent != null) {

            if(methClassParent.methodExists(methodName)) {
                break;
            }
            else {
                methClassParent = methClassParent.getParent(); 
                continue;
            }
        }

        return methClassParent;
    }

    public ClassData getSuperVariable(String variableName, ClassData varClass) {

        ClassData varClassParent = varClass.getParent();

        while(varClassParent != null) {

            if(varClassParent.variableExists(variableName)) {
                break;
            }
            else {
                varClassParent = varClassParent.getParent(); 
                continue;
            }
        }

        return varClassParent;
    }

    // Method for printing
    public void printSymbolTable() {

        int classNum = this.classes.size();
        for(int i = 1; i < classNum; i++) {
            String className = this.classes.get(i);
            ClassData currentClass = this.classMap.get(className);
            //System.out.println("Information about class " + className + ":");

            System.out.println("-----------Class " + currentClass.getName() + "-----------");

            // Fields
            System.out.println("---Variables---");

            for(int j = 0; j < currentClass.getVariables().size(); j++){
                System.out.println(currentClass.getName() + "." + currentClass.getVariables().get(j).getName() + " : " + currentClass.getVariables().get(j).getOffset());
            }

            // Methods
            System.out.println("---Methods---");

            for (int k = 0; k < currentClass.getMethods().size(); k++) {
                MethodData currentMethod = currentClass.getMethods().get(k);
                if(!(currentClass.getParent() != null && methodExistsInSuper(currentMethod.getName(), currentClass))) {
                    System.out.println(currentMethod.getClassData().getName() + "." + currentMethod.getName() + " : " + currentMethod.getOffset());
                }
            }

            System.out.println();
        }
    }

    public void setOffsets() {
        ClassData currentClass = null;
        IdData currentField = null;
        MethodData currentMethod = null;

        // Calculating the offsets for every class
        for(int i = 0; i < this.classes.size(); i++) {
            currentClass = this.classMap.get(this.classes.get(i));

           // Calculating the offsets for the fields
           for(int j = 0; j < currentClass.getVariables().size(); j++) {
                currentField = currentClass.getVariables().get(j);

                if(currentField.getType().equals("int")) {

                    if(currentClass.getParent() != null) {

                        // if currentField is the first field in current Class and current Class has a parent...
                        if(currentClass.getVariables().get(0).getName().equals(currentField.getName())) {
                            // Set current class's field offset to be the field offset of its parent
                            currentClass.setFieldOffset(currentClass.getParent().getFieldOffset());

                        }

                        // Set field's offset
                        currentField.setOffset(currentClass.getFieldOffset());
                        // Increment the classe's field offset by 4
                        currentClass.incFieldOffset(4);

                    }
                    else {
                        currentField.setOffset(currentClass.getFieldOffset());
                        currentClass.incFieldOffset(4);
                    }
                }
                else if(currentField.getType().equals("boolean")) {

                    if(currentClass.getParent() != null) {

                        // if currentField is the first field in current Class and current Class has a parent...
                        if(currentClass.getVariables().get(0).getName().equals(currentField.getName())) {
                            // Set current class's field offset to be the field offset of its parent
                            currentClass.setFieldOffset(currentClass.getParent().getFieldOffset());

                        }

                        // Set field's offset
                        currentField.setOffset(currentClass.getFieldOffset());
                        // Increment the classe's field offset by 1
                        currentClass.incFieldOffset(1);

                    }
                    else {
                        currentField.setOffset(currentClass.getFieldOffset());
                        currentClass.incFieldOffset(1);
                    }
                }
                else if(currentField.getType().equals("int[]")) {

                    if(currentClass.getParent() != null) {

                        // if currentField is the first field in current Class and current Class has a parent...
                        if(currentClass.getVariables().get(0).getName().equals(currentField.getName())) {
                            // Set current class's field offset to be the field offset of its parent
                            currentClass.setFieldOffset(currentClass.getParent().getFieldOffset());

                        }

                        // Set field's offset
                        currentField.setOffset(currentClass.getFieldOffset());
                        // Increment the classe's field offset by 8
                        currentClass.incFieldOffset(8);

                    }
                    else {
                        currentField.setOffset(currentClass.getFieldOffset());
                        currentClass.incFieldOffset(8);
                    }
                }
                else {
                    if(currentClass.getParent() != null) {


                        // if currentField is the first field in current Class and current Class has a parent...
                        if(currentClass.getVariables().get(0).getName().equals(currentField.getName())) {
                            // Set current class's field offset to be the field offset of its parent
                            currentClass.setFieldOffset(currentClass.getParent().getFieldOffset());

                        }

                        // Set field's offset
                        currentField.setOffset(currentClass.getFieldOffset());
                        // Increment the classe's field offset by 8
                        currentClass.incFieldOffset(8);

                    }
                    else {
                        currentField.setOffset(currentClass.getFieldOffset());
                        currentClass.incFieldOffset(8);
                    }
                }
           }

            // Calculating the offsets for the methods and the pointers
            for(int j = 0; j < currentClass.getMethods().size(); j++) {
                currentMethod = currentClass.getMethods().get(j);

                String methodName = currentMethod.getName();

                // if class's method has a parent class then...
                if(currentMethod.getClassData().getParent() != null) {
                    // if method does not also exists in parent class...
                    if(!currentMethod.getClassData().getParent().methodExists(methodName)) {

                        // if currentMethod is the first method in current Class and current Class has a parent
                        // and also method does not exist in super class...
                        if(currentClass.getMethods().get(0).getName().equals(currentMethod.getName())) {
                            // Set current class's method offset to be the method offset of its parent
                            currentClass.setMethodOffset(currentClass.getParent().getMethodOffset());
                        }

                        // Set field's offset
                        currentMethod.setOffset(currentClass.getMethodOffset());
                        // Increment the classe's field offset by 8
                        currentClass.incMethodOffset(8);
                    }
                }
                else {

                    currentMethod.setOffset(currentMethod.getClassData().getMethodOffset());
                    currentMethod.getClassData().incMethodOffset(8);
                }
            }
        }
    }
}
