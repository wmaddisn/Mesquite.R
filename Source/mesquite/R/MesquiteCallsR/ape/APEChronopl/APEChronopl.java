/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.ape.APEChronopl;

/* TODO: give user the option to adjust zero-length branches or not */

import java.awt.*;

import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.APETree;
import mesquite.lib.*;
import mesquite.lib.duties.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class APEChronopl extends BranchLengthsAltererMult {
	static double defaultLambda = 2.0;
	static boolean defaultCrossValidation = false;
	double lambda = defaultLambda;
	MesquiteBoolean crossValidation = new MesquiteBoolean(defaultCrossValidation);

	public String getName() {
		return "Penalized Likelihood (ape: chronopl)";
	}
	public String getExplanation() {
		return "Calls ape's chronopl function on a tree to apply Sanderson's 2002 Penalized Likelihood method for smoothing rates to yield a dated tree." ;
	}
	RLinker rLinker;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return sorry(getName() + " was cancelled");
		}
		addMenuItem("Parameters for ape Chronopl...", makeCommand("setParams", this));
		return true;
	}
	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	public String getParameters(){
		return "Parameters: Lambda = " + lambda + "; Cross validation? = " + crossValidation;
	}
	/*.................................................................................................................*/
	boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		mesquite.lib.ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "ape chronopl",buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("Parameters for ape chronopl");
		if (queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>Penalized Likelihood</h3>Node ages can be set using anchor tool.");
		DoubleField lambdaField = queryDialog.addDoubleField("Lambda", lambda, 20);
		Checkbox cvField = queryDialog.addCheckBox("Cross Validate", crossValidation.getValue());

		queryDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0) {
			lambda = lambdaField.getValue();
			crossValidation.setValue(cvField.getState());
			defaultLambda = lambda;
			defaultCrossValidation = crossValidation.getValue();
		}
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setParams " + lambda + " " + crossValidation);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the parameters for " + getName(), "[scale] [exponent] [Minimum Edge Length]", commandName, "setParams")) {
			MesquiteInteger io = new MesquiteInteger(0);
			double lambdaP = MesquiteDouble.fromString(arguments, io);
			String token = ParseUtil.getToken(arguments, io);
			boolean cvP = MesquiteBoolean.fromTrueFalseString(token);
			if (!MesquiteDouble.isCombinable(lambdaP)){
				if (queryOptions())
					parametersChanged(null);

				return null;
			}
			if (MesquiteDouble.isCombinable(lambdaP))
				lambda = lambdaP;
			crossValidation.setValue(cvP);
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
	NameReference nodeAgeConstrRef = NameReference.getNameReference("nodeAgeConstraints");
	String getConstraints(Tree tree, int node){
		return (String)tree.getAssociatedObject(nodeAgeConstrRef, node);
	}
	void getNodeAges(Tree tree, int node, double[] minAges,  double[] maxAges){
		String arguments = getConstraints(tree, node);
		if (!StringUtil.blank(arguments)){
			String minS = parser.getFirstToken(arguments);
			String to = parser.getNextToken();

			String maxS = parser.getNextToken();
			if (to != null && to.equals("+")){  //minimum
				double min = MesquiteDouble.fromString(minS);
				if (MesquiteDouble.isCombinable(min)) {
					minAges[node] = min;
				}
			}
			else {
				if (to != null && to.length()>1 && to.charAt(0) == '-')
					maxS = to.substring(1, to.length());
				double min = MesquiteDouble.fromString(minS);
				double max = MesquiteDouble.fromString(maxS);
				if (MesquiteDouble.isCombinable(min)) {
					if (MesquiteDouble.isCombinable(max)){
						minAges[node] = min;
						maxAges[node] = max;
					}
					else {
						minAges[node] = min;
						maxAges[node] = max;
					}
				}
			}

		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			getNodeAges(tree, d, minAges, maxAges);
		}
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

		rLinker.sendTreeToR(apeTree, "mTree", true);

		double[] minAges = new double[aTree.getNumNodeSpaces()];
		DoubleArray.deassignArray(minAges);
		double[] maxAges = new double[aTree.getNumNodeSpaces()];
		DoubleArray.deassignArray(maxAges);
		getNodeAges(aTree, aTree.getRoot(), minAges, maxAges);
		String nodeMin = "age.min = c(";
		String nodeMax = "age.max = c(";
		String nodes = "c(";
		boolean first = true;
		int count = 0;
		int which = -1;
		double whichMinAge = -1;
		double whichMaxAge = -1;
		boolean someMax = false;
		for (int i=0; i<minAges.length; i++){
			int apeNode = apeTree.getAPENode(i);
			if (apeNode>=0){
				if (MesquiteDouble.isCombinable(minAges[i])){
					count++;
					which = apeNode;
					whichMinAge = minAges[i];
					if (!first){
						nodeMin += ",";
						nodeMax += ",";
						nodes += ",";
					}
					first = false;
					nodeMin += " " + minAges[i];
					if (MesquiteDouble.isCombinable(maxAges[i])){
						whichMaxAge = maxAges[i];
						nodeMax += " " + maxAges[i];
						someMax = true;
					}
					else {
						whichMaxAge = minAges[i];
						nodeMax += " " + minAges[i];
					}
					nodes += " " + apeNode;
				}


			}
		}
		nodeMin += ")";
		nodeMax += ")";

		nodes += ")";
		if (count == 1){
			nodeMin = Double.toString(whichMinAge);
			nodeMax = Double.toString(whichMaxAge);
			nodes = Integer.toString(which);
		}

		String chronCommand =   "chrontree <- chronopl(mTree, " + lambda;

		if (count>0) {
			if (someMax)
				chronCommand += ", " + nodeMin + ", " + nodeMax + ", node = " + nodes;
			else
				chronCommand += ", " + nodeMin + ", age.max = NULL , node = " + nodes;
		}
		else
			chronCommand += ", age.min = 1 , age.max = NULL, node = \"root\"";
		chronCommand +=  ", S = 100, tol = 0.001, iter.max = 100, eval.max = 100, CV = " + crossValidation.toString() + ")";
		boolean was = rLinker.echoRConsole;
		rLinker.setEchoRConsole(true);
		REXP chrontree = rLinker.eval(chronCommand);
		rLinker.setEchoRConsole(was);
		REXP result = rLinker.eval("newick <- write.tree(chrontree, file = \"\", append= FALSE, digits = 10)");
		if (result == null) {
			logln("no tree returned! ");
			return false;
		}
		else {
			String description = result.asString();
			tree.readTree(description);
		}


		return true;
	}
}
