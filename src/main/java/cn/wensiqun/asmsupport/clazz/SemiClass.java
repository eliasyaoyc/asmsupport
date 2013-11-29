package cn.wensiqun.asmsupport.clazz;

import java.lang.reflect.Field;

import org.objectweb.asm.Opcodes;

import cn.wensiqun.asmsupport.definition.variable.GlobalVariable;
import cn.wensiqun.asmsupport.exception.ASMSupportException;
import cn.wensiqun.asmsupport.utils.reflet.ModifierUtils;


/**
 * 
 * @author 温斯群(Joe Wen)
 *
 */
public class SemiClass extends NewMemberClass {

	SemiClass(int version, int access, String name, Class<?> superCls,
            Class<?>[] interfaces) {
        this.version = version;
        this.name = name;
        this.mod = access;
        this.superClass = superCls;
        this.interfaces = interfaces;

        if(!ModifierUtils.isInterface(mod)){
            this.mod += Opcodes.ACC_SUPER;
        }
        
    }

    @Override
    public String getDescription() {
        return new StringBuilder("L").append(getName().replace(".", "/"))
                .append(";").toString();
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public int getDimension() {
        return -1;
    }

    @Override
    public int getCastOrder() {
        return 9;
    }

    @Override
    public GlobalVariable getGlobalVariable(String name) {
    	GlobalVariable gv = super.getGlobalVariable(name);
    	if(gv != null){
        	return gv;
        }
        
        Class<?> fieldOwner = superClass;
        Field f = null;
        for(;!fieldOwner.equals(Object.class)  ;fieldOwner = fieldOwner.getSuperclass()){
            try {
                f = fieldOwner.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
        }
        
        if(f == null){
            throw new ASMSupportException("no such field exception : " + name);
        }
        
        return new GlobalVariable(AClassFactory.getProductClass(fieldOwner),
                new ProductClass(f.getType()), 0, name);
    }
}