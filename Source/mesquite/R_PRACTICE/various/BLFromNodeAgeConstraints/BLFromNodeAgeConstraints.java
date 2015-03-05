/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.various.BLFromNodeAgeConstraints;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class BLFromNodeAgeConstraints extends BranchLengthsAltererMult {
	double resultNum;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		double[] ages = new double[tree.getNumNodeSpaces()];
		setByDeepest(tree, tree.getRoot(), ages);
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	NameReference nodeAgeConstrRef = NameReference.getNameReference("nodeAgeConstraints");
	double getConstraint(Tree tree, int node){
		String constraint = (String)tree.getAssociatedObject(nodeAgeConstrRef, node);
		if (!StringUtil.blank(constraint) && constraint.indexOf("age")>=0){
			parser.setString(constraint);
			parser.setPosition(constraint.indexOf("age") + 3);
			parser.getNextToken(); //eating up "="
			double age = MesquiteDouble.fromString(parser);
			if (MesquiteDouble.isCombinable(age))
				return age;		
		}
		return MesquiteDouble.unassigned;
	}
	double spacer  = 1;
	/*.................................................................................................................*/
	private void setByDeepest(AdjustableTree tree, int node, double[] ages){
		ages[node] = getConstraint(tree, node);
		if (tree.nodeIsTerminal(node) && !MesquiteDouble.isCombinable(ages[node])) {
			ages[node] = 0;
		}
		double maxDepth = 0;
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			setByDeepest(tree, daughter, ages);
			if (maxDepth<ages[daughter])
				maxDepth = ages[daughter];
		}
		if (!MesquiteDouble.isCombinable(ages[node]))
				ages[node] = maxDepth + spacer;
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			tree.setBranchLength(daughter, ages[node] - ages[daughter], false);
		}
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Enforce Node Age Constraints";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Uses node age constraints to set the branch lengths on the tree." ;
	}
}
