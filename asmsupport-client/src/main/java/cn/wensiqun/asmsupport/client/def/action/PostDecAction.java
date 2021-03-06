package cn.wensiqun.asmsupport.client.def.action;

import cn.wensiqun.asmsupport.client.def.Param;
import cn.wensiqun.asmsupport.client.def.param.basic.DummyParam;
import cn.wensiqun.asmsupport.core.operator.Operator;
import cn.wensiqun.asmsupport.core.utils.common.BlockTracker;

public class PostDecAction extends AbstractUnaryAction {

    public PostDecAction(BlockTracker tracker) {
        super(tracker, Operator.POS_DEC);
    }

    @Override
    public Param doAction(Param... operands) {
        return new DummyParam(tracker, tracker.track().postdec(operands[0].getTarget()));
    }

}
