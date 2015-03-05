/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.basic.TranslateToPhylo;


import mesquite.R.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class TranslateToPhylo extends TreeUtility  {
	APETree apeTree;
	Tree tree = null;
	/*.................................................................................................................*/
	public String getName() {
		return "Translate to phylo (ape tree)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Translate the tree in a tree window to the ape phylo format";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public   void visitNodes(int node, Tree tree, NumberArray result) {
		result.setValue(node, tree.tallestPathAboveNode(node, 0));

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			visitNodes(d, tree, result);
	}
	boolean hasZeroLengthBranches(Tree tree, int node){
		if (tree.getBranchLength(node) == 0)
			return true;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			if (hasZeroLengthBranches(tree, d))
				return true;
		return false;
	}
	public  void useTree(Tree tree) {
		apeTree = new APETree(tree);
		MesquiteMessage.println("Tree written as phylo:\n" + apeTree.toRCommand() + "\n\n");
		if (hasZeroLengthBranches(tree, tree.getRoot()))
			MesquiteMessage.println("Tree written as phylo, zero branch lengths adjusted:\n" + apeTree.toRCommand(true) + "\n\n");
		MesquiteMessage.println("Correspondence of node numbers, Mesquite and phylo:\n" + apeTree.dump() + "\n\n");
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Gets strings describing ape tree", "[]", commandName, "getApeTreeTables")) {
			if (apeTree != null)
				return apeTree.dump();
		}
		else if (checker.compare(this.getClass(), "Gets R command for ape tree", "[]", commandName, "getApeTreeCommand")) {
			if (apeTree != null)
				return apeTree.toRCommand();
		}
		else if (checker.compare(this.getClass(), "Gets R command for ape tree, adjusting for zero length branches", "[]", commandName, "getApeTreeCommandZLBA")) {
			if (apeTree != null)
				return apeTree.toRCommand(true);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
}


