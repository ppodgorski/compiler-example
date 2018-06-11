import java.util.*;

public class LLVMActions extends NarwhalBaseListener {

    enum VarType {STRING, INT, REAL}

    static class Value {
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
    private Queue<String> infixExpr = new LinkedList<>();
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
        assignVariable(ID, value, ctx.getStart().getLine(), false);
    }

    @Override
    public void exitExprAssign(NarwhalParser.ExprAssignContext ctx) {
        String ID = ctx.ID().getText();
        Value value = MathUtils.eval(infixExpr, globalNames, variables);
        declareVariable(ID, value);
        assignVariable(ID, value, ctx.getStart().getLine(), true);
        infixExpr = new LinkedList<>();
    }

    @Override
    public void exitInt(NarwhalParser.IntContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitReal(NarwhalParser.RealContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitId(NarwhalParser.IdContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitLp(NarwhalParser.LpContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitRp(NarwhalParser.RpContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitAdd(NarwhalParser.AddContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitSub(NarwhalParser.SubContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitMul(NarwhalParser.MulContext ctx) {
        infixExpr.add(ctx.getText());
    }

    @Override
    public void exitDiv(NarwhalParser.DivContext ctx) {
        infixExpr.add(ctx.getText());
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
    public void enterBlockif(NarwhalParser.BlockifContext ctx) {
        LLVMGenerator.if_start();
    }

    @Override
    public void exitBlockif(NarwhalParser.BlockifContext ctx) {
        LLVMGenerator.if_end();
    }

    @Override
    public void exitEqual(NarwhalParser.EqualContext ctx) {
        String ID = ctx.ID().getText();
        String INT = ctx.INT().getText();
        if (variables.containsKey(ID)) {
            LLVMGenerator.icmp(ID, INT, globalNames);
        } else {
            error(ctx.getStart().getLine(), "Unknown variable: " + ID);
        }
    }

    @Override
    public void exitRepetitions(NarwhalParser.RepetitionsContext ctx) {
        String ID = ctx.ID().getText();
        if (variables.containsKey(ID)) {
            if (variables.get(ID).type == VarType.INT) {
                LLVMGenerator.repeat_start(ID, globalNames, false);
            } else if (variables.get(ID).type == VarType.REAL) {
                LLVMGenerator.repeat_start(ID, globalNames, true);
            } else {
                error(ctx.getStart().getLine(), "Repeat value must be an integer or real.");
            }
        } else {
            error(ctx.getStart().getLine(), "Unknown variable: " + ID);
        }
    }

    @Override
    public void exitBlock(NarwhalParser.BlockContext ctx) {
        if (ctx.getParent() instanceof NarwhalParser.RepeatContext) {
            LLVMGenerator.repeat_end();
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
                return new Value(ctx.ID().getText(), declaredValue.type);
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

    private void assignVariable(String ID, Value value, int line, boolean isMathExpr) {
        if (global) {
            globalNames.add(ID);
        } else if (!globalNames.contains(ID)) {
            localNames.add(ID);
        }

        if (value.type == VarType.INT) {
            LLVMGenerator.assign_i32(ID, getValue(value, isMathExpr), globalNames);
        } else if (value.type == VarType.REAL) {
            LLVMGenerator.assign_double(ID, getValue(value, isMathExpr), globalNames);
        } else if (value.type == VarType.STRING) {
            assignString(ID, value, line);
        } else {
            error(line, "Assign error: " + ID);
        }
    }

    private String getValue(Value value, boolean isMathExpr) {
        if (isMathExpr) {
            return value.content;
        }
        if (MathUtils.isNumeric(value.content)) {
            return value.content;
        } else {
            if (value.type == VarType.REAL) {
                LLVMGenerator.load_double(value.content, globalNames);
            } else {
                LLVMGenerator.load_i32(value.content, globalNames);
            }
            return "%" + (LLVMGenerator.reg - 1);
        }
    }

    private void assignString(String ID, Value value, int line) {
        if (!variables.containsKey(ID)) {
            LLVMGenerator.assign_string(ID, value.content, global, function);
            variables.put(ID, value);
        } else {
            error(line, ID + " is constant value.");
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
