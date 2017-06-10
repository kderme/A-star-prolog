## About
We changed our https://github.com/kderme/A-star/ repo and integrated prolog.
Now instead of trating clients, taxis and nodes as Java objects, we used a prolog parser to 
save them in memory and make queries on them. Queries are done by Java using Jiprolog, a prolog compiler written in Java.

Heuristic function become much more complex, as we took into consideration the traffic, the lighing etc. The program can be easily extended by changing the importance of every parameter in the heuristic function.

## COMPILE
	You only need to compile ./src/*.java files
	Don`t forget to add TN2016-T2/JIProlog/jipconsole.jar to your build path
	
## EXECUTE
	Running directory should be here, not in src folder
	optins include
	-i inputDirPath
	-o outputDirPath (write priviledges)
	-a algorithm (<0,1,2>)
		0:precomputes staff, to do things faster
		1:fastest. But uses java for hops
		2:slowest (not recomended)

## Examples
	$java Main -o output1 -i input1 -a 1
	$java Main -a 2
	$java Main -i path/to/input
	$java Main

###### path/to/input/files must have:
	client.csv
	nodes.csv
	taxis.csv
	lines.csv
	traffic.csv
