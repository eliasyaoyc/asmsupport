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
public class IncrementAndDecrement extends CreateMethod {

	public IncrementAndDecrement(ClassCreator creator) {
		super(creator);
	}

	@Override
	public void createMethod() {
		creator.createMethod("incrementAndDecrement", null, null, null, null,
				Opcodes.ACC_PUBLIC, new CommonMethodBody() {
					@Override
					public void generateBody(LocalVariable... argus) {
						LocalVariable count = createVariable("count", AClass.INT_ACLASS, false, Value.value(10)); // int count = // 10;
						beforeInc(count); // ++count
						beforeDec(count); // --count;
						invoke(out, "println", append(Value.value("count = "), count)); // System.out.println("count = " +  count);
						runReturn();
					}
				});
	}

}