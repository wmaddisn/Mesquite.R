/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
 */
package mesquite.R.MesquiteCallsR.diversitree.lib;


import mesquite.R.common.*;
import mesquite.lib.*;


public class BiSSEParameterTranslator {
	public String constrain = "c(";
	public boolean cexists = false;
	MesquiteParameter[] paramsCopy;
	boolean[] constrained;
	public int[] translate = new int[6];
	
	public void buildTranslation(MesquiteParameter[] paramsCopy){
		this.paramsCopy = paramsCopy;
		constrain = "";
		cexists = false;
		boolean first = true;
		constrained = new boolean[paramsCopy.length];
		for (int i = 0; i<6; i++)
			translate[i] = 0;
		int count = 0;
	
		for (int i = 0; i< paramsCopy.length; i++){
			//parameter i
			constrained[i] = false;
			int c = MesquiteParameter.getWhichConstrained(paramsCopy, i);
			if (c <0) {  //this is not constrained to be the same as another parameter
				MesquiteParameter p = paramsCopy[i];
				if (p.isCombinable()) {  //the value is fixed, and so it won't show up as a parameter
					if (!first)
						constrain += ", ";
					first = false;
					constrain += paramsCopy[i].getName() + " ~ " + MesquiteDouble.toString(p.getValue()) + " ";
					cexists = true;
					constrained[i] = true;
				}
				else {
					translate[i] = count;
					count++;
				}
			}
			else {  //this is constrained to be the same as another parameter
				translate[i] = -c;  //hint that needs to be processed
				if (!first)
					constrain += ", ";
				first = false;
				constrain += paramsCopy[i].getName() + " ~ " + paramsCopy[c].getName() + " ";
				constrained[i] = true;
				cexists = true;
			}
		}
		for (int i = 0; i< translate.length; i++){
			if (translate[i]<0 && MesquiteInteger.isCombinable(translate[i]))
				translate[i] = translate[-translate[i]];
		}
	}
	String[] names = new String[]{"lambda0", "lambda1", "mu0", "mu1", "q01", "q10"};
	public String getResults(double[] parameters){
		String paramResults = "";
		for (int i=0; i<6; i++)
			paramResults += getParameterResult("\n" + names[i], i, parameters);
		return paramResults;
	}
	public int getTranslation(int i){
		int where = translate[i];
		if (MesquiteInteger.isCombinable(where))
			return where;
		else
			return i;
	}
	String getParameterResult(String name, int i, double[] parameters){
		int where = translate[i];
		if (MesquiteInteger.isCombinable(where)){
			int c = MesquiteParameter.getWhichConstrained(paramsCopy, i);
			if (c>=0)
				return name + " [constr. = " + names[c] + "] = " + MesquiteDouble.toString(parameters[where]);
			return name + " = " + MesquiteDouble.toString(parameters[where]);
		}

		return name + " [FIXED] = " + MesquiteDouble.toString(paramsCopy[i].getValue());

	}

}

