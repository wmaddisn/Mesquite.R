/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
 */
package mesquite.R.MesquiteCallsR.diversitree.BiSSELikelihoodTUnresolved;


import org.rosuda.JRI.REXP;

import mesquite.meristic.lib.*;
import mesquite.R.MesquiteCallsR.diversitree.lib.*;
import mesquite.R.common.APETree;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;

public class BiSSELikelihoodTUnresolved extends BiSSELikelihoodDRBase {

	/*------------------------------------------------------------------------------------------*/
	public String getName() {
		return "BiSSE for Terminally Unresolved (diversitree)";
	}

	public String getVeryShortName(){
		return "BiSSE TU (diversitree)";
	}

	public String getAuthors() {
		return "Rich FitzJohn";
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyMeristicData();
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "(From diversitree package in R): Calculates likelihood with a tree of a species diversification model whose speciation and extinction rates depend on the state of a binary character (BiSSE model, Maddison, Midford & Otto, 2007).";
	}

	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "diversification birth death";
	}

	public boolean isPrerelease(){
		return true;
	}
	/*------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || !(charStates instanceof MeristicDistribution))
			return;
		clearResultAndLastResult(result);
		if (resultString != null)
			resultString.setValue("Calculation not completed.  See log for details.");
		if (suspended)
			return;
		if (tree == null || charStates == null)
			return;


		if (!rLinker.require("diversitree"))
			return;
		CommandRecord.tick("diversitree BiSSE unresolved: preparing tree and data");
		//preparing tree ============
		APETree apeTree = new APETree(tree);
		rLinker.sendTreeToR(apeTree, "mTree");
		int numTerminals = tree.numberOfTerminalsInClade(tree.getRoot());

		//Character States ========
		if ( characterValid(charStates, tree)){
			if (resultString!=null)
				resultString.setValue(getName() + " unassigned because the character is not meristic or has fewer than 3 items or has missing data");
			return;
		}
		MeristicDistribution cStates = (MeristicDistribution)charStates;
		int[] nC = new int[numTerminals];
		int[] n0 = new int[numTerminals];
		int[] n1 = new int[numTerminals];
		String[] rowNames = new String[numTerminals];
		int[] values = new int[numTerminals];
		//int[] stateIfSingle = 
		for (int i = 0; i< numTerminals; i++){
			n0[i] = cStates.getState(apeTree.getMesquiteTaxon(i+1),0);
			n1[i] = cStates.getState(apeTree.getMesquiteTaxon(i+1),1);
			nC[i] = n0[i] + n1[i] + cStates.getState(apeTree.getMesquiteTaxon(i+1),2);
			if (nC[i] == 1){
				if (n0[i] ==1)
					values[i] = 0;
				if (n1[i] ==1)
					values[i] = 1;
			}
			rowNames[i] = tree.getTaxa().getTaxonName(apeTree.getMesquiteTaxon(i+1));
		}

		//sending unresolved data 
		rLinker.assign("n0",n0);
		rLinker.assign("n1",n1);
		rLinker.assign("Nc",nC);
		rLinker.assign("tip.label",rowNames);
		rLinker.eval("unresolved <- data.frame(tip.label, Nc, n0, n1)");
		//rLinker.eval("print(unresolvedData)");

		//sending resolved data
		rLinker.eval("tip.state <- rep(NA, " + numTerminals + ")");
		rLinker.eval("names(tip.state) <- tip.label");

		/*TEMP: checking against Rich's example
		rLinker.eval("lik <- make.bisse(mTree, tip.state, unresolved)");
		rLinker.eval("L <- lik(c(.3, .3, 0, 0, .05, .05))");
		rLinker.eval("print(L)");
		 */

		rLinker.eval("tip.state2 <- tip.state");
		rLinker.eval("tip.state2[unresolved$Nc == 1 & unresolved$n0 == 1] <- 0");
		rLinker.eval("tip.state2[unresolved$Nc == 1 & unresolved$n1 == 1] <- 1");
		rLinker.eval("unresolved2 <- unresolved[unresolved$Nc > 1,]");
		//========================
		/* Rich FitzJohn's simple example
 phy <- read.tree(text="((a:1.0,b:1.0):2.0,(c:2.0,(d:1.0,e:1.0):1.0):1.0):1.0;")
## I just picked these parameter values as they give a reasonable
## likelihood.
pars <- c(.3, .3, 0, 0, .05, .05)

## Here is the full unresolved clade information
unresolved <- data.frame(tip.label=letters[1:5],
                         Nc=c(5, 1, 1, 9, 1),
                         n0=c(3, 1, 0, 3, 0),
                         n1=c(0, 0, 1, 5, 0))
tip.state <- rep(NA, 5)
names(tip.state) <- letters[1:5]
lik <- make.bisse(phy, tip.state, unresolved)
lik(pars)

## Strictly, only tips a and d need to be included here - this is how
## it should be done, really (otherwise the slower, less accurate,
## unresolved clade calculations are performed for the tips that
## represent a single species).
tip.state2 <- tip.state
tip.state2[unresolved$Nc == 1] <- unresolved$n1[unresolved$Nc==1]
unresolved2 <- unresolved[unresolved$Nc > 1,]
lik2 <- make.bisse(phy, tip.state2, unresolved2)
lik2(c(.3, .3, 0, 0, .05, .05))

## These give numbers that are very similar (differing about 8
## d.p. in)
lik(c(.3, .3, 0, 0, .05, .05))-lik2(c(.3, .3, 0, 0, .05, .05))

		 * */
		/*
		rLinker.eval("lik2 <- make.bisse(mTree, tip.state2, unresolved2)");
		rLinker.eval("L2 <- lik2(c(.3, .3, 0, 0, .05, .05))");
		rLinker.eval("print(L2)");
		 */

		CommandRecord.tick("diversitree BiSSE unresolved: preparing likelihood function");
		//getting likelihood function ============
		REXP lik = rLinker.eval("lik <- make.bisse(mTree, tip.state2, unresolved = unresolved2)");

		//Parameters ========
		BiSSEParameterTranslator translator = new BiSSEParameterTranslator();
		paramsCopy = MesquiteParameter.cloneArray(params, paramsCopy);
		translator.buildTranslation(paramsCopy);


		if (translator.cexists){
			lik = rLinker.eval("lik <- constrain(lik, " + translator.constrain + ")");
		}
		
		//========================

		//Doing likelihood calculations ===========
		REXP p = rLinker.eval("p <- starting.point.bisse(mTree)");
		CommandRecord.tick("diversitree BiSSE unresolved: searching for maximum likelihood");
		REXP fit = rLinker.eval("fit <- find.mle(lik, p)");

		REXP params = rLinker.eval("estimatedPar <- fit$par");
		rLinker.eval("print(estimatedPar)");
		if (params != null){
			double[] parameters = params.asDoubleArray();
			

			
			REXP lnLik = rLinker.eval("lnLikelihood <- fit$lnLik");

			rLinker.eval("print(\"Ln likelihood\")");
			rLinker.eval("print(lnLikelihood)");
			CommandRecord.tick("diversitree BiSSE unresolved: result obtained");
			double lnLikelihood = lnLik.asDouble();

			if (reportMode >0) {
				result.setValue(parameters[reportMode-1]);
			}
			else {
				result.setValue(lnLikelihood);
				//	lambda0 lambda1 mu0 mu1 q01 q10 
				MesquiteParameter[] params6 = new MesquiteParameter[6];
				for(int i = 0; i<6; i++) {
					params6[i] = new MesquiteParameter();
					params6[i].setValue(parameters[translator.getTranslation(i)]);
				}
				params6[0].setName("lambda0");
				params6[1].setName("lambda1");
				params6[2].setName("mu0");
				params6[3].setName("mu1");
				params6[4].setName("q01");
				params6[5].setName("q10");
				result.copyAuxiliaries(params6);
			}
			String paramResults = translator.getResults(parameters);

			if (resultString != null)
				resultString.setValue(reportModeName.getValue() + ": " + result.getDoubleValue() + "; " + paramResults);
		}
		else
			CommandRecord.tick("diversitree BiSSE unresolved: results not obtained");

		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	boolean characterValid(CharacterDistribution dist, Tree tree){
		if (!(dist instanceof MeristicDistribution))
			return false;

		MeristicDistribution cd = (MeristicDistribution)dist;
		if (cd.getNumItems() <3)
			return false;
		return inIsValid(cd, tree, tree.getRoot());
	}
	boolean inIsValid(MeristicDistribution dist, Tree tree, int node){

		if (tree.nodeIsTerminal(node)){
			for (int item = 0; item<3; item++)
				if (MesquiteInteger.isCombinable(dist.getState(tree.taxonNumberOfNode(node), item)))
					return false;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (!inIsValid(dist, tree, d))
					return false;
			}
		}
		return true;
	}

}

