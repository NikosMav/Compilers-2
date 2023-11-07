# Compilers Assignment - Symbol Table Generation and Semantic Checks

## Program Structure

The code for the assignment is organized across the following Java files:

- Data.java
- IdData.java
- MethodData.java
- ClassData.java
- SymbolTable.java
- Visitor1.java
- Visitor2.java
- Visitor3.java
- Main.java

The Visitor1, Visitor2, and Visitor3 files are responsible for the collection of all classes and new types, the aggregation of all fields and methods of the classes, and performing semantic checks on various expressions and statements within methods, respectively.

Throughout these processes, various other semantic checks are concurrently performed. During the execution of all Visitors, a correctly structured Symbol Table is constructed, which, at the end, is printed with the various offsets set for the fields and methods of each class according to the rules set forth in the assignment description.

## Program Execution

The provided makefile from the original file distribution was used to execute the program. To run multiple files, the syntax should be as specified in the assignment description. However, if a file containing a semantic error is provided, the program will terminate and print an error message.

## License

This project is for educational use only and is part of the coursework for _Κ24 System Programming_ at _DiT, NKUA_.
