import java.util.HashMap;

public class LLVMActions extends NarwhalBaseListener {

    private HashMap<String, String> variables = new HashMap<>();

    private String value;

    @Override
    public void exitAssign(NarwhalParser.AssignContext ctx) {
        if (ctx.value().STRING() != null) {
            String tmp = ctx.value().STRING().getText();
            tmp = tmp.substring(1, tmp.length() - 1);
            variables.put(ctx.ID().getText(), tmp);
        } else if (ctx.value().INT() != null) {
            String tmp = ctx.value().INT().getText();
            variables.put(ctx.ID().getText(), tmp);
        }
    }

    @Override
    public void exitProg(NarwhalParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitValue(NarwhalParser.ValueContext ctx) {
        if (ctx.ID() != null) {
            value = variables.get(ctx.ID().getText());
        }
        if (ctx.STRING() != null) {
            String tmp = ctx.STRING().getText();
            value = tmp.substring(1, tmp.length() - 1);
        }
        if (ctx.INT() != null) {
            value = ctx.INT().getText();
        }
    }

    @Override
    public void exitPrint(NarwhalParser.PrintContext ctx) {
        LLVMGenerator.print(value);
    }

}
