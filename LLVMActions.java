import java.util.HashMap;
import java.util.HashSet;

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
    private HashSet<String> globalNames = new HashSet<>();
    private HashSet<String> localNames = new HashSet<>();
    private boolean global = true;
    private String function = "";

    @Override
    public void enterFunction(NarwhalParser.FunctionContext ctx) {
        global = false;
        function = ctx.ID().getText();
        LLVMGenerator.function_start(function);
    }

    @Override
    public void exitFunction(NarwhalParser.FunctionContext ctx) {
        global = true;
        LLVMGenerator.load_return(ctx.r_return().ID().getText(), globalNames);
        LLVMGenerator.function_end();
        removeLocalVariables();
        localNames = new HashSet<>();
    }

    @Override
    public void exitCall(NarwhalParser.CallContext ctx) {
        String ID = ctx.ID().getText();
        LLVMGenerator.call(ID);
    }

    @Override
    public void exitAssign(NarwhalParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value value = getValue(ctx.value());
        declareVariable(ID, value);
        assignVariable(ID, value, ctx);
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
    public void exitReadInt(NarwhalParser.ReadIntContext ctx) {
        String ID = ctx.ID().getText();
        if (!variables.containsKey(ID)) {
            if (global) {
                globalNames.add(ID);
            }
            LLVMGenerator.declare_i32(ID, global);
            LLVMGenerator.scanf_i32(ID, globalNames);
            variables.put(ID, new Value(ID, VarType.INT));
        } else {
            LLVMGenerator.scanf_i32(ID, globalNames);
        }
    }

    @Override
    public void exitReadReal(NarwhalParser.ReadRealContext ctx) {
        String ID = ctx.ID().getText();
        if (!variables.containsKey(ID)) {
            if (global) {
                globalNames.add(ID);
            }
            LLVMGenerator.declare_double(ID, global);
            LLVMGenerator.scanf_double(ID, globalNames);
            variables.put(ID, new Value(ID, VarType.REAL));
        } else {
            LLVMGenerator.scanf_double(ID, globalNames);
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
            if (declaredValue == null) {
                error(ctx.getStart().getLine(), "Invalid value.");
                return null;
            } else {
                return new Value(declaredValue.content, declaredValue.type);
            }
        }
    }

    private void declareVariable(String ID, Value value) {
        if (!variables.containsKey(ID)) {
            if (value.type != VarType.STRING) {
                variables.put(ID, value);
            }
            if (value.type == VarType.INT) {
                LLVMGenerator.declare_i32(ID, global);
            } else if (value.type == VarType.REAL) {
                LLVMGenerator.declare_double(ID, global);
            }
        }
    }

    private void assignVariable(String ID, Value value, NarwhalParser.AssignContext ctx) {
        if (global) {
            globalNames.add(ID);
        } else if (!globalNames.contains(ID)) {
            localNames.add(ID);
        }

        if (value.type == VarType.INT) {
            LLVMGenerator.assign_i32(ID, value.content, globalNames);
        } else if (value.type == VarType.REAL) {
            LLVMGenerator.assign_double(ID, value.content, globalNames);
        } else if (value.type == VarType.STRING) {
            assignString(ID, value, ctx);
        } else {
            error(ctx.getStart().getLine(), "Assign error: " + ID);
        }
    }

    private void assignString(String ID, Value value, NarwhalParser.AssignContext ctx) {
        if (!variables.containsKey(ID)) {
            LLVMGenerator.assign_string(ID, value.content, global, function);
            variables.put(ID, value);
        } else {
            error(ctx.getStart().getLine(), ID + " is constant variable.");
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
            LLVMGenerator.printf_i32(ID, globalNames);
        } else if (variables.get(ID).type == VarType.REAL) {
            LLVMGenerator.printf_double(ID, globalNames);
        } else if (variables.get(ID).type == VarType.STRING) {
            LLVMGenerator.printf_string(ID, variables.get(ID).content.length(), globalNames, function);
        }
    }

    private String getTextWithoutQuotes(NarwhalParser.ValueContext ctx) {
        String text = ctx.STRING().getText();
        return text.substring(1, text.length() - 1);
    }

    private void removeLocalVariables() {
        for (String id : localNames) {
            variables.remove(id);
        }
    }

    private void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }

}
