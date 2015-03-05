/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.various.AddMatrix;
/*~~  */


import org.rosuda.JRI.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.charMatrices.lib.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class AddMatrix extends SourceModifiedMatrix {
	RLinker rLinker;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!super.startJob(arguments, condition, hiredByName))
			return false;
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run R");
		}
		return true; 
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ContinuousStateTest();
	}


	/*.................................................................................................................*/
	private MCharactersDistribution getM(Taxa taxa){

		MContinuousDistribution cMatrix = (MContinuousDistribution)getBasisMatrix(taxa); //TODO: shouldn't have to do this for every matrix, just first after change of currentMatrix
		if (cMatrix==null)
			return null;
		MAdjustableDistribution modified = cMatrix.makeBlankAdjustable();
		int numNodes = cMatrix.getNumNodes();
		double[] rMatrix = new double[cMatrix.getNumChars()*(numNodes)];
		for (int ic = 0; ic<cMatrix.getNumChars(); ic++)
			for (int itn = 0; itn<numNodes; itn++){
				rMatrix[ic*(numNodes) + itn] = cMatrix.getState(ic, itn);
			}
		rLinker.assign("data",rMatrix);
		rLinker.eval("dim(data) <- c(" + (numNodes) + ", " + cMatrix.getNumChars() + ")");
		rLinker.eval("data <- as.data.frame(data)");
		rLinker.eval("print(data)");
		REXP sum = rLinker.eval("data + data");
		Debugg.println("sum " + sum);
		Object obj = sum.getContent();
		if (obj instanceof double[]){
			Debugg.println("&&&&&&double[]");
			double[] s = (double[])obj;
			for (int ic = 0; ic<cMatrix.getNumChars(); ic++)
				for (int itn = 0; itn<numNodes; itn++){
					((MContinuousAdjustable)modified).setState(ic, itn, s[ic*(numNodes) + itn]);
				}
		}
		else if (obj instanceof RVector){
			Debugg.println("&&&&&&RVector");
			RVector rv =(RVector)obj;
			double[][] s = new double[cMatrix.getNumChars()][];
			for (int ic = 0; ic<s.length; ic++){
				s[ic] = (double[])rv.at(ic).getContent();
				for (int it=0; it<numNodes; it++)
					((MContinuousAdjustable)modified).setState(ic, it, s[ic][it]);
			}
		}
		return modified;
	}
	/*.................................................................................................................*/
	public String getMatrixName(Taxa taxa, int ic) {
		return "added";
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
		return getM(taxa);
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getMatrix(Taxa taxa, int im){
		return getM(taxa);
	}
	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Taxa taxa){
		return 1; //TODO: convert all these to MesquiteInteger.infinite
	}
	/*.................................................................................................................*/
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return 0;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add Matrix";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies character matrices that are added  THIS IS FOR R-Mesquite practice." ;
	}

}
