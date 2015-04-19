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
package cn.wensiqun.asmsupport.core.definition.variable;

import cn.wensiqun.asmsupport.core.block.ProgramBlockInternal;
import cn.wensiqun.asmsupport.core.log.Log;
import cn.wensiqun.asmsupport.core.log.LogFactory;
import cn.wensiqun.asmsupport.core.utils.AClassUtils;
import cn.wensiqun.asmsupport.standard.clazz.AClass;

public class StaticGlobalVariable extends GlobalVariable {

    private static final Log LOG = LogFactory.getLog(StaticGlobalVariable.class);

    /** 如果当前全局变量是静态变量，那么staticOwner表示静态变量的所属Class */
    private AClass owner;

    /**
     * 
     * @param owner
     * @param gve
     */
    public StaticGlobalVariable(AClass owner, AClass declaringClass, 
            AClass formerType, int modifiers, String name){
    	super(declaringClass, formerType, modifiers, name);
        this.owner = owner;
    }

    public AClass getOwner() {
        return owner;
    }

    @Override
    public void loadToStack(ProgramBlockInternal block) {
        if (!AClassUtils.visible(block.getMethodOwner(), getDeclaringClass(), getActuallyOwnerType(),
                getModifiers())) {
            throw new IllegalArgumentException("Cannot access field " + getActuallyOwnerType() + "#"
                    + getName() + " from " + block.getMethodOwner());
        }

        if (LOG.isPrintEnabled()) {
            LOG.print("get field " + getName() + " from class " + getDeclaringClass().getName()
                    + " and push to stack!");
        }
        block.getInsnHelper().getStatic(owner.getType(), getName(), getFormerType().getType());
    }

}
