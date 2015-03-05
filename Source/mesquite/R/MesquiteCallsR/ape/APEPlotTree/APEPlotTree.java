/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.ape.APEPlotTree;


import mesquite.R.MesquiteCallsR.ape.lib.APEPlotTreeOptions;
import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class APEPlotTree extends TreeWindowAssistantN  {
	RLinker rLinker;
	String options = "";
	Tree tree = null;
	APEPlotTreeOptions aOptions = new APEPlotTreeOptions();
	/*.................................................................................................................*/
	public String getName() {
		return "Plot Tree (ape: plot)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Plots a single tree (the same as in a tree window) in ape." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		if (!MesquiteThread.isScripting()){
			options = aOptions.queryPlotTreeOptions(containerOfModule());
		}
		addMenuItem( "ape Plot Tree Options...", makeCommand("setOptions",  this));
		addMenuItem( "ape Plot Tree to PDF File...", makeCommand("printTree",  this));
		addMenuItem( "Disconnect ape Plot Tree", makeCommand("disconnect",  this));
		return true;
	}

	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setOptions " + StringUtil.tokenize(options)); 

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the options for tree plotting", "[]", commandName, "setOptions")) {
			if (StringUtil.blank(arguments)) {
				options = aOptions.queryPlotTreeOptions(containerOfModule());
				setTree(tree);
			}
			else
				options = parser.getFirstToken(arguments);
		}
		else if (checker.compare(this.getClass(), "Disconnects tree plotting", "[]", commandName, "disconnect")) {
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Print tree to PDF", "[]", commandName, "printTree")) {
			APETree apeTree = new APETree(tree);
			rLinker.sendTreeToR(apeTree, "mTree");
			String path = MesquiteFile.saveFileAsDialog("Plot tree and save as PDF file");
			rLinker.eval("pdf(file = \"" + path + "\")");
			rLinker.eval("plot(mTree, " + options + ")");
			rLinker.eval("dev.off()");
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (rLinker ==  null)
			return;
		this.tree = tree;
		if (StringUtil.blank(options))
			return;
		if (!rLinker.require("ape"))
			return;
		APETree apeTree = new APETree(tree);
		
		rLinker.sendTreeToR(apeTree, "mTree");
		
		rLinker.eval("plot(mTree, " + options + ")");

	}


}


