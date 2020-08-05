**********************
SeqEvo version 2.0
MT and BY : 2016-08-10
**********************

SequenceEvolver (SeqEvo) is a program written to produce a sequence level design for a given network configuration. 
Network configuration is specified by an input file containing domain information, and an input file containing strand information.
Parameters affecting how the program will run are specified by the user in the input file. 

The default file path for the parameters file is "input.txt"
The default file path for the domain information is "in.domains.txt"
The default file path for the strand information is "in.strands.txt"

A different filepath for the parameters file name can be provided by calling the file with the option <-i> followed by the filename. For example the command to run SeqEvo.jar using the "newInput.txt" input file would be : java -jar SeqEvo.jar -i "newInput.txt"
A different filepath for the domain or strand information can be specified in the parameters file.


*******************
System Requirements
*******************

SeqEvo requires a recent version of Java to be installed ( version 1.6 or newer recommended ).

******************
Running SeqEvo.jar
******************

To run the SeqEvo program, open a command line in the folder containing the SeqEvo.jar file and excecute the command <java -jar SeqEvo2.jar> excluding the <>.
SeqEvo will require a Parameters file (default: input.txt), domains file (default: in.domains.txt), and sequence file (defualt: in.strands.txt) to run.

If the program runs properly, there should be a text output to the command line, and several output files produced. By default, all these files begin with the phrase "out.", but new names or paths can be specified for them using the parameters file.
Depending on the network size and parameters specified, program execution can take anywhere from a few seconds second to several days. It is recomended that the total number of generations (cpg and cpl variables) be set low (1 or 10), and then increased as necessary.
