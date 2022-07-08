import syntaxtree.*;
import visitor.*;
import java.util.*;

class Visitor1 extends GJDepthFirst<String, Data>{

    protected SymbolTable symbolTable;

    public Visitor1(SymbolTable symbolTable){
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
        // Get the class's name and check if it is already declared, if not insert it in the symboltable
        String className = node.f1.accept(this, null);

        if(this.symbolTable.getClasses().contains(className)) {
            System.out.println("Class name " + className + " already defined!");
            System.exit(-1);
        }

        // Add the name of the class to the list of valid data types
        this.symbolTable.getValidTypes().add(className);

        // Add the name of the current class to the list
        this.symbolTable.getClasses().add(className);
        
        // Create new class data from new className
        ClassData classData = new ClassData(null, className, 0);

        // Create a mapping for the current class
        this.symbolTable.insertClass(classData);

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
        String className = node.f1.accept(this, null);
        if(this.symbolTable.getClasses().contains(className)) {
            System.out.println("Class name " + className + " already defined!");
            System.exit(-1);
        }

        // Add the name of the class to the list of valid data types
        this.symbolTable.getValidTypes().add(className);

        // Add the name of the current class to the list
        this.symbolTable.getClasses().add(className);
        
        // Create new class data from new className
        ClassData classData = new ClassData(null, className, 0);

        // Create a mapping for the current class
        this.symbolTable.insertClass(classData);

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
        String className = node.f1.accept(this, null);
        if(this.symbolTable.getClasses().contains(className)) {
            System.out.println("Class name " + className + " already defined!");
            System.exit(-1);
        }

        // Add the name of the class to the list of valid data types
        this.symbolTable.getValidTypes().add(className);

        // Add the name of the current class to the list
        this.symbolTable.getClasses().add(className);

        // This class inherits from another. We must check if this extension is valid.
        // First we have to check if super class exists
        String superclassName = node.f3.accept(this, null);
        ArrayList<String> classes = this.symbolTable.getClasses();
        if(!classes.contains(superclassName)) {
            System.out.println("Super class " + superclassName + " does not exist!");
            System.exit(-1);
        }

        // Then check if super class name and child class name match
        if(superclassName.equals(className)) {
            System.out.println("Super class name and Child class name match! " + superclassName);
            System.exit(-1);
        }

        ClassData superClass = this.symbolTable.getClassMap().get(superclassName);

        // Create new class data from new className
        ClassData classData = new ClassData(superClass, className, 0);
        // Create a mapping for the current class
        this.symbolTable.insertClass(classData);

        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, Data argu) {
        return n.f0.toString();
    }
}
