/*
 *@Type CommandPos.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:35
 * @version
 */
package model.command;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CommandPos implements Serializable {
    private static final long serialVersionUID = 1L;
    private int pos;
    private int len;

    //一个命令位置，包含两个属性：pos 和 len，分别表示位置和长度。
    public CommandPos(int pos, int len) {
        this.pos = pos;
        this.len = len;
    }

    @Override
    public String toString() {
        return "CommandPos{" +
                "pos=" + pos +
                ", len=" + len +
                '}';
    }
}
