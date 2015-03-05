/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.ape.APEPlotTreeToPDF;

import mesquite.R.MesquiteCallsR.ape.lib.*;
import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.APETree;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/** ======================================================================== */

public class APEPlotTreeToPDF extends TreeUtility {
	RLinker rLinker;
	static APEPlotTreeOptions aO;
	/*.................................................................................................................*/
	 public String getName() {
	return "Plot Tree to PDF (ape: plot)";
	 }
/*.................................................................................................................*/
/** returns an explanation of what the module does.*/
public String getExplanation() {
	return "Plots the tree using ape's plot function and saves to a PDF.";
	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		return true;
	}

	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
 	
	public  void useTree(Tree tree) {
		if (rLinker ==  null)
			return;
		if (aO == null)
			aO = new APEPlotTreeOptions();
		String options = aO.queryPlotTreeOptions(containerOfModule());
		if (StringUtil.blank(options))
			return;
		if (!rLinker.require("ape"))
			return;
		//rLinker.setVerbose(true);
		APETree apeTree = new APETree(tree);
		rLinker.sendTreeToR(apeTree, "mTree");
		String path = MesquiteFile.saveFileAsDialog("Plot tree and save as PDF file");
		rLinker.eval("pdf(file = \"" + path + "\")");
		rLinker.eval("plot(mTree, " + options + ")");
		rLinker.eval("dev.off()");
	}
	
	
	public boolean isSubstantive(){
		return false;
	}
   	 
}

