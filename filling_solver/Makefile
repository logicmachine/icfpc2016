TARGET = FillingSolver.jar
OBJ = Rational.class Geometry.class PartsDecomposer.class Main.class 

.PHONY: all clean

all: $(TARGET)

$(TARGET): $(OBJ)
	jar cvfm $(TARGET) manifest.mf *.class

%.class: src/%.java
	javac -encoding utf8 -d . $<

clean:
	rm -f $(TARGET)
	rm -f *.class
