

Parameter beim Aufruf des Programms:
	nach java DesignAnalyzer müssen 3 oder 4 (je nach dem, ob routing file angegeben wird) volle Dateipfade in Linux-Notation (/ statt \) angegeben werden. Die Files sollen in folgender
	Reihenfolge angegeben werden: *.net *.arch *.p (und optional) *.r 
	Darauf folgend dürfen Parameterpaare gemäß der Bezeichnung in der Aufgabenstellung wie zum Beispiel "-W 3" genutzt werden. Die Reihenfolge untereinander ist irrelevant.
	
	javac -d bin -sourcepath src -cp lib/lib1.jar;lib/lib2.jar src/com/example/Application.java