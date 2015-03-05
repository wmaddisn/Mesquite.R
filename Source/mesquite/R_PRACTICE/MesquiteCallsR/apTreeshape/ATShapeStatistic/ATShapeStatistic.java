/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/

package mesquite.R_PRACTICE.MesquiteCallsR.apTreeshape.ATShapeStatistic;



import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class ATShapeStatistic extends NumberForTree {


	public String getName() {
		return "apTreeshape shape.statistic";
	}
	public String getExplanation() {
		return "apTreeshape shape.statistic.";
	}
	RLinker rLinker;

	MesquiteBoolean verbose;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		verbose = new MesquiteBoolean(false);
		addCheckMenuItem( null, "Show R Console Output", makeCommand("toggleVerbose", this), verbose);
		return true;
	}
	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	/** Returns the R packages needed*/
	public String[] packagesRequired(){
		return new String[]{"apTreeShape"};
	}
	/*===== For NumberForItem interface ======*/
	public boolean returnsMultipleValues(){
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("toggleVerbose " + verbose.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not to show the results from the R console", "[on = show; off]", commandName, "toggleVerbose")) {

			verbose.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(MesquiteWindow.class, "Forces a recalculation", null, commandName, "recalculate")) {

			parametersChanged(null);

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, CharacterDistribution matrix){
	}

	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree == null)
			return;
		result.setToUnassigned();
		rLinker.setEchoRConsole(verbose.getValue());
		if (verbose.getValue())
			logln("=============\nAbout to call R for " + getName());
		if (!rLinker.require("apTreeShape"))
			return;
		APETree apeTree = new APETree(tree);

		rLinker.sendTreeToR(apeTree, "mTree");
		rLinker.eval("ts <- as.treeshape(mTree, NULL)");
		try {
		REXP val =rLinker.eval("shape.statistic(ts, norm = NULL)");
		double[] d = (double[])val.getContent();
		if (d != null && d.length>0){
			result.setValue(d[0]);
			if (resultString != null){
				resultString.setValue("shape.statistic from apTreeshape: " + MesquiteDouble.toStringDigitsSpecified(d[0], 6));
			}
		}
		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("Error in recovering results from R");
		}
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	public boolean showCitation(){
		return true;
	}
}
