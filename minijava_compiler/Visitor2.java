import syntaxtree.*;
import visitor.*;
import java.util.*;


class Visitor2 extends GJDepthFirst<String, Data>{

    protected SymbolTable symbolTable;

    public Visitor2(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal node, Data argu) throws Exception{
        // Call main class
        node.f0.accept(this, null);

        // Then proceed to other class declarations
        if (node.f1.present()) {
            node.f1.accept(this, null);
        }

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass node, Data argu) throws Exception {

        // First of all retrieve the current class data
        String className = node.f1.accept(this, null);
        ClassData cdata = this.symbolTable.getClassMap().get(className);

        // Now we continue where we left of from Visitor1...

        // Get the return type and name of main function.
        String returnType = "void";
        String methodName = "main";

        // Check if method has same name as its class
        if(cdata.getName().equals(methodName)) {
            System.out.println("Method cannot have the same name as class! " + methodName);
            System.exit(-1);
        }

        // Check if method has already been defined
        if(cdata.methodExists(methodName)) {
            System.out.println("Method " + methodName + " already defined in class!");
            System.exit(-1);
        }

        // Create new method data 
        MethodData newMethod = new MethodData(cdata, returnType, methodName, 0);
        // Add class' method
        cdata.insertMethod(newMethod);

        // Get the type of the method's argument
        String argumentType = "String[]";
        // Get the name of the method's argument
        String argumentName = "args";

        // No need to conduct checks for the argument's type (String[]) and its name (args).
        // With that assumption we move on... 

        // Now create the indentifier's (argumnent's) data
        IdData newArgument = new IdData(argumentType, true, argumentName, 0);
        // Adding the new identifier into the methods arguments
        newMethod.insertArgument(newArgument);
        // Map the new data into method's VarMap
        newMethod.insertIntoGeneralVarMap(newArgument);

        // Checking the variables
        if(node.f14.present()) {
            // Count how many variable declarations exist
            int varDeclNum = node.f14.nodes.size();

            // Loop for every declaration
            for(int i = 0; i < varDeclNum; i++) {
                // Extract a string of form (variable_type, variable_name)
                String variableDeclaration = node.f14.nodes.get(i).accept(this, null);

                // Split string
                String[] splittedString = variableDeclaration.split(" ");
                String variableType = splittedString[0];
                String variableName = splittedString[1];

                // Check if variable_type is valid
                ArrayList<String> validTypes = this.symbolTable.getValidTypes();
                if(!validTypes.contains(variableType)) {
                    System.out.println("Invalid type: " + variableType + " for variable: " + variableName);
                    System.exit(-1);
                }

                // Check if variable_name already exists
                ArrayList<IdData> variableList = cdata.getVariables();
                int classVarNum = variableList.size(); 
                for(int index = 0; index < classVarNum; index++) {
                    if(variableList.get(i).getName().equals(variableName)) {
                        System.out.println("Variable " + variableName + " already defined in class!");
                        System.exit(-1);
                    }
                }

                // variable_type is valid and variable_name does not already exist so we insert the new variable in symboltable
                // First create its idData
                IdData newMethVariable = new IdData(variableType, false, variableName, 0);
                // Insert it into the methodData
                newMethod.insertVariable(newMethVariable);
                // Map the new data into method's VarMap
                newMethod.insertIntoGeneralVarMap(newMethVariable);
            }
        }

        return null;
    }

    /**
     * f0 -> ClassDeclaration
     *      | ClassExtendsDeclaration
     */
    public String visit(TypeDeclaration n, Data argu) throws Exception {
        // Either call ClassDeclaration or ClassExtendsDeclaration
        n.f0.accept(this, null);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration node, Data argu) throws Exception {

        // First of all retrieve the current class data
        String className = node.f1.accept(this, null);
        ClassData cdata = this.symbolTable.getClassMap().get(className);

        // Now we continue where we left of from Visitor1...

        // Checks for the class's variables
        if(node.f3.present()) {
            // Count how many variable declarations exist
            int varDeclNum = node.f3.nodes.size();

            // Loop for every declaration
            for(int i = 0; i < varDeclNum; i++) {
                // Extract a string of form (variable_type, variable_name)
                String variableDeclaration = node.f3.nodes.get(i).accept(this, null);

                // Split string
                String[] splittedString = variableDeclaration.split(" ");
                String variableType = splittedString[0];
                String variableName = splittedString[1];

                // Check if variable_type is valid
                ArrayList<String> validTypes = this.symbolTable.getValidTypes();
                if(!validTypes.contains(variableType)) {
                    System.out.println("Invalid type: " + variableType + " for variable: " + variableName);
                    System.exit(-1);
                }

                // Check if variable_name already exists
                ArrayList<IdData> variableList = cdata.getVariables();
                int classVarNum = variableList.size(); 
                for(int index = 0; index < classVarNum; index++) {
                    if(variableList.get(index).getName().equals(variableName)) {
                        System.out.println("Variable " + variableName + " already defined in class!");
                        System.exit(-1);
                    }
                }

                // variable_type is valid and variable_name does not already exists so we insert the new variable in symboltable
                // First create its idData
                IdData newClassVariable = new IdData(variableType, false, variableName, 0);
                // Insert it into the classData
                cdata.insertVariable(newClassVariable);
            }
        }

        // Now call the methodDeclaration for the current class
        if(node.f4.present()) {
            node.f4.accept(this, cdata);
        }

        return null;
    }


    public String visit(ClassExtendsDeclaration node, Data argu) throws Exception {

        // First of all retrieve the current class data
        String className = node.f1.accept(this, null);
        ClassData cdata = this.symbolTable.getClassMap().get(className);

        // Now we continue where we left of from Visitor1...

        // Checks for the class's variables
        if(node.f5.present()) {
            // Count how many variable declarations exist
            int varDeclNum = node.f5.nodes.size();

            // Loop for every declaration
            for(int i = 0; i < varDeclNum; i++) {
                // Extract a string of form (variable_type, variable_name)
                String variableDeclaration = node.f5.nodes.get(i).accept(this, null);

                // Split string
                String[] splittedString = variableDeclaration.split(" ");
                String variableType = splittedString[0];
                String variableName = splittedString[1];

                // Check if variable_type is valid
                ArrayList<String> validTypes = this.symbolTable.getValidTypes();
                if(!validTypes.contains(variableType)) {
                    System.out.println("Invalid type: " + variableType + " for variable: " + variableName);
                    System.exit(-1);
                }

                // Check if variable_name already exists
                ArrayList<IdData> variableList = cdata.getVariables();
                int classVarNum = variableList.size();    
                for(int index = 0; index < classVarNum; index++) {
                    if(variableList.get(index).getName().equals(variableName)) {
                        System.out.println("Variable " + variableName + " already defined in class!");
                        System.exit(-1);
                    }
                }

                // variable_type is valid and variable_name does not already exists so we insert the new variable in symboltable
                // First create its idData
                IdData newClassVariable = new IdData(variableType, true, variableName, 0);
                // Insert it into the classData
                cdata.insertVariable(newClassVariable);
            }
        }

        // Now call the methodDeclaration for the current class
        if(node.f6.present()) {
            node.f6.accept(this, cdata);
        }

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration node, Data argu) throws Exception {
        return (node.f0.accept(this, null) + " " + node.f1.accept(this, null));
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration node, Data argu) throws Exception {

        // First of all
        ClassData cdata = (ClassData) argu;

        // Get the return type of method
        String returnType = node.f1.accept(this, null);
        // Get the method name 
        String methodName = node.f2.accept(this, null);

        // Check if variable_type is valid
        ArrayList<String> validTypes = this.symbolTable.getValidTypes();
        if(!validTypes.contains(returnType)) {
            System.out.println("Invalid return type: " + returnType + " for method: " + methodName);
            System.exit(-1);
        }

        // Check if method has already been defined
        if(cdata.methodExists(methodName)) {
            System.out.println("Method " + methodName + " already defined in class!");
            System.exit(-1);
        }

        // Check if method has same name as its class
        if(cdata.getName().equals(methodName)) {
            System.out.println("Method cannot have the same name as class!" + methodName);
            System.exit(1);
        }

        // After the checks create the method's data
        MethodData newMethod = new MethodData(cdata, returnType, methodName, 0);
        // and add the new method to the class's list
        cdata.insertMethod(newMethod);

        // Visit the argument list and update the methods data
        if(node.f4.present()) {
            node.f4.accept(this, newMethod);
        }

        // We are not done yet with the method's checks. Lastly we must check if method is defined already in super class...

        // Checking polymorphism
        // If method exists also in class's super class then...
        if(this.symbolTable.methodExistsInSuper(newMethod.getName(), cdata)) {

            ClassData superClass = this.symbolTable.getSuperMethod(newMethod.getName(), cdata);
            MethodData superMethod = superClass.getCertainMethod(newMethod.getName());

            // The methods must have the same return type
            if(!superMethod.getReturnType().equals(newMethod.getReturnType())) {
                System.out.println("Method " + methodName + " has different return types in super class " + superClass.getName());
                System.exit(-1);
            }

            // The methods must have the same number of arguments
            if(superMethod.getArguments().size() != newMethod.getArguments().size()) {
                System.out.println("Method " + methodName + " must have the same number of arguments in both the super class " + superClass.getName() + " and sub class " + cdata.getName());
                System.exit(-1);
            }

            // The arguments in both methods must have the same data types
            for(int i = 0; i < superMethod.getArguments().size(); i++) {
                String superType = superMethod.getArguments().get(i).getType();
                String childType = newMethod.getArguments().get(i).getType();

                if(!superType.equals(childType)) {
                    System.out.println("Method " + methodName + "() must have the same type of argumentsin both the super class " + superClass.getName() + " and sub class " + cdata.getName());
                    System.exit(-1);
                }
            }

        }

        // Now we must check the method's variables
        if(node.f7.present()) {
            // Count how many variable declarations exist
            int varDeclNum = node.f7.nodes.size();

            // Loop for every declaration
            for(int i = 0; i < varDeclNum; i++) {
                // Extract a string of form (variable_type, variable_name)
                String variableDeclaration = node.f7.nodes.get(i).accept(this, null);

                // Split string
                String[] splittedString = variableDeclaration.split(" ");
                String variableType = splittedString[0];
                String variableName = splittedString[1];

                // Check if variable_type is valid
                if(!validTypes.contains(variableType)) {
                    System.out.println("Invalid type: " + variableType + " for variable: " + variableName);
                    System.exit(-1);
                }

                // Check if variable already exists in method
                if(newMethod.variableExists(variableName)) {
                    System.out.println("Variable " + variableName + " already defined in method!");
                    System.exit(-1);
                }

                // variable_type is valid and variable_name does not already exists so we insert the new variable in symboltable
                // First create its idData
                IdData newMethVariable = new IdData(variableType, false, variableName, 0);
                // Insert it into the methodData
                newMethod.insertVariable(newMethVariable);
                // Map the new data into method's VarMap
                newMethod.insertIntoGeneralVarMap(newMethVariable);
            }
        }
        
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList node, Data mdata) throws Exception {

        node.f0.accept(this, mdata);
        node.f1.accept(this, mdata);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter node, Data mdata) throws Exception{

        // First of all
        MethodData methData = (MethodData) mdata;

        String argumentType = node.f0.accept(this, null);
        String argumentName = node.f1.accept(this, null);

        // Check if argumnent_type is valid
        if(!this.symbolTable.getValidTypes().contains(argumentType)) {
            System.out.println("Invalid argument type: " + argumentType + " for argument: " + argumentName);
            System.exit(-1);
        }

        // Checking if the argument_name already exists in arguments list
        ArrayList<IdData> argumentList = methData.getArguments();
        int methodArgNum = argumentList.size(); 

        for(int index = 0; index < methodArgNum; index++) {
            if(argumentList.get(index).getName().equals(argumentName)) {
                System.out.println("Argument " + argumentName + " already defined in method!");
                System.exit(-1);
            }
        }

        // argument_type is valid and argument_name does not already exists so we insert the new argument in symboltable
        // First create its idData
        IdData newArgument = new IdData(argumentType, true, argumentName, 0);
        // Insert it into the method's argument list
        methData.insertArgument(newArgument);
        // Map the new data into method's VarMap
        methData.insertIntoGeneralVarMap(newArgument);

        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm node, Data mdata) throws Exception {
        node.f1.accept(this, mdata);
        return null;
    }

    /**
     * f0 -> FormalParameterTerm()
     */
    public String visit(FormalParameterTail node, Data mdata) throws Exception {
        if(node.f0.present()) {
            node.f0.accept(this, mdata);
        }
        return null;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type node, Data argu) throws Exception {
        // Get the string - type and return it
        String type = node.f0.accept(this, null);
        return type;
    }

    /**
     * f0 -> int
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, Data argu) {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, Data argu) {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, Data argu) {
        return "int";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, Data argu) {
        return n.f0.toString();
    }
}