import syntaxtree.*;
import visitor.*;

class Visitor3 extends GJDepthFirst<String, Data>{

    protected SymbolTable symbolTable;

    public Visitor3(SymbolTable symbolTable){
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

        // Get the current method
        String methodName = "main";
        MethodData mdata = cdata.getCertainMethod(methodName);

        if (node.f15.present()) {
            node.f15.accept(this, mdata);
        }

        return null;
    }

    /**
     * f0 -> ClassDeclaration
     *      | ClassExtendsDeclaration
     */
    public String visit(TypeDeclaration node, Data argu) throws Exception {
        // Either call ClassDeclaration or ClassExtendsDeclaration
        node.f0.accept(this, null);
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
    public String visit(ClassDeclaration node, Data argu) throws Exception{

        // First of all retrieve the current class data
        String className = node.f1.accept(this, null);
        ClassData cdata = this.symbolTable.getClassMap().get(className);

        if (node.f4.present()) {
            node.f4.accept(this, cdata);
        }

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration node, Data argu) throws Exception {

        // First of all retrieve the current class data
        String className = node.f1.accept(this, null);
        ClassData cdata = this.symbolTable.getClassMap().get(className);

        if (node.f6.present()) {
            node.f6.accept(this, cdata);
        }

        return null;
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
    public String visit(MethodDeclaration node, Data cdata) throws Exception {

        // First of all
        ClassData currentClass = (ClassData) cdata;

        // Get the current method's name so that we know which method we update
        String methodName = node.f2.accept(this, null);
        // Get the class's method
        MethodData currentMethod = currentClass.getCertainMethod(methodName);

        if (node.f8.present()) {
            node.f8.accept(this, currentMethod);
        }

        // Check if return type does not match the method's type
        String returnType = node.f10.accept(this, currentMethod);
        if (!currentMethod.getReturnType().equals(returnType)) {
            System.out.println("Incompatible return type! " + returnType + " for method: " + currentMethod.getName());
            System.exit(-1);
        }

        // Visit the Expression node with some information
        node.f10.accept(this, currentMethod);

        return null;
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     */
    public String visit(Statement node, Data mdata) throws Exception{
        node.f0.accept(this, mdata);
        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block node, Data mdata) throws Exception{
        if (node.f1.present()) {
            node.f1.accept(this, mdata);
        }

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement node, Data mdata) throws Exception{

        // First of all
        MethodData currentMethod = (MethodData) mdata;

        // Get the identifier's name
        String identifier = node.f0.accept(this, null);
        
        // Check if identifier is defined locally inside function
        if(currentMethod.variableExists(identifier)) {
            
            // Identifier (variable) has been defined inside method so we continue...
            IdData identifiersData = currentMethod.getGeneralVarMap().get(identifier);

            // Visit the expression node
            String expressionType = node.f2.accept(this, currentMethod);

            // First of all if epxression returned "this" we have to correct the expressionType
            if(expressionType.equals("this")) {
                expressionType = currentMethod.getClassData().getName();
            }

            if (!identifiersData.getType().equals(expressionType)) {

                // Checking the case of polymorphism
                if (this.symbolTable.getClassMap().get(expressionType) != null) {

                    if (!(this.symbolTable.variableTypePolymorphism(identifiersData.getType(), this.symbolTable.getClassMap().get(expressionType)))) {
                        System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType);
                        System.exit(-1);
                    }
                } else {
                    System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType); //////////////
                    System.exit(-1);
                }
            }

            // Mark the local variable as initialized
            currentMethod.getCertainVariable(identifier).setInitialized(true);

        } // Check if identifier refers to a variable of the class's method
        else if (currentMethod.getClassData().variableExists(identifier)) {

            // Identifier (variable) has been defined inside class's method so we continue...
            IdData identifiersData = currentMethod.getClassData().getCertainVariable(identifier);

            //StatementData sdata = new StatementData(currentMethod.getClassData(), currentMethod);
            // Visit the expression node
            String expressionType = node.f2.accept(this, currentMethod); ////////////////

            // First of all if epxression returned "this" we have to correct the expressionType
            if(expressionType.equals("this")) {
                expressionType = currentMethod.getClassData().getName();
            }

            if (!identifiersData.getType().equals(expressionType)) {

                // Checking the case of polymorphism
                if (this.symbolTable.getClassMap().get(expressionType) != null) {
                    if (!(this.symbolTable.variableTypePolymorphism(identifiersData.getType(), this.symbolTable.getClassMap().get(expressionType)))) {
                        System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType);
                        System.exit(-1);
                    }
                } else {
                    System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType);
                    System.exit(-1);
                }
            }

            // Mark the local variable as initialized
            currentMethod.getClassData().getCertainVariable(identifier).setInitialized(true);
        }

        // Case 3: if the identifier refers to a field of the superclass of the method (if it exists)
        else if (this.symbolTable.variableExistsInSuper(identifier, currentMethod.getClassData())) {

            // Identifier (variable) has been defined inside a super class of class's method so we continue...
            IdData identifiersData = this.symbolTable.getSuperVariable(identifier, currentMethod.getClassData()).getCertainVariable(identifier);
            
            //StatementData sdata = new StatementData(currentMethod.getClassData(), currentMethod);
            // Visit the expression node
            String expressionType = node.f2.accept(this, currentMethod); ////////////////

            // First of all if epxression returned "this" we have to correct the expressionType
            if(expressionType.equals("this")) {
                expressionType = currentMethod.getClassData().getName();
            }

            if (!identifiersData.getType().equals(expressionType)) {

                // Checking the case of polymorphism
                if (this.symbolTable.getClassMap().get(expressionType) != null) {
                    if (!(this.symbolTable.variableTypePolymorphism(identifiersData.getType(), this.symbolTable.getClassMap().get(expressionType)))) {
                        System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType);
                        System.exit(-1);
                    }
                } else {
                    System.out.println("Incompatible types! " + identifiersData.getType() + " " + expressionType);
                    System.exit(-1);
                }
            }

            // Mark the local variable as initialized
            this.symbolTable.getSuperVariable(identifier, currentMethod.getClassData()).getCertainVariable(identifier).setInitialized(true);
        }
        // If none of the above worked, print an error
        else {
            System.out.println("Unknown name " + identifier);
            System.exit(-1);
        }

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement node, Data mdata) throws Exception{

        // First of all
        MethodData currentMethod = (MethodData) mdata;

        // First we have to conduct checks for the identifier...

        // Get the identifier's name
        String identifier = node.f0.accept(this, null);
        String arrayType = null;

        // Find the array type based on  the identifier
        if(currentMethod.variableExists(identifier)) {
            arrayType = currentMethod.getCertainVariable(identifier).getType();
        } else if(currentMethod.getClassData().variableExists(identifier)){
            arrayType = currentMethod.getClassData().getCertainVariable(identifier).getType();
        } else if(this.symbolTable.variableExistsInSuper(identifier, currentMethod.getClassData())){
            arrayType = this.symbolTable.getSuperVariable(identifier, currentMethod.getClassData()).getCertainVariable(identifier).getType();
        } else {
            System.out.println("Array " + identifier + " not defined!");
            System.exit(-1);
        }

        // Now that we got the array type conduct check
        if(!(arrayType.equals("int[]"))) {
            System.out.println("Invalid array type! " + arrayType);
            System.exit(-1);
        }

        String arrayIndex = node.f2.accept(this, currentMethod);

        // Check the type of the array index
        if(!(arrayIndex.equals("int"))) {
            System.out.println("Invalid index type! " + arrayIndex);
            System.exit(-1);
        }

        String expressionType = node.f5.accept(this, currentMethod);

        // Checking the type of the expression that is to be assigned
        if (!(expressionType.equals("int"))) {
            System.out.println("Incompatible types! " + arrayType + " " + expressionType);
            System.exit(-1);
        }

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, Data mdata) throws Exception{
        n.f2.accept(this, mdata);
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, Data mdata) throws Exception{

        String expressionType = n.f2.accept(this, mdata);
        if (!expressionType.equals("boolean")) {
            System.out.println("Invalid type in if statement! " + expressionType);
            System.exit(-1);
        }

        n.f4.accept(this, mdata);
        n.f6.accept(this, mdata);

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, Data mdata) throws Exception{

        String expressionType = n.f2.accept(this, mdata);
        if (!expressionType.equals("boolean")) {
            System.out.println("Invalid type in while loop! " + expressionType);
            System.exit(1);
        }

        n.f4.accept(this, mdata);

        return null;
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | Clause()
     */
    public String visit(Expression n, Data mdata) throws Exception{
        return n.f0.accept(this, mdata);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, Data mdata) throws Exception{

        // Get clause1 and clause2
        // These clauses are basically strings - they must be of type boolean
        String clause1 = n.f0.accept(this, mdata);
        String clause2 = n.f2.accept(this, mdata);

        if (!(clause1.equals("boolean") && clause2.equals("boolean"))) {
            System.out.println("Bad operand types for && operator! " + clause1 + " " + clause2);
            System.exit(-1);
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, Data mdata) throws Exception{

        // Get clause1 and clause2
        // These clauses are basically strings - they must be of type integer ("int")
        String clause1 = n.f0.accept(this, mdata);
        String clause2 = n.f2.accept(this, mdata);


        if (!(clause1.equals("int") && clause2.equals("int"))) {
            System.out.println("Bad operand types for < operator! " + clause1 + " " + clause2);
            System.exit(-1);
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, Data mdata) throws Exception{

        // Get clause1 and clause2
        // These clauses are basically strings - they must be of type integer ("int")
        String clause1 = n.f0.accept(this, mdata);
        String clause2 = n.f2.accept(this, mdata);


        if (!(clause1.equals("int") && clause2.equals("int"))) {
            System.out.println("Bad operand types for + operator! " + clause1 + " " + clause2);
            System.exit(-1);
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, Data mdata) throws Exception{

        // Get clause1 and clause2
        // These clauses are basically strings - they must be of type integer ("int")
        String clause1 = n.f0.accept(this, mdata);
        String clause2 = n.f2.accept(this, mdata);


        if (!(clause1.equals("int") && clause2.equals("int"))) {
            System.out.println("Bad operand types for - operator! " + clause1 + " " + clause2);
            System.exit(-1);
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, Data mdata) throws Exception{

        // Get clause1 and clause2
        // These clauses are basically strings - they must be of type integer ("int")
        String clause1 = n.f0.accept(this, mdata);
        String clause2 = n.f2.accept(this, mdata);


        if (!(clause1.equals("int") && clause2.equals("int"))) {
            System.out.println("Bad operand types for * operator! " + clause1 + " " + clause2);
            System.exit(-1);
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, Data mdata) throws Exception{

        String arrayType = n.f0.accept(this, mdata);

        // Checking array type
        if(!(arrayType.equals("int[]"))) {
            System.out.println("Invalid array type! " + arrayType);
            System.exit(-1);
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, Data mdata) throws Exception{

        String arrayType = n.f0.accept(this, mdata);

        // Checking array type
        if(!(arrayType.equals("int[]"))) {
            System.out.println("Invalid array type! " + arrayType);
            System.exit(-1);
        }

        String expressionType = n.f2.accept(this, mdata);
        if (!expressionType.equals("int")) {
            System.out.println("Invalid index type! " + expressionType);
            System.exit(-1);
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend node, Data mdata) throws Exception{

        // First of all
        MethodData currentMethod = (MethodData) mdata;

        String primaryExpressionType = node.f0.accept(this, mdata);

        // If "this"
        if(primaryExpressionType.equals("this")) {
            primaryExpressionType = currentMethod.getClassData().getName();
        }
        
        String method = node.f2.accept(this, null);
        // maybe to be corrected later...
        ClassData callingClass = this.symbolTable.getCertainClass(primaryExpressionType);

        MethodData calledMethod = null;
        ClassData actualMethodsClass = null;

        // Check if method exists in calling class
        if (!callingClass.methodExists(method)) {

            // Check if method exists in super classes
            if (!(this.symbolTable.methodExistsInSuper(method, callingClass))) {        ///////////
                System.out.print("Method " + method + " does not exist in the calling class and its superclasses!");
                System.exit(-1);
            } else {
                // Correct methods Class
                actualMethodsClass = this.symbolTable.getSuperMethod(method, callingClass);
                calledMethod = actualMethodsClass.getCertainMethod(method);
            }

        } else {
            calledMethod = callingClass.getCertainMethod(method);
        }

        // Working on the arguments
        if (node.f4.present()) {

            // Check if the method takes any arguments
            if (calledMethod.getArguments().isEmpty()) {
                System.out.println("Method " + calledMethod.getName() + " does not take arguments");
                System.exit(-1);
            }

            // Get the argument list
            String argumentList = node.f4.accept(this, mdata);
            // Split it and conduct checks
            String[] argumentTypes = argumentList.split(" ");

            // Check the number of arguments
            if(argumentTypes.length != calledMethod.getArguments().size()) {
                System.out.println("Method " + calledMethod.getName() + " does not take " + argumentTypes.length + " arguments");
                System.exit(-1);
            }

            // For every argument...
            String legitArgumentType;
            String toBeCheckedArgumentType;
            for(int i = 0; i < argumentTypes.length; i++) {
                legitArgumentType = calledMethod.getArguments().get(i).getType();
                toBeCheckedArgumentType = argumentTypes[i];

                // Check the "this" case
                if(toBeCheckedArgumentType.equals("this")) {
                    toBeCheckedArgumentType = currentMethod.getClassData().getName();
                }

                // Checks for the type of the argument.
                // if legitArgumentType is not equal to the returned type then check if it corresponds to some parent class...
                if (!legitArgumentType.equals(toBeCheckedArgumentType)) {
                    // Checking the case of polymorphism
                    if(this.symbolTable.getClassMap().get(toBeCheckedArgumentType) != null){
                        if(!(this.symbolTable.variableTypePolymorphism(legitArgumentType, this.symbolTable.getClassMap().get(toBeCheckedArgumentType)))) {
                            System.out.println("Invalid type for argument " + calledMethod.getArguments().get(i).getName() + " expected: " + legitArgumentType + " and got: " + toBeCheckedArgumentType);
                            System.exit(-1);
                        }
                    } else {
                        System.out.println("Invalid type for argument " + calledMethod.getArguments().get(i).getName() + " expected: " + legitArgumentType + " and got: " + toBeCheckedArgumentType);
                        System.exit(-1);
                    }
                }
            }
        }

        String returnType = calledMethod.getReturnType();
        return returnType;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList node, Data mdata) throws Exception{

        // Get the type of the first argument
        String firstType = node.f0.accept(this, mdata);

        // Construct a string of all tha arguments
        // Add the first argument
        String argumentList = firstType;

        // Get the rest arguments
        String argumentListRest = node.f1.accept(this, mdata);
        // Check if the rest list is empty
        if(argumentListRest != "") {
            // if not concat
            argumentList = argumentList + " " + argumentListRest;
        }

        return argumentList;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail node, Data mdata) throws Exception{

        String argumentListRest = "";

        if (node.f0.present()) {
            // Count how many arguments exist
            int argNum = node.f0.nodes.size();

            // Loop for every declaration
            for(int i = 0; i < argNum; i++) {
                // Extract a string of form (variable_type, variable_name)
                String argument = node.f0.nodes.get(i).accept(this, mdata);

                argumentListRest = argumentListRest + argument + " ";
            }
        }
        return argumentListRest;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm node, Data mdata) throws Exception{
        return node.f1.accept(this, mdata);
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, Data mdata) throws Exception{
        return n.f0.accept(this, mdata);
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, Data argu) throws Exception{

        String expressionType = n.f1.accept(this, argu);
        // if (expressionType == null)
        //     System.out.println("Expression type is null");

        if (!expressionType.equals("boolean")) {
            System.out.println("Bad operand type for ! operator! " + expressionType);
            System.exit(-1);
        }

        return "boolean";
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, Data argu) throws Exception{

        // First of all
        MethodData mdata = (MethodData) argu;

        String primaryExpression = n.f0.accept(this, argu);

        // Case for identifiers
        if (mdata.variableExists(primaryExpression)) {
            return mdata.getCertainVariable(primaryExpression).getType();
        } else if (mdata.getClassData().variableExists(primaryExpression)) {
            return mdata.getClassData().getCertainVariable(primaryExpression).getType();
        } else if (this.symbolTable.variableExistsInSuper(primaryExpression, mdata.getClassData())){
            return this.symbolTable.getSuperVariable(primaryExpression, mdata.getClassData()).getCertainVariable(primaryExpression).getType();
        }

        // If all above fails just return the type that primaryExpression returned
        return primaryExpression;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, Data argu) {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, Data argu) {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, Data argu) {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, Data cdata) {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, Data argu) {
        return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, Data argu) throws Exception{

        // Check for expression at f3
        String expressionType = n.f3.accept(this, argu);
        if (!expressionType.equals("int")) {
            System.out.println("Incompatible types! " + expressionType + " " + "int");
            System.exit(-1);
        }

        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, Data argu) throws Exception{

        // Identifier must be a name of a declared class
        String identifier = n.f1.accept(this, null);
        if (this.symbolTable.getCertainClass(identifier) == null) {
            System.out.println("Name " + identifier + " does not exist");
            System.exit(-1);
        }

        return identifier;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, Data argu) throws Exception{
        return n.f1.accept(this, argu);
    }
}
