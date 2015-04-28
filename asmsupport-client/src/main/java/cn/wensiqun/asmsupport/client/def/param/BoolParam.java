package cn.wensiqun.asmsupport.client.def.param;

import cn.wensiqun.asmsupport.client.block.KernelProgramBlockCursor;
import cn.wensiqun.asmsupport.client.def.Param;
import cn.wensiqun.asmsupport.client.def.ParamPostern;
import cn.wensiqun.asmsupport.client.def.action.AndAction;
import cn.wensiqun.asmsupport.client.def.action.EqualAction;
import cn.wensiqun.asmsupport.client.def.action.LogicAndAction;
import cn.wensiqun.asmsupport.client.def.action.LogicOrAction;
import cn.wensiqun.asmsupport.client.def.action.LogicXorAction;
import cn.wensiqun.asmsupport.client.def.action.NotEqualAction;
import cn.wensiqun.asmsupport.client.def.action.OperatorAction;
import cn.wensiqun.asmsupport.client.def.action.OrAction;
import cn.wensiqun.asmsupport.client.def.behavior.BoolBehavior;
import cn.wensiqun.asmsupport.client.def.behavior.ObjectBehavior;
import cn.wensiqun.asmsupport.core.definition.value.Value;

public class BoolParam extends PriorityParam implements BoolBehavior {

	public BoolParam(KernelProgramBlockCursor cursor, Param param) {
        super(cursor, param);
    }
	
    public BoolParam(KernelProgramBlockCursor cursor, OperatorAction action, Param... operands) {
        super(cursor, action, operands);
    }

    @Override
    public BoolParam eq(Param para) {
        priorityStack.pushAction(new EqualAction(cursor), para);
        return this;
    }

    @Override
    public BoolParam ne(Param para) {
        priorityStack.pushAction(new NotEqualAction(cursor), para);
        return this;
    }

    @Override
    public BoolParam and(Param param) {
        priorityStack.pushAction(new AndAction(cursor), param);
        return this;
    }

    @Override
    public BoolParam or(Param param) {
        priorityStack.pushAction(new OrAction(cursor), param);
        return this;
    }

    @Override
    public BoolParam logicAnd(Param param) {
        priorityStack.pushAction(new LogicAndAction(cursor), param);
        return this;
    }

    @Override
    public BoolParam logicOr(Param param) {
        priorityStack.pushAction(new LogicOrAction(cursor), param);
        return this;
    }

    @Override
    public BoolParam logicXor(Param param) {
        priorityStack.pushAction(new LogicXorAction(cursor), param);
        return this;
    }

    @Override
    public BoolParam and(boolean param) {
        return and(new DummyParam(cursor, Value.value(param)));
    }

    @Override
    public BoolParam or(boolean param) {
        return or(new DummyParam(cursor, Value.value(param)));
    }

    @Override
    public BoolParam logicAnd(boolean param) {
        return logicAnd(new DummyParam(cursor, Value.value(param)));
    }

    @Override
    public BoolParam logicOr(boolean param) {
        return logicOr(new DummyParam(cursor, Value.value(param)));
    }

    @Override
    public BoolParam logicXor(boolean param) {
        return logicXor(new DummyParam(cursor, Value.value(param)));
    }

    @Override
    public ObjectBehavior stradd(Param param) {
        return new ObjectParam(cursor, cursor.getPointer().stradd(target, ParamPostern.getTarget(param)));
    }

}
