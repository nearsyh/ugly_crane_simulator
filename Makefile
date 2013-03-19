all:Simulator.class

Simulator.class:Blocks.java Truck.java CraneArms.java Canvas.java Simulator.java 
	javac Simulator.java

.PHONY:run
run:
	make all
	java Simulator

.PHONY:clean
clean:
	rm -f *.class

.PHONY:build
build:
	make all
