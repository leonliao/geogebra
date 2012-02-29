/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoDependentConic.java
 *
 * Created on 29. Oktober 2001
 */

package geogebra.common.kernel.algos;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.arithmetic.Equation;
import geogebra.common.kernel.arithmetic.ExpressionValue;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.arithmetic.Polynomial;
import geogebra.common.kernel.geos.GeoConic;

/**
 *
 * @author  Markus
 * @version 
 */
public class AlgoDependentConic extends AlgoElement {

    private Equation equation;
    private ExpressionValue [] ev = new ExpressionValue[6];  // input
    private GeoConic conic;     // output                 
        
    /** Creates new AlgoJoinPoints */
    public AlgoDependentConic(Construction cons,String label, Equation equ) {  
       	super(cons, false); // don't add to construction list yet
        equation = equ;                
        Polynomial lhs = equ.getNormalForm();
        
        ev[0] = lhs.getCoefficient("xx");       
        ev[1] = lhs.getCoefficient("xy");       
        ev[2] = lhs.getCoefficient("yy");        
        ev[3] = lhs.getCoefficient("x");        
        ev[4] = lhs.getCoefficient("y");                
        ev[5] = lhs.getConstantCoefficient();          
       
        // check coefficients
        for (int i=0; i<6; i++) {
        	// find constant parts of input and evaluate them right now
            if (ev[i].isConstant()) ev[i] = ev[i].evaluate(StringTemplate.defaultTemplate);
                 
            // check that coefficient is a number: this may throw an exception
            ExpressionValue eval = ev[i].evaluate(StringTemplate.defaultTemplate);
            ((NumberValue) eval).getDouble();  
        }  
        
        // if we get here, all is ok: let's add this algorithm to the construction list
        cons.addToConstructionList(this, false);
        
        conic = new GeoConic(cons); 
        setInputOutput(); // for AlgoElement
        
        // compute value of dependent number        
        compute();      
        conic.setLabel(label);                
    }   
    
	@Override
	public Algos getClassName() {
		return Algos.AlgoDependentConic;
	}
    
    // for AlgoElement
	@Override
	protected void setInputOutput() {
        input = equation.getGeoElementVariables();  
  
        super.setOutputLength(1);
        super.setOutput(0, conic);
        setDependencies(); // done by AlgoElement
    }    
    
    public GeoConic getConic() { return conic; }
    
    // calc the current value of the arithmetic tree
    @Override
	public final void compute() {   
    	try {
	        conic.setCoeffs( 
	                ev[0].evaluateNum().getDouble(),
	                ev[1].evaluateNum().getDouble(),
	                ev[2].evaluateNum().getDouble(),        
	                ev[3].evaluateNum().getDouble(),        
	                ev[4].evaluateNum().getDouble(),        
	                ev[5].evaluateNum().getDouble()
	                );
	    } catch (Throwable e) {
			conic.setUndefined();
		}
    }   

    @Override
	public final String toString(StringTemplate tpl) {
        return equation.toString(tpl);
    }
    
    @Override
	public final String toRealString(StringTemplate tpl) {
        return equation.toRealString(tpl);
    }           
}
