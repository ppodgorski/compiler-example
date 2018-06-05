import java.util.*;

class MathUtils {

    static LLVMActions.Value eval(Queue<String> infix, HashSet<String> globalNames, HashMap<String, LLVMActions.Value> variables) {
        Queue<String> rpn = infixToRpn(intToReal(infix));
        Stack<String> stack = new Stack<>();

        for (String item : rpn) {
            switch (item) {
                case "+":
                    LLVMGenerator.add_double(stack.pop(), stack.pop());
                    stack.push("%" + (LLVMGenerator.reg - 1));
                    break;
                case "-":
                    String s1 = stack.pop();
                    String s2 = stack.pop();
                    LLVMGenerator.sub_double(s2, s1);
                    stack.push("%" + (LLVMGenerator.reg - 1));
                    break;
                case "*":
                    LLVMGenerator.mul_double(stack.pop(), stack.pop());
                    stack.push("%" + (LLVMGenerator.reg - 1));
                    break;
                case "/":
                    String d1 = stack.pop();
                    String d2 = stack.pop();
                    LLVMGenerator.div_double(d2, d1);
                    stack.push("%" + (LLVMGenerator.reg - 1));
                    break;
                default:
                    if (isNumeric(item)) {
                        stack.push(item);
                    } else {
                        handleVariable(item, stack, variables, globalNames);
                    }
                    break;
            }
        }

        if (allValuesAreIntegers(rpn, variables)) {
            LLVMGenerator.fptosi(stack.pop());
            return new LLVMActions.Value("%" + (LLVMGenerator.reg - 1), LLVMActions.VarType.INT);
        } else {
            return new LLVMActions.Value("%" + (LLVMGenerator.reg - 1), LLVMActions.VarType.REAL);
        }

    }

    private static boolean allValuesAreIntegers (Queue<String> values, HashMap<String, LLVMActions.Value> variables) {
        for (String item : values) {
            if (isNumeric(item)) {
                if (!isInteger(item)) {
                    return false;
                }
            } else if (variables.containsKey(item)) {
                if (variables.get(item).type != LLVMActions.VarType.INT) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void handleVariable(String item, Stack<String> stack,
                                       HashMap<String, LLVMActions.Value> variables, HashSet<String> globalNames) {
        if (variables.containsKey(item)) {
            LLVMActions.Value value = variables.get(item);
            if (value.type == LLVMActions.VarType.INT) {
                LLVMGenerator.load_i32(item, globalNames);
                LLVMGenerator.sitofp("%" + (LLVMGenerator.reg - 1));
            } else {
                LLVMGenerator.load_double(item, globalNames);
            }
            stack.push("%" + (LLVMGenerator.reg - 1));
        } else {
            throw new RuntimeException("Unknown variable: " + item);
        }
    }

    private static Queue<String> intToReal(Queue<String> ints) {
        Queue<String> reals = new LinkedList<>();
        for (String item : ints) {
            if (isInteger(item)) {
                double d = (double) Integer.parseInt(item);
                reals.add(String.valueOf(d));
            } else {
                reals.add(item);
            }
        }
        return reals;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static Queue<String> infixToRpn(Queue<String> infix) {
        Queue<String> rpn = new LinkedList<>();
        Stack<String> stack = new Stack<>();

        for (String item : infix) {
            switch (item) {
                case "+":
                case "-":
                    while (!stack.empty() && (stack.peek().equals("*") || stack.peek().equals("/"))) {
                        rpn.add(stack.pop());
                    }
                    stack.push(item);
                    break;
                case "*":
                case "/":
                    stack.push(item);
                    break;
                case "(":
                    stack.push(item);
                    break;
                case ")":
                    while (!stack.empty() && !stack.peek().equals("(")) {
                        rpn.add(stack.pop());
                    }
                    stack.pop();
                    break;
                default:
                    rpn.add(item);
                    break;
            }
        }

        while (!stack.isEmpty()) {
            rpn.add(stack.pop());
        }

        return rpn;
    }

    static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
