/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.This source code and its compiled class files are free and modifiable under the terms of GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)*/package mesquite.R.MesquiteCallsR.ape.APEaceMLCont;import org.rosuda.JRI.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.CharStatesForNodes;import mesquite.R.MesquiteCallsR.lib.RLinker;import mesquite.R.common.APETree;import mesquite.cont.lib.*;/* ======================================================================== */public class APEaceMLCont extends CharStatesForNodes  {	public String getName() {		return "Brownian Motion Likelihood (ape: ace: ML)";	}	public String getExplanation() {		return "Reconstructs ancestral states using likelihood under a simle Brownian Motion model, using ape's ace function";	}	RLinker rLinker;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		rLinker = new RLinker();		if (!rLinker.linkToR()) {			return sorry(getName() + " cannot run: Problem with R.");		}		if (!MesquiteThread.isScripting()) {			/*if (!queryOptions())				return sorry(getName() + " was cancelled");*/		}		return true;	}	public boolean requestPrimaryChoice(){		return true;	}	public void endJob(){		if (rLinker != null){			rLinker.end();		}		super.endJob();	}	public boolean allowsStateWeightChoice(){		return false;	}	public  boolean calculateStates(Tree aTree, CharacterDistribution charStates, CharacterHistory resultStates, MesquiteString resultString){		if (resultStates == null)			return false;		////rLinker.setVerbose(true);		resultStates.deassignStates();		if (!(aTree instanceof MesquiteTree))			return false;		if (!(charStates instanceof ContinuousDistribution)){			if (resultString != null)				resultString.setValue(getName() + " can be used only with continuous data");			return false;		}		ContinuousDistribution cStates = (ContinuousDistribution)charStates;		MesquiteTree tree = (MesquiteTree)aTree;		if (!rLinker.require("ape"))			return false;		APETree apeTree = new APETree(tree);		rLinker.sendTreeToR(apeTree, "mTree");		int numTerminals = tree.numberOfTerminalsInClade(tree.getRoot());		double[] values = new double[numTerminals];		String[] rowNames = new String[numTerminals];		for (int i = 0; i< numTerminals; i++){			values[i] = cStates.getState(apeTree.getMesquiteTaxon(i+1));			rowNames[i] = tree.getTaxa().getTaxonName(apeTree.getMesquiteTaxon(i+1));		}		rLinker.assign("data",values);		rLinker.eval("dim(data) <- c(" + numTerminals + ",1)");		rLinker.assign("rowNames",rowNames);		rLinker.eval("rownames(data) <- rowNames");		/* */		//rLinker.eval("print(data)");		//rLinker.eval("print(mTree)");		String aceCommand = "ace1 <- ace(data, mTree, type=\"continuous\", method=\"ML\")";		REXP result = rLinker.eval(aceCommand);		REXP R_ace = rLinker.eval("ace1$ace");		REXP R_CI95 = rLinker.eval("ace1$CI95");		REXP R_sigma2 = rLinker.eval("ace1$sigma2");		REXP R_logLike = rLinker.eval("ace1$loglik");		try {			ContinuousHistory cResults = (ContinuousHistory)resultStates;			cResults.establishItem(NameReference.getNameReference("ML estimates"));			double[] est = (double[])R_ace.getContent();			cResults.establishItem(NameReference.getNameReference("Lower 95% CI"));			cResults.establishItem(NameReference.getNameReference("Upper 95% CI"));			double[] ci = (double[]) R_CI95.getContent();			double[] rate = (double[]) R_sigma2.getContent();			double[] logLikelihood = (double[])R_logLike.getContent();			int numTT = tree.numberOfTerminalsInClade(tree.getRoot());			for (int it=0; it< tree.getTaxa().getNumTaxa(); it++){				int node = tree.nodeOfTaxonNumber(it);				if (tree.nodeExists(node)){					double state =  cStates.getState(it);					cResults.setState(node, 0, state);					cResults.setState(node, 1, state);					cResults.setState(node, 2, state);				}			}			for (int i=0; i< est.length; i++){				int mesquiteNode = apeTree.getMesquiteNode(i+ numTT+1);				cResults.setState(mesquiteNode, 0, est[i]);				cResults.setState(mesquiteNode, 1, ci[i]);				cResults.setState(mesquiteNode, 2, ci[i+est.length]);			}			if (resultString != null)				resultString.setValue("From ape's ace function, ML estimate of Brownian rate: " + MesquiteDouble.toString(rate[0]) + " (SE "+ MesquiteDouble.toString(rate[1]) + ", Log.likelihood " + MesquiteDouble.toString(logLikelihood[0]) + ")");		}		catch(Exception r){			MesquiteMessage.warnProgrammer("Problem retrieving results from R analysis in " + getName());			return false;		}		return true;	}}