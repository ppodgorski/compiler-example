import java.util.HashMap;
import java.util.Stack;

public class LLVMActions extends NarwhalBaseListener {

    enum VarType {STRING, INT, REAL}

    class Value {
        String content;
        VarType type;

        Value(String content, VarType type) {
            this.content = content;
            this.type = type;
        }
    }

    private HashMap<String, Value> variables = new HashMap<>();


    @Override
    public void exitAssign(NarwhalParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value value = getValue(ctx.value());
        declareVariable(ID, value);
        assignVariable(ID, value, ctx);
    }

    @Override
    public void exitValue(NarwhalParser.ValueContext ctx) {

    }

    @Override
    public void exitPrint(NarwhalParser.PrintContext ctx) {
        String ID = ctx.value().getText();
        if (variables.containsKey(ID)) {
            printVariable(ID);
        } else {
            printConstant(ctx.value());
        }
    }

    @Override
    public void exitProg(NarwhalParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    private Value getValue(NarwhalParser.ValueContext ctx) {
        if (ctx.INT() != null) {
            return new Value(ctx.INT().getText(), VarType.INT);
        } else if (ctx.REAL() != null) {
            return new Value(ctx.REAL().getText(), VarType.REAL);
        } else if (ctx.STRING() != null) {
            return new Value(getTextWithoutQuotes(ctx), VarType.STRING);
        } else {
            Value declaredValue = variables.get(ctx.ID().getText());
            if (declaredValue  == null) {
                error(ctx.getStart().getLine(), "Invalid value.");
                return null;
            } else {
                return new Value(declaredValue.content, declaredValue.type);
            }
        }
    }

    private void declareVariable(String ID, Value value) {
        if (!variables.containsKey(ID)) {
            variables.put(ID, value);
            if (value.type == VarType.INT) {
                LLVMGenerator.declare_i32(ID);
            } else if (value.type == VarType.REAL) {
                LLVMGenerator.declare_double(ID);
            } else if (value.type == VarType.STRING) {
                LLVMGenerator.declare_string(ID, value.content.length());
            }
        }
    }

    private void assignVariable(String ID, Value value, NarwhalParser.AssignContext ctx) {
        if (value.type == VarType.INT) {
            LLVMGenerator.assign_i32(ID, value.content);
        } else if (value.type == VarType.REAL) {
            LLVMGenerator.assign_double(ID, value.content);
        } else if (value.type == VarType.STRING) {
            LLVMGenerator.assign_string(ID, value.content);
        } else {
            error(ctx.getStart().getLine(), "Assign error: " + ID);
        }
    }

    private void printConstant(NarwhalParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            LLVMGenerator.print(getTextWithoutQuotes(ctx));
        } else {
            LLVMGenerator.print(ctx.getText());
        }
    }

    private void printVariable(String ID) {
        if (variables.get(ID).type == VarType.INT) {
            LLVMGenerator.printf_i32(ID);
        } else if (variables.get(ID).type == VarType.REAL) {
            LLVMGenerator.printf_double(ID);
        } else if (variables.get(ID).type == VarType.STRING) {
            LLVMGenerator.printf_string(ID, variables.get(ID).content.length());
        }
    }

    private String getTextWithoutQuotes(NarwhalParser.ValueContext ctx) {
        String text = ctx.STRING().getText();
        return text.substring(1, text.length() - 1);
    }

    private void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }

}
