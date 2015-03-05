/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.MesquiteCallsR.geiger.GEIGERDisparity;

import org.rosuda.JRI.*;

import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.cont.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**Suppliies numbers for each node of a tree.*/

public class GEIGERDisparity extends NumbersForNodesAndMatrix {
	public String getName() {
		return "Disparity (geiger: tip.disparity)";
	}
	public String getExplanation() {
		return "Reports the results from the tip.disparity function from the geiger package of R.";
	}

	RLinker rLinker;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		return true;
	}

	/*.................................................................................................................*/
	public Class getCharacterClass() {
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}

	public CompatibilityTest getCompatibilityTest(){
		return new ContinuousStateTest();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns reconstructor", null, commandName, "getReconstructor")) {
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, MCharactersDistribution data, NumberArray result, MesquiteString resultString) {
		if (tree==null || data==null)
			return;
		if (!( data instanceof MContinuousDistribution)){
			return;
		}
		MContinuousDistribution cMatrix = (MContinuousDistribution)data;
		if (!rLinker.require("geiger"))
			return;
		//rLinker.setVerbose(true);
		APETree apeTree = new APETree(tree);
		rLinker.sendTreeToR(apeTree, "phy");
		int[] mesquiteNodes = apeTree.getNodeTable(2);
		//packing the character matrix in terms of terminal taxon numbering of APE tree
		logln("=============\nAbout to call R for " + getName());

		double[] rMatrix = new double[cMatrix.getNumChars()*(apeTree.numTerminals)];
		String[] namesMatrix = new String[apeTree.numTerminals];
		boolean[] toDelete = new boolean[apeTree.numTerminals];
		for (int ic = 0; ic<cMatrix.getNumChars(); ic++){
			int terminalCounter= -1;
			for (int itn = 0; itn<apeTree.numNodes-1; itn++){
				if (tree.nodeIsTerminal(mesquiteNodes[itn])) {
					int iTaxon = tree.taxonNumberOfNode(mesquiteNodes[itn]);
					terminalCounter++;
					double state = cMatrix.getState(ic, iTaxon);
					if (!MesquiteDouble.isCombinable(state)){
						state = Double.NaN;
						toDelete[terminalCounter] = true;
					}
					namesMatrix[terminalCounter] = tree.getTaxa().getTaxonName(iTaxon);
					rMatrix[ic*(apeTree.numTerminals) + terminalCounter] = state;
				}
			}
		}

		rLinker.assign("data",rMatrix);
		rLinker.eval("dim(data) <- c(" + (apeTree.numTerminals) + ", " + cMatrix.getNumChars() + ")");
		String rowNamesString = "";
		boolean first = true;
		for (int it = 0; it< namesMatrix.length; it++){
			if (!first)
				rowNamesString += ", ";
			first = false;
			rowNamesString += "\"" + namesMatrix[it] + "\"";
		}
		rLinker.eval("rownames(data)<-c(" + rowNamesString + ")");
		/**/
		for (int it = 0; it< toDelete.length; it++){
			if (toDelete[it])
				rLinker.eval("data <-data[-" + (it+1) + ",]");
		}
		/**/

		rLinker.eval("data <- as.data.frame(data)");
		rLinker.eval("data[is.nan(data)] <- NA");
		rLinker.eval("print(data)");
		REXP rtree = rLinker.eval("newick <- write.tree(phy, file = \"\", append= FALSE, multi.line = FALSE, digits = 10)");
		REXP R_disparities = rLinker.eval("tip.disparity(phy, data, data.names=NULL, disp = \"avg.sq\")");
		try{
			Debugg.println("result " + R_disparities);
			Debugg.println("result2 " + R_disparities.getContent());
			double[] disparities = (double[]) R_disparities.getContent();
			Debugg.println(DoubleArray.toString(disparities));
			int internalCounter = -1;
			for (int itn = 0; itn<apeTree.numNodes-1; itn++){
				if (tree.nodeIsInternal(mesquiteNodes[itn])) {
					internalCounter++;
					result.setValue(mesquiteNodes[itn], disparities[internalCounter]);
				}
			}
			
		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("Exception thrown in processing R results");
		}



	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, MCharactersDistribution observedStates){
		if (tree == null)
			return;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

}
