import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ANTLRFileStream input = new ANTLRFileStream(args[0]);
        NarwhalLexer lexer = new NarwhalLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NarwhalParser parser = new NarwhalParser(tokens);

        ParseTree tree = parser.prog();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new LLVMActions(), tree);

    }
}
