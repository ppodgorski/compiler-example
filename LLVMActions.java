import java.util.HashMap;

public class LLVMActions extends NarwhalBaseListener {

    HashMap<String, String> memory = new HashMap<String, String>();
    String value;

    @Override
    public void exitAssign(NarwhalParser.AssignContext ctx) {
        String tmp = ctx.STRING().getText();
        tmp = tmp.substring(1, tmp.length() - 1);
        memory.put(ctx.ID().getText(), tmp);
    }

    @Override
    public void exitProg(NarwhalParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitValue(NarwhalParser.ValueContext ctx) {
        if (ctx.ID() != null) {
            value = memory.get(ctx.ID().getText());
        }
        if (ctx.STRING() != null) {
            String tmp = ctx.STRING().getText();
            value = tmp.substring(1, tmp.length() - 1);
        }
    }

    @Override
    public void exitPrint(NarwhalParser.PrintContext ctx) {
        LLVMGenerator.print(value);
    }

}
