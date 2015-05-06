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
package cn.wensiqun.asmsupport.client.def.param;

import cn.wensiqun.asmsupport.client.block.KernelProgramBlockCursor;
import cn.wensiqun.asmsupport.client.def.Param;
import cn.wensiqun.asmsupport.client.def.var.FieldVar;
import cn.wensiqun.asmsupport.core.definition.KernelParame;
import cn.wensiqun.asmsupport.standard.def.clazz.AClass;

/**
 * A simple parameter that only a proxy of {@link KernelParame}
 * 
 * @author WSQ
 *
 */
public class DummyParam extends Param {

    protected KernelParame target;
    
    protected KernelProgramBlockCursor cursor;
    
    public DummyParam(KernelProgramBlockCursor cursor, KernelParame target) {
        this.cursor = cursor;
        this.target = target;
    }

    @Override
    public final FieldVar field(String name) {
        return new FieldVar(cursor, target.field(name));
    }
    
    @Override
    public final AClass getResultType() {
        return target.getResultType();
    }

    @Override
    protected KernelParame getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return getTarget().toString();
    }
    
    
}