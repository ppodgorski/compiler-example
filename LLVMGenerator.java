class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static int str_i = 0;

    static void print(String text) {
        int str_len = text.length();
        String str_type = "[" + (str_len + 2) + " x i8]";
        header_text += "@str" + str_i + " = constant" + str_type + " c\"" + text + "\\0A\\00\"\n";
        main_text += "call i32 (i8*, ...) @printf(i8* getelementptr inbounds ( " + str_type + ", " + str_type + "* @str" + str_i + ", i32 0, i32 0))\n";
        str_i++;
    }

    static String generate() {
        String text;
        text = "declare i32 @printf(i8*, ...)\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }

}
