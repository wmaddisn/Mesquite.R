/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.diversitree.SimulateBirthDeath;


import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class SimulateBirthDeath extends TreeSimulate  {
	RLinker rLinker;
	double birthRate = 0.3;
	double deathRate = 0.1;
	int retryNum = 100;
	MesquiteDouble b, d;
	MesquiteInteger retries;
	Tree tree = null;
	SimOptions aOptions = new SimOptions();
	/*.................................................................................................................*/
	public String getName() {
		return "Simulate B/D Trees (diversitree: tree.bd)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "(From diversitree package in R): Generates tree by simple birth/death model with a constant rate of speciation (birth) and of extinction (death).";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		b = new MesquiteDouble(birthRate);
		d = new MesquiteDouble(deathRate);
		retries = new MesquiteInteger(retryNum);
		if (!MesquiteThread.isScripting()){
			 aOptions.queryOptions(containerOfModule(), b, d, retries);
		}
		addMenuItem( "Birth/Death Options...", makeCommand("setOptions",  this));
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
		temp.addLine("setOptions " + b + " " + d); 

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the options for tree simulation", "[]", commandName, "setOptions")) {
			if (StringUtil.blank(arguments)) {
				aOptions.queryOptions(containerOfModule(), b, d, retries);
			}
			else {
				b.setValue(parser.getFirstToken(arguments));
				d.setValue(parser.getNextToken());
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		return MesquiteInteger.infinite;
	}
	/*.................................................................................................................*/
	public Tree getSimulatedTree(Taxa taxa, Tree baseTree, int treeNumber, ObjectContainer extra, MesquiteLong seed) { //todo: should be two seeds passed!
		if (rLinker ==  null)
			return null;
		if (!rLinker.require("diversitree"))
			return null;
		REXP bdtree = null;
		REXP result = null;
		int count = 0;
		rLinker.eval("set.seed(" + seed.getValue() + ")");
		String options = "c(" + b.getValue() + ",  " + d.getValue() + ") ";

		while (result == null && count++<retries.getValue()){
			bdtree = rLinker.eval("bdtree <- tree.bd(" + options + ", max.taxa = " + taxa.getNumTaxa() + ", max.t=Inf, include.extinct = FALSE)");
			if (bdtree != null){
				rLinker.assign("my.names", rLinker.getTaxonLabelsVector(taxa));
				rLinker.eval("my.names <- sample(my.names)");
				rLinker.eval("bdtree$tip.label <- my.names");
				rLinker.eval("bdtree$node.label <- NULL");

				//rLinker.eval("print(bdtree)");
				result = rLinker.eval("newick <- write.tree(bdtree, file = \"\", append= FALSE, digits = 10)");
			}
		}
		if (result == null)
			MesquiteMessage.println("Tree simulation failed; tree apparently went extinct " + retries.getValue() +" times.");

		rLinker.eval("print(newick)");
		if (result == null)
			return null;
		String description = result.asString();
		//	description = StringUtil.replace(description, "sp", "");

		MesquiteTree t = new MesquiteTree(taxa, description);
		return t;
	}


}

class SimOptions  {

	public void queryOptions(MesquiteWindow window, MesquiteDouble b, MesquiteDouble d, MesquiteInteger retries){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(window, "Birth/Death (diversitree)",buttonPressed);
		dialog.addLargeOrSmallTextLabel("Options for diversitree's Birth Death Simulation");
		//	if (queryDialog.isInWizard())
		//		queryDialog.appendToHelpString("<h3>Initial guesses for alpha and sigma parameters</h3>Reasonable defaults might be 0 for alpha and 1 for sigma.");
		DoubleField bF = dialog.addDoubleField("Birth Rate", b.getValue(), 20);
		DoubleField dF = dialog.addDoubleField ("Death Rate", d.getValue(), 20);
		IntegerField c = dialog.addIntegerField ("Number of retries if extinct", retries.getValue(), 20);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0) {

			b.setValue(bF.getValue());

			d.setValue(dF.getValue());
			retries.setValue(c.getValue());

		}

		dialog.dispose();
	}

}
