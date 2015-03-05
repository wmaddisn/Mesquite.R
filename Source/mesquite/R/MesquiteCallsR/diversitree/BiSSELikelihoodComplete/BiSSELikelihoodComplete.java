/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.diversitree.BiSSELikelihoodComplete;


import org.rosuda.JRI.REXP;

import mesquite.R.MesquiteCallsR.diversitree.lib.*;
import mesquite.R.common.APETree;
import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteParameter;
import mesquite.lib.MesquiteString;
import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterDistribution;

public class BiSSELikelihoodComplete extends BiSSELikelihoodDRBase {




	/*------------------------------------------------------------------------------------------*/
	public String getName() {
		return "BiSSE Likelihood (diversitree)";
	}
	
	public String getVeryShortName(){
		return "BiSSE (diversitree)";
	}

	public String getAuthors() {
		return "Rich FitzJohn";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "(From diversitree package in R): Calculates likelihood with a tree of a species diversification model whose speciation and extinction rates depend on the state of a binary character (BiSSE model, Maddison, Midford & Otto, 2007).";
	}
	/*------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result == null)
			return;
		clearResultAndLastResult(result);
		if (resultString != null)
			resultString.setValue("Calculation not completed.  See log for details.");
		if (suspended)
			return;
		if (tree == null || charStates == null)
			return;


		//rLinker.setVerbose(true);
		if (!rLinker.require("diversitree"))
			return;
		
		
		//preparing tree ============
		CommandRecord.tick("diversitree BiSSE: preparing tree and data");
		APETree apeTree = new APETree(tree);
		rLinker.sendTreeToR(apeTree, "mTree");
		int numTerminals = tree.numberOfTerminalsInClade(tree.getRoot());

		//Character States ========
		if ( !CategoricalDistribution.isBinaryNoMissing(charStates, tree)){
			if (resultString!=null)
				resultString.setValue(getName() + " unassigned because the character is not binary or has missing data");
			return;
		}
		CategoricalDistribution cStates = (CategoricalDistribution)charStates;
		int[] values = new int[numTerminals];
		String[] rowNames = new String[numTerminals];
		for (int i = 0; i< numTerminals; i++){
			values[i] = CategoricalState.minimum(cStates.getState(apeTree.getMesquiteTaxon(i+1)));
			rowNames[i] = tree.getTaxa().getTaxonName(apeTree.getMesquiteTaxon(i+1));
		}
		rLinker.assign("data",values);
		rLinker.assign("rowNames",rowNames);
		rLinker.eval("names(data) <- rowNames");
		//========================
		
		
		
		//getting likelihood function ============
		REXP lik = rLinker.eval("lik <- make.bisse(mTree, data)");

		//Parameters ========
		BiSSEParameterTranslator translator = new BiSSEParameterTranslator();
		paramsCopy = MesquiteParameter.cloneArray(params, paramsCopy);
		translator.buildTranslation(paramsCopy);
		

		if (translator.cexists){
			lik = rLinker.eval("lik <- constrain(lik, " + translator.constrain + ")");
		}
		//========================

		//Doing likelihood calculations ===========
		REXP p = rLinker.eval("p <- starting.point(mTree)");
		CommandRecord.tick("diversitree BiSSE: searching for maximum likelihood");
		REXP fit = rLinker.eval("fit <- find.mle(lik, p)");

		REXP params = rLinker.eval("estimatedPar <- fit$par");
		rLinker.eval("print(estimatedPar)");
		if (params != null){
			double[] parameters = params.asDoubleArray();
			REXP lnLik = rLinker.eval("lnLikelihood <- fit$lnLik");

			rLinker.eval("print(\"Ln likelihood\")");
			rLinker.eval("print(lnLikelihood)");
			CommandRecord.tick("diversitree BiSSE: result obtained");
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
			CommandRecord.tick("diversitree BiSSE: results not obtained");
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "diversification birth death";
	}

	public boolean isPrerelease(){
		return true;
	}


}

