import syntaxtree.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {

        FileInputStream fin = null;

        for(int i = 0; i < args.length; i++) {
            try{
                fin = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fin);
    
                Goal root = parser.Goal();
                System.err.println("Program parsed successfully.");
    
                SymbolTable symbolTable = new SymbolTable();
    
                // Call Visitor 1 to collect all the new data types and classes and do some necessary checks 
                Visitor1 vis1 = new Visitor1(symbolTable);
                root.accept(vis1, null);
    
                // After Visitor 1, we must call Visitor 2 in order to check every data type and conduct some more checks 
                Visitor2 vis2 = new Visitor2(symbolTable);
                root.accept(vis2, null);
    
                // After Visitor 2, we must call Visitor 3 in order to check for semantic errors (incorrect assignments, allocations, etc.)
                Visitor3 vis3 = new Visitor3(symbolTable);
                root.accept(vis3, null);
    
                symbolTable.setOffsets();
    
                symbolTable.printSymbolTable();
            }
    
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
    
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
    
            finally{
    
                try{
                    if(fin != null) fin.close();
                }
    
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}