csmart/doc/CSMART-README.txt

This is the Cougaar Society Monitoring, Analysis and Reporting Tool (CSMART), v0.4

Please send comments to cougaar-developers@cougaar.org, or enter bugs on the Cougaar Bugzilla site: http://www.cougaar.org/bugs/

See csmart/bin/README for details on how to run various components, using the supplied scripts.
See csmart/doc/CSMART-Install.txt for instructions on installing and configuratin CSMART.
See csmart/data/README.txt for details on the included data files.

CSMART is a Cougaar module, and so should be installed on a core
Cougaar installation using the latest version of Cougaar

Once installed, the script called "CSMART" will start up the CSMART Client.
Separately, you must install, configure, and start up the Node Server (the "server" module) on each host on
which you wish CSMART to start up a Cougaar node.

Once CSMART is running, see the online help for some simple documentation.

Note that the CSMART Society Monitor tool for viewing the contents of a running Blackboard may be
used separately from the rest of CSMART:
1) Copy the appropriate PSPs from csmart/data/common/default.psps.xml
2) Include the csmart, xerces, grappa, planserver, glm jar files in your various Paths
3) Use the included sample Monitor scripts to run the Society Monitor stand-alone

The usual usage pattern for CSMART:
1) Create a new Society in the Organizer (tree at the bottom of the Frame), from the right-click menu.
-- select Scalability (the scalability module) or ABC (the ABC PlugIns)
2) Select Config Builder to configure your society
-- changes are saved automatically
3) Create a new Experiment with your Society (right-click on the society in the Organizer)
4) Select Experiment Builder to edit your experiment (you may add impacts, for example)
5) Select Experiment Controller to assign (via drag-and-drop) Agents to Nodes, Nodes to Hosts 
-- create new Nodes or Hosts via a right-click menu
6) Hit Run to start your society running.
-- The STDOUT is displayed, one tabbed-frame per Node.
-- The colored circle at the top indicates load on the Node
-- Some Societies' Nodes will exit on their own accord when done. Others will simply quiesce
7) While your society is still running, you may select Society Monitor to view the contents of 
the Blackboard for that society
8) When the society has finished running, if you included some Metrics Collection (Scalability and ABC both do), you may view those
-- Select Analyzer
-- Specify a directory for saving Metrics files to, if not already done
-- Hit Excel button to launch excel on a specified metrics file

Known Problems
1) If the area under the menu is not being refreshed in the CSMART Performance Analyzer tool after the File-Open menu item is invoked, then you need to update your version of Java to jdk1.3.1_01 (see http://developer.java.sun.com/developer/bugParade/bugs/4189244.html).
