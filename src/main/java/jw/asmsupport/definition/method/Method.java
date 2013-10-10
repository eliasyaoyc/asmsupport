package jw.asmsupport.definition.method;


import org.apache.commons.collections.CollectionUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import jw.asmsupport.Executeable;
import jw.asmsupport.asm.CommonInstructionHelper;
import jw.asmsupport.asm.InstructionHelper;
import jw.asmsupport.asm.StackLocalMethodVisitor;
import jw.asmsupport.block.ProgramBlock;
import jw.asmsupport.block.control.Try;
import jw.asmsupport.block.method.SuperMethodBody;
import jw.asmsupport.clazz.AClass;
import jw.asmsupport.clazz.NewMemberClass;
import jw.asmsupport.creator.IClassContext;
import jw.asmsupport.definition.variable.LocalVariable;
import jw.asmsupport.entity.MethodEntity;
import jw.asmsupport.operators.util.ThrowExceptionContainer;
import jw.asmsupport.utils.ASConstant;
import jw.asmsupport.utils.LocalVariables;
import com.googlecode.jwcommon.ModifierUtils;
import jw.asmsupport.utils.Scope;
import jw.asmsupport.utils.Stack;


/**
 * 方法的抽象。
 * 
 * @author 温斯群(Joe Wen)
 * 
 */
public class Method {

	/** 方法实体 */
    private MethodEntity me;

    /** 该方法对应的栈 */
    private Stack stack;
    
    //0 : indicate add, 1 : indicate modify
    /** 表示当前的Method是添加到Class中还是修改Method */
    private int mode = ASConstant.METHOD_CREATE_MODE_ADD;

    /** 该方法对应的本地变量 */
    private LocalVariables locals;

    /** 调用ASM框架的帮助类 */
    private InstructionHelper insnHelper;

    /** 当前Method的methodBody类 */
    private SuperMethodBody methodBody;

    /** 当前Method所包含的所有字节码操作 */
    private int totalIns = 0;
    
    /** ClassCreator 或者 ClassModifier的引用 */
    private IClassContext context;
    
    /** 当前方法的描述 */
    private String methodDesc;
    
    /** 当前方法需要抛出的异常 */
    private ThrowExceptionContainer throwExceptions;
    
    /** 当前method所属的类 */
    private NewMemberClass methodOwner;
    
    /** 当前方法的参数 */
    private LocalVariable[] arguments;
    
    /** 
     * 当在Method中发现需要创建try catch finally程序块的时候将 try语句块的引用保存在此变量中
     * 然后延迟try程序块内的一些操作的创建。 当和当前Try相关的所有catch和finally程序块都创建
     * 完成 再一一调用期prepare方法 具体可见ProgramBlock的tiggerTryCatchPrepare方法
     * 
     * 这样做的主要目的是为了能自动将finally语句块的内容插入到try或catch中所有return指令之前
     **/
    private Try nearlyTryBlock;

    /**
     * 构造方法
     * @param me 
     * @param context
     * @param methodBody
     * @param mode
     */
    public Method(MethodEntity me, IClassContext context, SuperMethodBody methodBody, int mode) {
        super();
        this.me = me;
        this.context = context;
        this.throwExceptions = new ThrowExceptionContainer();
        this.stack = new Stack();
        this.locals = new LocalVariables();
        this.mode = mode;

        CollectionUtils.addAll(throwExceptions, me.getExceptions());
        
        Type[] argTypes = new Type[me.getArgClasses().length];
        
        for(int i=0; i<argTypes.length; i++){
            argTypes[i] = me.getArgClasses()[i].getType();
        }
        
        methodDesc = Type.getMethodDescriptor(this.me.getReturnType(),
                argTypes);
        
        this.insnHelper = new CommonInstructionHelper(this);
        
        if(!ModifierUtils.isAbstract(me.getModifier())){
            // 设置method属性
            this.methodBody = methodBody;
            this.methodBody.setScope(new Scope(this.locals, null));
            this.methodBody.setOwnerBlock(null);
            this.methodBody.setInsnHelper(insnHelper);
        	methodOwner = context.getCurrentClass();
        }
    }
    
    /**
     * 获取所有需要抛出的异常
     * @param block
     */
    private void getThrowExceptionsInProgramBlock(ProgramBlock block){
    	ThrowExceptionContainer blockExceptions = block.getThrowExceptions();
    	if(blockExceptions != null){
    		for(AClass exp : blockExceptions){
    			throwExceptions.add(exp);
    		}
    	}
    	
    	for(Executeable exe : block.getExecuteQueue()){
    		if(exe instanceof ProgramBlock){
    			getThrowExceptionsInProgramBlock((ProgramBlock)exe);
    		}
    	}
    }
    
    /**
     * 创建ASM的MethodVisitor
     */
    private void createMethodVisitor(){
    	
    	if(!ModifierUtils.isAbstract(me.getModifier())){
            for(Executeable exe : getMethodBody().getExecuteQueue()){
    		    if(exe instanceof ProgramBlock){
    			    getThrowExceptionsInProgramBlock((ProgramBlock)exe);
    		    }
    	    }
    	}
    	
        String[] exceptions = new String[this.throwExceptions.size()];
        int i = 0;
        for(AClass te : this.throwExceptions){
            exceptions[i++] = te.getType().getInternalName();
        }
        
        MethodVisitor mv = context.getClassVisitor().visitMethod(
                me.getModifier(), me.getName(), methodDesc, null, exceptions);
        
        insnHelper.setMv(new StackLocalMethodVisitor(mv, stack));
        
    }

    /**
     * 当前Method是否是static的
     * @return
     */
    public boolean isStatic() {
        return (me.getModifier() & Opcodes.ACC_STATIC) != 0;
    }
    
    /**
     * 启动创建或修改程序
     */
    public void startup() {
        createMethodVisitor();
        if(!ModifierUtils.isAbstract(me.getModifier())){
            this.methodBody.execute();
            this.methodBody.endMethodBody();
        }
       	insnHelper.endMethod();
    }

    public Stack getStack() {
        return stack;
    }

    public LocalVariables getLocals() {
        return locals;
    }

    /**
     * 下一条指令的序号
     * @return
     */
    public int nextInsNumber() {
        totalIns++;
        return totalIns;
    }

    public SuperMethodBody getMethodBody() {
        return methodBody;
    }

    public InstructionHelper getInsnHelper() {
        return insnHelper;
    }

    public MethodEntity getMethodEntity() {
        return me;
    }
    
    public void removeThrowException(AClass exception){
    	throwExceptions.remove(exception);
    }
    
    public String getDesc(){
        return methodDesc;
    }

    @Override
    public String toString() {
        return me.getMethodString();
    }

	public NewMemberClass getMethodOwner() {
		return methodOwner;
	}

	public LocalVariable[] getArguments() {
		return arguments;
	}

	public void setArguments(LocalVariable[] arguments) {
		this.arguments = arguments;
	}

	public int getMode() {
		return mode;
	}

	public Try getNearlyTryBlock() {
		return nearlyTryBlock;
	}

	public void setNearlyTryBlock(Try nearlyTryBlock) {
		this.nearlyTryBlock = nearlyTryBlock;
	}
	
	
}