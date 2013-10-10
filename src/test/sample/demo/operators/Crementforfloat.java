/**
 * 
 */
package demo.operators;


import org.objectweb.asm.Opcodes;

import jw.asmsupport.block.method.common.CommonMethodBody;
import jw.asmsupport.clazz.AClass;
import jw.asmsupport.creator.ClassCreator;
import jw.asmsupport.definition.value.Value;
import jw.asmsupport.definition.variable.LocalVariable;


import demo.CreateMethod;

/**
 * @author 温斯群(Joe Wen)
 * 
 */
public class Crementforfloat extends CreateMethod {

	public Crementforfloat(ClassCreator creator) {
		super(creator);
	}

	@Override
	public void createMethod() {
		creator.createMethod("arithmeticCalculations", null, null, null, null,  Opcodes.ACC_PUBLIC, 
	    		new CommonMethodBody(){
					@Override
					public void generateBody(LocalVariable... argus) {
			        	LocalVariable a = createVariable("a", AClass.INT_ACLASS, false, Value.value(1)); //int a = 1;
			        	LocalVariable b = createVariable("b", AClass.INT_ACLASS, false, Value.value(2)); //int b = 2;
			        	LocalVariable c = createVariable("c", AClass.INT_ACLASS, false, Value.value(0)); //int c = 0;
			        	
			        	assign(c, add(a, b)); // c = a + b
			        	invoke(out, "println", c); // System.out.println(c);
					    runReturn(); //return
					}
	            }
	    );
	}

}