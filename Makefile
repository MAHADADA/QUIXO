ifeq ($(shell echo "win"), "win")
	RM = del /s /q
else
	RM = rm -rf
endif

all: program.jar


program.jar: program.java
	mkdir bin
	javac -d bin program.java
	jar cvfe program.jar program -C bin .

test: program.jar
	java -jar program.jar

clean:
	$(RM) bin
	$(RM) program.jar
