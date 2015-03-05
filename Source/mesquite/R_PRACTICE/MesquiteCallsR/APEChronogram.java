/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.MesquiteCallsR;


import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.APETree;
import mesquite.lib.*;
import mesquite.lib.duties.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class APEChronogram extends BranchLengthsAltererMult {
	public String getName() {
		return "DEFUNCT NPRS (ape: chronogram)";
	}
	public String getExplanation() {
		return "Calls ape's chronogram function on a tree to apply Sanderson's 1997 non-parametric rate smoothing." ;
	}
	RLinker rLinker;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run:  Problem with R.");
		}
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return sorry(getName() + " was cancelled");
		}
		addMenuItem("Parameters for APE Chronogram...", makeCommand("setParams", this));
		return true;
	}
	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
	public String getParameters(){
		return "Parameters: Scale = " + scale + "; Exponent = " + expo + "; Minimum Edge Length = " + minEdgeLength;
	}
	boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "APE Chronogram",buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("Parameters for APE Chronogram");
		DoubleField scaleField = queryDialog.addDoubleField("Scale", scale, 20);
		IntegerField expField = queryDialog.addIntegerField("Exponent", expo, 20);
		DoubleField meLField = queryDialog.addDoubleField("Minimum Edge Length", minEdgeLength, 20);

		queryDialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0) {
				scale = scaleField.getValue();
				expo = expField.getValue();
				minEdgeLength = meLField.getValue();
				defaultScale = scale;
				defaultExpo = expo;
				defaultMinEdgeLength = minEdgeLength;
			}
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	static double defaultScale = 1;
	static int defaultExpo = 2;
	static double defaultMinEdgeLength = 1e-06;
	double scale = defaultScale;
	int expo = defaultExpo;
	double minEdgeLength = defaultMinEdgeLength;
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setParams " + scale + " " + expo + " " + minEdgeLength);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the parameters for Chronogram", "[scale] [exponent] [Minimum Edge Length]", commandName, "setParams")) {
			MesquiteInteger io = new MesquiteInteger(0);
			double scaleP = MesquiteDouble.fromString(arguments, io);
			int expoP = MesquiteInteger.fromString(arguments, io);
			double melP = MesquiteDouble.fromString(arguments, io);
			if (!MesquiteDouble.isCombinable(scaleP)  && !MesquiteDouble.isCombinable(melP)  && !MesquiteInteger.isCombinable(expoP)){
				if (queryOptions())
					parametersChanged(null);

				return null;
			}
			if (MesquiteDouble.isCombinable(scaleP))
				scale = scaleP;
			if (MesquiteDouble.isCombinable(melP))
				minEdgeLength = melP;
			if (MesquiteInteger.isCombinable(expoP))
				expo = expoP;
			if (!MesquiteThread.isScripting())
				parametersChanged(null);

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree aTree, MesquiteString resultString, boolean notify){
		if (rLinker ==  null)
			return false;
		if (!(aTree instanceof MesquiteTree))
			return false;
		MesquiteTree tree = (MesquiteTree)aTree;
		if (!rLinker.require("ape"))
			return false;
		APETree apeTree = new APETree(tree);
		
		rLinker.sendTreeToR(apeTree, "mTree");

		REXP chrontree = rLinker.eval("chrontree <- chronogram(mTree, scale = " + scale + ", expo = " + expo + ", minEdgeLength = " + minEdgeLength + ")");
		REXP result = rLinker.eval("newick <- write.tree(chrontree, file = \"\", append= FALSE, digits = 10)");
		if (result == null) {
			discreetAlert(getName() + " failed because no result was returned.");
			return false;
		}
		String description = result.asString();
		tree.readTree(description);
		

		return true;
	}
}
