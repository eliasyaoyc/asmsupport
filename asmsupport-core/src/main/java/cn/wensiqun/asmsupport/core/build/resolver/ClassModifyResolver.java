/**    
 *  Asmsupport is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.wensiqun.asmsupport.core.build.resolver;

import cn.wensiqun.asmsupport.core.asm.adapter.ClassModifierClassAdapter;
import cn.wensiqun.asmsupport.core.asm.adapter.VisitXInsnAdapter;
import cn.wensiqun.asmsupport.core.block.method.clinit.KernelStaticBlockBody;
import cn.wensiqun.asmsupport.core.block.method.common.KernelMethodBody;
import cn.wensiqun.asmsupport.core.block.method.common.KernelModifiedMethodBody;
import cn.wensiqun.asmsupport.core.block.method.init.KernelConstructorBody;
import cn.wensiqun.asmsupport.core.build.FieldBuilder;
import cn.wensiqun.asmsupport.core.build.MethodBuilder;
import cn.wensiqun.asmsupport.core.build.impl.DefaultMethodBuilder;
import cn.wensiqun.asmsupport.core.context.ClassExecuteContext;
import cn.wensiqun.asmsupport.core.loader.CachedThreadLocalClassLoader;
import cn.wensiqun.asmsupport.core.utils.log.Log;
import cn.wensiqun.asmsupport.core.utils.log.LogFactory;
import cn.wensiqun.asmsupport.org.objectweb.asm.ClassReader;
import cn.wensiqun.asmsupport.org.objectweb.asm.ClassWriter;
import cn.wensiqun.asmsupport.org.objectweb.asm.Opcodes;
import cn.wensiqun.asmsupport.org.objectweb.asm.Type;
import cn.wensiqun.asmsupport.standard.def.clazz.IClass;
import cn.wensiqun.asmsupport.standard.def.clazz.ProductClass;
import cn.wensiqun.asmsupport.standard.def.method.AMethodMeta;
import cn.wensiqun.asmsupport.standard.error.ASMSupportException;
import cn.wensiqun.asmsupport.standard.utils.ASMSupportClassLoader;
import cn.wensiqun.asmsupport.standard.utils.IClassUtils;
import cn.wensiqun.asmsupport.utils.ASConstants;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ClassModifyResolver extends AbstractBytecodeResolver {
	
    private static final Log LOG = LogFactory.getLog(ClassModifyResolver.class);
	
    protected List<MethodBuilder> methodModifiers = new ArrayList<>();
    
    private List<KernelModifiedMethodBody> modifyConstructorBodies;

	private ProductClass productClass;

	public ClassModifyResolver(Class<?> clazz) {
		this(clazz, CachedThreadLocalClassLoader.getInstance());
	}
	
	public ClassModifyResolver(Class<?> clazz, ASMSupportClassLoader classLoader) {
		super(classLoader);
		if(!clazz.isArray()){
			this.productClass = (ProductClass) classLoader.getType(clazz);
		}else{
			throw new ASMSupportException("cannot modify array type : " + clazz);
		}
	}
	
	public final void modifyMethod(String name, Class<?>[] argClasses, KernelModifiedMethodBody mb){
		Class<?> clazz = productClass.getReallyClass();
		if(argClasses == null){
			argClasses = new Class<?>[0];
		}
		IClass[] argCls = new IClass[argClasses.length];
		String[] defaultArgNames = new String[argClasses.length];
		for(int i=0; i<argCls.length; i++){
			argCls[i] = classLoader.getType(argClasses[i]);
			defaultArgNames[i] = "arg" + i;
		}
		try {
			
			DefaultMethodBuilder methodCreator;
			if(name.equals(ASConstants.CLINIT)){
				methodCreator = DefaultMethodBuilder.buildForModify(name, argCls, defaultArgNames, classLoader.getType(void.class), null, Opcodes.ACC_STATIC, mb);
			}else if(name.equals(ASConstants.INIT)){
				if(modifyConstructorBodies == null){
					modifyConstructorBodies = new ArrayList<>();
				}
				modifyConstructorBodies.add(mb);
				
				Constructor<?> constructor = clazz.getDeclaredConstructor(argClasses);
				methodCreator = DefaultMethodBuilder.buildForModify(ASConstants.INIT,
						argCls, 
						defaultArgNames, 
						classLoader.getType(void.class),
						IClassUtils.convertToAClass(classLoader, constructor.getExceptionTypes()),
						constructor.getModifiers(), mb);
			}else{
				Method method = clazz.getDeclaredMethod(name, argClasses);
				methodCreator = DefaultMethodBuilder.buildForModify(name,
						argCls, 
						defaultArgNames, 
						classLoader.getType(method.getReturnType()),
						IClassUtils.convertToAClass(classLoader, method.getExceptionTypes()),
						method.getModifiers(), mb);
			}
			
			methodModifiers.add(methodCreator);
		} catch (NoSuchMethodException e) {
			throw new ASMSupportException("No such method " + AMethodMeta.getMethodString(name, argCls) + " in " + productClass);
		}
	}

	/**
	 *
	 * Create a field with null value.
	 *
	 * @param name            the field name
	 * @param modifiers       the field modifiers
	 * @param type      the field type
	 * @return
	 */
	public FieldBuilder createField(String name, int modifiers, IClass type) {
		return createField(name, modifiers, type, null);
	}

	/**
	 *
	 * Create a field with special value.
	 *
	 * @param name
	 * @param modifiers
	 * @param type
	 * @param value The initial value, this value is only support static field,
	 *              otherwise will ignored.This parameter, which may be null
	 *              if the field does not have an initial value,
	 *              must be an Integer, a Float, a Long, a Double or a
	 *              String (for int, float, long or String fields respectively).
	 *              This parameter is only used for static fields. Its value is
	 *              ignored for non static fields, which must be initialized
	 *              through bytecode instructions in constructors or methods.
	 * @return
	 */
	public FieldBuilder createField(String name, int modifiers, IClass type, Object value) {
		return createFieldInternal(name, modifiers, type, value);
	}

	/**
	 * Create a constructor.
	 * 
	 * @param access
	 * @param argTypes
	 * @param argNames
	 * @param exceptions
	 * @param body
	 * @return
	 */
    public MethodBuilder createConstructor(int access, IClass[] argTypes, String[] argNames, IClass[] exceptions, KernelConstructorBody body) {
        return createConstructorInternal(access, argTypes, argNames, exceptions, body);
    }
	
    /**
     * 
     * @param name
     * @param argClasses
     * @param argNames
     * @param returnClass
     * @param exceptions
     * @param access
     * @param mb
     * @return
     */
    public MethodBuilder createMethod(String name, IClass[] argClasses,
            String[] argNames, IClass returnClass, IClass[] exceptions,
            int access, KernelMethodBody mb) {
		return createMethodInternal(name, argClasses, argNames, returnClass, exceptions, access, mb);
    }

	/**
	 * Create static block of class.
	 *
	 * @param block
     */
    public MethodBuilder createStaticBlock(KernelStaticBlockBody block){
		if(productClass.existStaticInitBlock()) {
			throw new IllegalArgumentException("The static block(<clinit>) has already exist, call modifyMethod to change it.");
		}
		return createStaticBlockInternal(block);
    }

	@Override
	public void initialized(ClassExecuteContext context) {
		InputStream is = classLoader.getResourceAsStream(productClass.getName());
		try {
			//modify class
			ClassReader cr = new ClassReader(is);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			context.setClassVisitor(cw);
            context.setOwner(productClass);

            ClassModifierClassAdapter adapter = new ClassModifierClassAdapter(cw, this);
			cr.accept(adapter, ClassReader.SKIP_FRAMES);
			
			if(LOG.isPrintEnabled()){
	            LOG.print("Start modify class : " + productClass.getReallyClass());
	        }
	        
	        // create field
	        for (FieldBuilder ifc : fields) {
	            ifc.initialized(context);
	        }

	        // create method
	        for (MethodBuilder imc : methods) {
	            imc.initialized(context);
	        }
	        
	        // modify method
	        for (MethodBuilder imc : methodModifiers) {
	            imc.initialized(context);
	        }

	        Map<String, List<VisitXInsnAdapter>> superConstructorMap = adapter.getSuperConstructorMap();
	        
	        if(modifyConstructorBodies != null){
	            for(KernelModifiedMethodBody mbfm : modifyConstructorBodies){
	                 Type[] argumentTypes = mbfm.getMethod().getMeta().getParameterAsmTypes();
	                 String desc = Type.getMethodDescriptor(Type.VOID_TYPE, argumentTypes);
	                 mbfm.setSuperConstructorOperators(superConstructorMap.get(desc));
	            }
	        }
		} catch (Exception e) {
			throw new ASMSupportException(e.getMessage(), e);
		}
	}

	@Override
	public void prepare() {
		for (FieldBuilder ifc : fields) {
			ifc.prepare();
		}

		for (MethodBuilder imc : methods) {
			imc.prepare();
		}

		for (MethodBuilder imc : methodModifiers) {
			imc.prepare();
		}

	}

	@Override
	public void execute(ClassExecuteContext classExecuteContext) {

        for (FieldBuilder ifc : fields) {
            ifc.execute(classExecuteContext);
        }

        for (MethodBuilder imc : methods) {
            imc.execute(classExecuteContext);
        }

        for (MethodBuilder imc : methodModifiers) {
            imc.execute(classExecuteContext);
        }

        if(LOG.isPrintEnabled()){
            LOG.print("End modify class : " + productClass.getReallyClass());
        }
	}

	@Override
	public ProductClass getCurrentClass() {
		return productClass;
	}

	public List<MethodBuilder> getMethodModifiers() {
		return methodModifiers;
	}

}
