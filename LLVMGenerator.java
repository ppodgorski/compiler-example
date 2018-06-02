class LLVMGenerator {

    private static String header_text = "";
    private static String main_text = "";
    private static int str_i = 0;
    private static int reg = 1;

    static String generate(){
        String text = "";
        text += "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* nocapture writeonly, i8* nocapture readonly, i64, i32, i1)\n";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i32 @scanf(i8*, ...)\n";
        text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n";
        text += "@strp = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }

    static void print(String text) {
        int str_len = text.length();
        String str_type = "[" + (str_len + 2) + " x i8]";
        header_text += "@str" + str_i + " = constant" + str_type + " c\"" + text + "\\0A\\00\"\n";
        main_text +=  "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ( " + str_type + ", " + str_type + "* @str" + str_i + ", i32 0, i32 0))\n";
        str_i++;
        reg++;
    }

    static void printf_i32(String id) {
        main_text += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_double(String id) {
        main_text += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_string(String id, int length) {
        main_text += "%" + reg + " = getelementptr inbounds [" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* %" + id + ", i32 0, i32 0\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void scanf_i32(String id) {
        main_text += "store i32 0, i32* %" + id + "\n";
        main_text += "%" + reg + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    static void declare_i32(String id) {
        main_text += "%" + id + " = alloca i32\n";
    }

    static void declare_double(String id) {
        main_text += "%" + id + " = alloca double\n";
    }

    static void declare_string(String id, int length) {
        main_text += "%" + id + " = alloca [" + (length + 1) + " x i8]\n";
    }

    static void assign_i32(String id, String value) {
        main_text += "store i32 " + value + ", i32* %" + id + "\n";
    }

    static void assign_double(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
    }

    static void assign_string(String id, String text) {
        int len = text.length() + 1;
        String str_type = "[" + len + " x i8]";
        header_text += "@" + id + " = constant" + str_type + " c\"" + text + "\\00\"\n";
        main_text += "%" + reg + " = bitcast [" + len + " x i8]* %" + id + " to i8*\n";
        reg++;
        main_text += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (reg - 1) + ", i8* getelementptr inbounds ([" + len + " x i8], [" + len + " x i8]* @" + id + ", i32 0, i32 0), i64 " + len + ", i32 1, i1 false)\n";
    }

}
