/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.MesquiteCallsR.ouch.OUCHHansenFitUV;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.cont.lib.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class OUCHHansenFitUV extends NumberForCharAndTree {

	/*
Univariate:
alpha: 1;  default guess 0
sigma 1;  default guess 1
Theta: 1 + number of regimes

bivariate
alpha: 2 if no correlation, 3 if correlation; default guesses 0
sigma: 2 if no correlation, 3 if correlation; default guesses 1
theta: 1 + number of regimes


	 * */
	public String getName() {
		return "OUCH Hansen Fit Univariate";
	}
	public String getExplanation() {
		return "Fits the Hansen model using OUCH, for a single character.  Uses colors on tree branches to define different regimes.";
	}
	RLinker rLinker;

	//when operating to return a single number it can report that number as the likelihood or other values
	String[] reportModes = new String[]{"Likelihood","Alpha","Sigma","Theta0","Theta1","Theta2","Theta3"};
	int reportMode = 0;
	static final int LIKELIHOOD = 0;
	static final int ALPHA = 1;
	static final int SIGMA = 2;
	static final int THETA0 = 3;
	static final int THETA1 = 4;
	static final int THETA2 = 5;
	static final int THETA3 = 6;
	static double guessAlpha = 0.0;
	static double guessSigma = 1.0;

	MesquiteBoolean verbose;


	public CompatibilityTest getCompatibilityTest(){
		return new ContinuousStateTest();
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry("OUCH Hansen Fit cannot run: Problem with R.");
		}
		if (!MesquiteThread.isScripting()) {
			if (!queryDisplayOptions())
				return sorry(getName() + " was cancelled");
		}
		verbose = new MesquiteBoolean(false);
		addMenuItem("Initial Guesses for Hansen Fit...", makeCommand("setGuesses", this));
		addMenuItem("Report for Hansen Fit...", makeCommand("setReportMode", this));
		addMenuItem("Recalculate Hansen Fit", makeCommand("recalculate", this));
		addCheckMenuItem( null, "Show R Console Output", makeCommand("toggleVerbose", this), verbose);
		return true;
	}
	public void endJob(){
		if (rLinker != null){
			rLinker.end();
		}
		super.endJob();
	}
	/*===== For NumberForItem interface ======*/
	public boolean returnsMultipleValues(){
		return true;
	}
	public String getParameters(){
		return "Initial Guess for alpha = " + guessAlpha + "; for sigma = " + guessSigma;
	}
	boolean queryGuessesOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Initial Guesses",buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("Initial guesses for alpha and sigma parameters");
		if (queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>Initial guesses for alpha and sigma parameters</h3>Reasonable defaults might be 0 for alpha and 1 for sigma.");
		DoubleField dFieldA = queryDialog.addDoubleField("Alpha", guessAlpha, 20);
		DoubleField dFieldS = queryDialog.addDoubleField("Sigma", guessSigma, 20);

		queryDialog.completeAndShowDialog(true);

		if (buttonPressed.getValue()==0) {
			guessAlpha = dFieldA.getValue();
			guessSigma = dFieldS.getValue();
		}
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	boolean queryDisplayOptions(){
		int choice = ListDialog.queryList(containerOfModule(), "What value to report for Hansen Fit?", 
				"What value to report for Hansen Fit?", 
				"Choose whether the module \"" + getName() + "\" should report the likelihood, or one of the parameter values. (NOTE: Theta1, Theta2, Theta3 will be available only if there are multiple regimes on the tree)", 
				reportModes, reportMode);
		if (MesquiteInteger.isCombinable(choice)){
			reportMode = choice;
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setGuesses " + guessAlpha + " " + guessSigma);
		temp.addLine("setReportMode " + reportMode);
		temp.addLine("toggleVerbose " + verbose.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(MesquiteWindow.class, "Sets the reportMode", "[reportMode]", commandName, "setReportMode")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int mode= MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(mode)){
				queryDisplayOptions();
				return null;
			}
			if (MesquiteInteger.isCombinable(mode)){
				reportMode = mode;
				parametersChanged(null);
				return null;
			}

		}
		else if (checker.compare(MesquiteWindow.class, "Sets the initial guesses", "[guessAlpha] [guessAlpha]", commandName, "setGuesses")) {
			MesquiteInteger io = new MesquiteInteger(0);
			double alpha = MesquiteDouble.fromString(arguments, io);
			double sigma = MesquiteDouble.fromString(arguments, io);
			if (!MesquiteDouble.isCombinable(alpha)  && !MesquiteDouble.isCombinable(sigma)){
				if (queryGuessesOptions())
					parametersChanged(null);

				return null;
			}
			if (MesquiteDouble.isCombinable(alpha))
				guessAlpha = alpha;
			if (MesquiteDouble.isCombinable(sigma))
				guessSigma = sigma;
			if (!MesquiteThread.isScripting())
				parametersChanged(null);

		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show the results from the R console", "[on = show; off]", commandName, "toggleVerbose")) {

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

	NameReference colorNameRef = NameReference.getNameReference("color");

	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree == null || observedStates == null)
			return;
		if (!(observedStates instanceof ContinuousDistribution)){
			discreetAlert("Oops, not a continuous distribution (" + getName() + ")");
			return;
		}
		rLinker.setEchoRConsole(verbose.getValue());
		if (verbose.getValue())
			logln("=============\nAbout to call R for " + getName());
		ContinuousDistribution cStates = (ContinuousDistribution)observedStates;
		if (resultString !=null)
			resultString.setValue("");
		int numNodes = tree.numberOfNodesInClade(tree.getRoot());
		double[] times = new double[numNodes];
		int[][] anc = rLinker.packOUCHTree(tree, times);

		String[] nodeNames = new String[numNodes];
		double[] rStates = new double[numNodes];
		String missingData = "";
		//packing both the character matrix and the node names for OUCH
		for (int itn = 0; itn<numNodes; itn++){
			if (tree.nodeIsTerminal(anc[1][itn])) {
				rStates[ itn] = cStates.getState(tree.taxonNumberOfNode(anc[1][itn]));
				if (!MesquiteDouble.isCombinable(rStates[itn])){
					rStates[ itn] = Double.NaN;
					missingData += "\n" + tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(anc[1][itn]));
				}
				nodeNames[itn] = tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(anc[1][itn]));
			}
			else{
				rStates[ itn] = Double.NaN;
				nodeNames[itn] = "";
			}
		}

		//packing the regimes
		int[] regimes = new int[numNodes];
		if (tree.getWhichAssociatedLong(colorNameRef)== null){
			for (int i=0; i<numNodes; i++)
				regimes[i] = 1;
		}
		else {
			for (int i=0; i<numNodes; i++){
				long collong = tree.getAssociatedLong(colorNameRef, anc[1][i]);
				int colint = MesquiteInteger.unassigned;
				if (MesquiteLong.isCombinable(collong))
					colint = (int)collong;
				regimes[i] = colint;
			}
			for (int i=0; i<numNodes; i++){
				if (!MesquiteInteger.isCombinable(regimes[i]))
					regimes[i] = ColorDistribution.NO_COLOR;  //a 
			}
		}
		if (verbose.getValue())
			logln("REGIMES " + IntegerArray.toString(regimes));


		/*	Debugg.println("anc " + anc.length + " \n " + Integer2DArray.toString(anc));
		Debugg.println("rMatrix\n" + DoubleArray.toString(rStates));
		Debugg.println("times\n" + DoubleArray.toString(times));
		Debugg.println("regimes\n" + IntegerArray.toString(regimes));
		Debugg.println("numNodes " + numNodes);
		 */
		if (!rLinker.require("ouch"))
			return;
		rLinker.assign("ancestor",anc[0]);
		rLinker.eval("ancestor[ancestor==-1] <- NA");
		rLinker.eval("node <- 1:length(ancestor)");
		rLinker.assign("time",times);
		rLinker.assign("data",rStates);
		//rLinker.eval("dim(data) <- c(" + (numNodes) + ", " + cMatrix.getNumChars() + ")");
		rLinker.eval("data <- as.data.frame(data)");
		if (verbose.getValue())
			rLinker.eval("print(data)");
		rLinker.eval("rownames(data) <- node");
		rLinker.eval("data[is.nan(data)] <- NA");
		if (verbose.getValue())
			rLinker.eval("print(cbind(node,ancestor))");
		rLinker.eval("tree <- ouchtree(node,ancestor,time/max(time))");
		rLinker.assign("reg",regimes);
		rLinker.eval("reg <- as.factor(reg)");
		rLinker.eval("names(reg) <- node");
		rLinker.eval("h1 <- hansen(data,tree,reg,alpha=" + guessAlpha + ",sigma=" + guessSigma + ")");
		if (verbose.getValue())
			rLinker.eval("print(h1)");
		rLinker.eval("ch1 <- coef(h1)");

		/**/
		REXP R_loglik = rLinker.eval("logLik(h1)");
		REXP R_dof = rLinker.eval("dof(h1)");
		REXP R_alpha = rLinker.eval("ch1$alpha");
		REXP R_sigma = rLinker.eval("ch1$sigma");
		REXP R_theta = rLinker.eval("unlist(ch1$theta)");
		// generic vectors are RVector to accomodate names
		try{
			if (verbose.getValue())
				logln("Results returned from R");
			double[] loglik = (double[]) R_loglik.getContent();
			double[] alpha = (double[]) R_alpha.getContent();
			double[] sigma = (double[]) R_sigma.getContent();
			double[] theta = (double[]) R_theta.getContent();
			if (reportMode == LIKELIHOOD){
				result.setValue(loglik[0]);
				result.setName("lnLikelihood");
			}
			else {
				if (reportMode == ALPHA)
					result.setValue(alpha[0]);
				else if (reportMode == SIGMA)
					result.setValue(sigma[0]);
				else if (reportMode == THETA0)
					result.setValue(theta[0]);
				else if (reportMode == THETA1 && theta.length>1)
					result.setValue(theta[1]);
				else if (reportMode == THETA2 && theta.length>2)
					result.setValue(theta[2]);
				else if (reportMode == THETA3 && theta.length>3)
					result.setValue(theta[3]);
				else
					result.setToUnassigned();
				result.setName(reportModes[reportMode]);
			}
			MesquiteNumber[] results = new MesquiteNumber[1 + alpha.length + sigma.length + theta.length];
			for (int i=0; i<results.length; i++)
				results[i] = new MesquiteNumber();

			String rs = result.getName() + " " + MesquiteDouble.toString(result.getDoubleValue()) + "  [";

			results[0].setValue(loglik[0]);
			results[0].setName("lnLikelihood");
			rs += " lnLikelihood " + MesquiteDouble.toString(loglik[0]);
			if (loglik[0]>0)
				rs += " WARNING: This is an impossible log likelihood. There has been a problem with the calculation. ";
			for (int i= 0; i<alpha.length; i++){
				results[i +1].setValue(alpha[i]);
				results[i +1].setName("alpha" + i);
				rs += " alpha" + i + " " + MesquiteDouble.toString(alpha[i]);
			}
			for (int i= 0; i<sigma.length; i++){
				results[i +1+alpha.length].setValue(sigma[i]);
				results[i +1+alpha.length].setName("sigma" + i);
				rs += " sigma" + i + " " + MesquiteDouble.toString(sigma[i]);
			}
			for (int i= 0; i<theta.length; i++){
				results[i +1+alpha.length+sigma.length].setValue(theta[i]);
				results[i +1+alpha.length+sigma.length].setName("theta" + i);
				rs += " theta" + i + " " + MesquiteDouble.toString(theta[i]);
			}
			rs += "]";
			if (!StringUtil.blank(missingData))
				rs += "  WARNING:  The following taxa have missing data, and the calculations are INVALID" + missingData;
			if (resultString != null)
				resultString.setValue(rs);
			result.copyAuxiliaries(results);
			saveLastResult(result);
			saveLastResultString(resultString);
			if (verbose.getValue())
				logln("Results processed from R");
		}
		catch (Exception e){
			if (!StringUtil.blank(missingData) && resultString != null)
				resultString.setValue( "  WARNING:  The following taxa have missing data, and the calculations FAILED " + missingData );;
			MesquiteMessage.warnProgrammer("Exception thrown in processing R results");
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
