class LLVMGenerator {

    private static String header_text = "";
    private static String main_text = "";
    private static String buffer = "";
    private static int str_i = 0;
    private static int reg = 1;
    private static int main_reg = 1;

    static String generate() {
        main_text += buffer;
        formatMainText();
        String text = "";
        text += "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* nocapture writeonly, i8* nocapture readonly, i64, i32, i1)\n";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i32 @scanf(i8*, ...)\n";
        text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n";
        text += "@strsi = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strsd = constant [4 x i8] c\"%lf\\00\"\n";
        text += "\n";
        text += header_text;
        text += "define i32 @main() nounwind {\n";
        text += main_text;
        text += "  ret i32 0\n";
        text += "}\n";
        return text;
    }

    static void function_start(String id) {
        main_text += buffer;
        main_reg = reg;
        buffer = "define i32 @" + id + "() nounwind {\n";
        reg = 1;
    }

    static void function_end() {
        buffer += "ret i32 %" + (reg - 1) + "\n";
        formatBuffer();
        buffer += "}\n\n";
        header_text += buffer;
        buffer = "";
        reg = main_reg;
    }

    static void print(String text) {
        int str_len = text.length();
        String str_type = "[" + (str_len + 2) + " x i8]";
        header_text += "@str" + str_i + " = constant" + str_type + " c\"" + text + "\\0A\\00\"\n";
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ( " + str_type + ", " + str_type + "* @str" + str_i + ", i32 0, i32 0))\n";
        str_i++;
        reg++;
    }

    static void printf_i32(String id) {
        buffer += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_double(String id) {
        buffer += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_string(String id, int length) {
        buffer += "%" + reg + " = getelementptr inbounds [" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* %" + id + ", i32 0, i32 0\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void scanf_i32(String id) {
        assign_i32(id, "0");
        buffer += "%" + reg + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strsi, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    static void scanf_double(String id) {
        assign_double(id, "0.0");
        buffer += "%" + reg + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strsd, i32 0, i32 0), double* %" + id + ")\n";
        reg++;
    }

    static void declare_i32(String id) {
        buffer += "%" + id + " = alloca i32\n";
    }

    static void declare_double(String id) {
        buffer += "%" + id + " = alloca double\n";
    }

    static void declare_string(String id, int length) {
        buffer += "%" + id + " = alloca [" + (length + 1) + " x i8]\n";
    }

    static void assign_i32(String id, String value) {
        buffer += "store i32 " + value + ", i32* %" + id + "\n";
    }

    static void assign_double(String id, String value) {
        buffer += "store double " + value + ", double* %" + id + "\n";
    }

    static void assign_string(String id, String text) {
        int len = text.length() + 1;
        String str_type = "[" + len + " x i8]";
        header_text += "@" + id + " = constant" + str_type + " c\"" + text + "\\00\"\n";
        buffer += "%" + reg + " = bitcast [" + len + " x i8]* %" + id + " to i8*\n";
        reg++;
        buffer += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (reg - 1) + ", i8* getelementptr inbounds ([" + len + " x i8], [" + len + " x i8]* @" + id + ", i32 0, i32 0), i64 " + len + ", i32 1, i1 false)\n";
    }

    private static void formatBuffer() {
        String[] lines = buffer.split("\n");
        StringBuilder sb = new StringBuilder();
        sb.append(lines[0]).append("\n");
        for (int i = 1; i < lines.length; i++) {
            sb.append("  ").append(lines[i]).append("\n");
        }
        buffer = sb.toString();
    }

    private static void formatMainText() {
        String[] lines = main_text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append("  ").append(line).append("\n");
        }
        main_text = sb.toString();
    }

}
