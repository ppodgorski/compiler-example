ANTLR=../antlr-4.7.1-complete.jar

all: clean generate compile test

generate:
	java -jar $(ANTLR) -o output Narwhal.g4

compile:
	javac -cp $(ANTLR):output:. Main.java

test:
	java -cp $(ANTLR):output:. Main test.nw > test.ll
	clang test.ll

clean:
	rm -f test.ll
	rm -f *.class
	rm -rf output
	rm -f a.out