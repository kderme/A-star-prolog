###COMPILE
	You only need to compile ./src/*.java files
	Don`t forget to add TN2016-T2/JIProlog/jipconsole.jar to your build path
	
###EXECUTE
	You should run programs here not in src folder
	$java Main [optins]
	-i inputDirPath
	-o outputDirPath (write priviledges)
	-a algorithm (<0,1,2>)
		0:precomputes staff, to do things faster
		1:fastest. But uses java for hops
		2:slowest (not recomended)

##examples
	$java Main -o output1 -i input1 -a 1
	$java Main -a 2
	$java Main -i path/to/input
	$java Main

######path/to/input/files must have:
	client.csv
	nodes.csv
	taxis.csv
	lines.csv
	traffic.csv