csmart/data/ul-test

A simple 3 Agent society for testing attacks and kinetic events
and their impacts.
The Victim is modeled off of the Agent in singleCluster.
Modify the RealWorldEvents.xml file to modify the attacks made.
This society may serve as an example of how impacts are injected into
a Cougaar society using the ABC impacts models. There is a Generator
Agent which generates RealWorldEvents from the given XML file. These
are sent to the Transducer, which generates a model of the society
(from the given Society.dat file), and hands that model to each of the
RWEs in turn. They generate InfrastructureEvents which are sent to the
appropriate Agents in the rest of the society.

See the Event Modelling section of the User's Guide for more
information.
For other examples of RealWorldEvent scripts, see data/rwe-scripts.
Experiment with using these scripts with this society, or by adding an
"ABC Impact" to your CSMART society.
